package com.jack.authserver.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jack.authserver.entity.Authorities;
import com.jack.authserver.entity.SpringSecurityUser;
import com.jack.authserver.mapper.AuthoritiesMapper;
import com.jack.authserver.mapper.SpringSecurityUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * 处理自定义用户数据
 * <p></p>
 * 资源服务器自定义用户信息，会发送RabbitMq消息。
 */
@Slf4j
@Component
public class CustomUserMqMessageConsumer {

    @Autowired
    private SpringSecurityUserMapper springSecurityUserMapper;
    @Autowired
    private AuthoritiesMapper authoritiesMapper;

    /**
     * 处理 新增用户 的消息
     * @param userData  用户数据
     */
    @RabbitListener(queues = "jack-custom-addUser-queue", errorHandler = "rabbitDefaultErrorHandler")
    @Transactional
    public void addUser(String userData) {
        SpringSecurityUser user = JSON.parseObject(userData, SpringSecurityUser.class);
        Assert.hasText(user.getPassword(), "password can not be empty");

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

    /**
     * 处理 删除用户 的消息
     * @param username  用户名
     */
    @RabbitListener(queues = "jack-custom-deleteUser-queue", errorHandler = "rabbitDefaultErrorHandler")
    @Transactional
    public void deleteUser(String username) {
        if (!StringUtils.hasText(username)) {
            log.debug("The username is empty");
            return;
        }

        authoritiesMapper.delete(new LambdaQueryWrapper<Authorities>()
                .eq(Authorities::getUsername, username));
        springSecurityUserMapper.deleteById(username);
    }
}
