package com.jack.authserver.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jack.authserver.annotation.RabbitOperation;
import com.jack.authserver.config.RabbitmqConstant;
import com.jack.authserver.entity.Authorities;
import com.jack.authserver.entity.SpringSecurityUser;
import com.jack.authserver.mapper.AuthoritiesMapper;
import com.jack.authserver.mapper.SpringSecurityUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * 处理自定义用户数据
 * <p></p>
 * 资源服务器自定义用户信息，会发送RabbitMq消息。
 */
@Slf4j
public class CustomUserMqMessageConsumer {

    /**
     * 授权服务器内置的账号。不允许修改、删除。
     */
    public static final String EMBED_USERNAME = "jack";

    @Autowired
    private SpringSecurityUserMapper springSecurityUserMapper;
    @Autowired
    private AuthoritiesMapper authoritiesMapper;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 处理 自定义用户 的消息
     * @param operationData  用户操作数据
     */
    @RabbitListener(queues = "jack-custom-user-queue", errorHandler = "rabbitDefaultErrorHandler")
    @Transactional
    public void customUserOperation(String operationData) {
        RabbitOperation<?> rabbitOperation = JSON.parseObject(operationData, RabbitOperation.class);
        switch (rabbitOperation.getOp()) {
            case ADD -> {
                SpringSecurityUser springSecurityUser = JSON.parseObject(JSON.toJSONString(rabbitOperation.getData()),
                        SpringSecurityUser.class);
                addUser(springSecurityUser);
            }
            case DELETE -> deleteUser(rabbitOperation.getData().toString());
            case UPDATE -> {
                SpringSecurityUser springSecurityUser = JSON.parseObject(JSON.toJSONString(rabbitOperation.getData()),
                        SpringSecurityUser.class);
                updateUser(springSecurityUser);
            }
            default -> throw new IllegalArgumentException("Unsupported Operation: {}" + rabbitOperation.getOp());
        }
    }

    private void addUser(SpringSecurityUser user) {
        Assert.hasText(user.getPassword(), "password can not be empty");
        if (EMBED_USERNAME.equals(user.getUsername())) {
            log.error("username: {} already existed, and it is an embed account. You should not operate this account.", EMBED_USERNAME);
            throw new IllegalArgumentException("Invalid username: " + EMBED_USERNAME);
        }

        SpringSecurityUser existedUser = springSecurityUserMapper.selectOne(new LambdaQueryWrapper<SpringSecurityUser>()
                .eq(SpringSecurityUser::getUsername, user.getUsername()));
        if (existedUser != null) {
            log.info("Already existed user: {}", user);
            return;
        }

        if (user.getEnabled() == null) {
            user.setEnabled(true);
        }

        springSecurityUserMapper.insert(user);
        authoritiesMapper.insert(Authorities.builder()
                .username(user.getUsername())
                .authority("ROLE_USER").build());
        log.debug("Added user: {}", user.getUsername());
    }

    private void deleteUser(String username) {
        if (EMBED_USERNAME.equals(username)) {
            log.error("username: {} already existed, and it is an embed account. You should not operate this account.", EMBED_USERNAME);
            throw new IllegalArgumentException("Invalid username: " + EMBED_USERNAME);
        }

        authoritiesMapper.delete(new LambdaQueryWrapper<Authorities>()
                .eq(Authorities::getUsername, username));
        springSecurityUserMapper.deleteById(username);

        RabbitOperation<Object> rabbitOperation = RabbitOperation.builder()
                .op(RabbitOperation.OP.DELETE)
                .data(username)
                .build();
        rabbitTemplate.convertAndSend(RabbitmqConstant.SPRING_SECURITY_USER.exchange,
                RabbitmqConstant.SPRING_SECURITY_USER.routeKey,
                JSON.toJSONString(rabbitOperation));
    }

    private void updateUser(SpringSecurityUser user) {
        if (EMBED_USERNAME.equals(user.getUsername())) {
            log.error("username: {} already existed, and it is an embed account. You should not operate this account.", EMBED_USERNAME);
            throw new IllegalArgumentException("Invalid username: " + EMBED_USERNAME);
        }

        if (user.getPassword() != null
                || user.getEnabled() != null
                || user.getPhone() != null) {
            springSecurityUserMapper.updateById(user);
        }

        RabbitOperation<Object> rabbitOperation = RabbitOperation.builder()
                .op(RabbitOperation.OP.UPDATE)
                .data(user)
                .build();
        rabbitTemplate.convertAndSend(RabbitmqConstant.SPRING_SECURITY_USER.exchange,
                RabbitmqConstant.SPRING_SECURITY_USER.routeKey,
                JSON.toJSONString(rabbitOperation));
    }
}
