package com.jack.authserver.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jack.authserver.mapper.Oauth2AuthorizationMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 初始化授权数据。应用启动时，清空之前的授权数据。避免单点登录异常。
 */
public class InitAuthorization {
    @Autowired
    private Oauth2AuthorizationMapper oauth2AuthorizationMapper;

    @PostConstruct
    public void init() {
        oauth2AuthorizationMapper.delete(new LambdaQueryWrapper<>());
    }
}
