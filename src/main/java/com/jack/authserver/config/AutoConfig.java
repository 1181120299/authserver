package com.jack.authserver.config;

import com.jack.authserver.annotation.AliSmsProvider;
import com.jack.authserver.annotation.FixedResponseProcessor;
import com.jack.authserver.annotation.LoginEntryProvider;
import com.jack.authserver.annotation.SmsProvider;
import com.jack.authserver.controller.*;
import com.jack.authserver.service.impl.CustomUserMqMessageConsumer;
import com.jack.authserver.service.impl.Oauth2RegisteredClientServiceImpl;
import com.jack.authserver.service.impl.SpringSecurityUserServiceImpl;
import com.jack.authserver.task.InitAuthorization;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

/**
 * 补充管理需要自动配置的bean
 */
@Configuration
public class AutoConfig {

    @Bean
    public FixedResponseProcessor fixedResponseProcessor() {
        return new FixedResponseProcessor();
    }

    @Bean
    public MapperScannerConfigurer mapperScannerConfigurer() {
        MapperScannerConfigurer configurer = new MapperScannerConfigurer();
        configurer.setBasePackage("com.jack.authserver.mapper");
        return configurer;
    }

    @Bean
    public CustomUserMqMessageConsumer customUserMqMessageConsumer() {
        return new CustomUserMqMessageConsumer();
    }

    @Bean
    public Oauth2RegisteredClientServiceImpl oauth2RegisteredClientService() {
        return new Oauth2RegisteredClientServiceImpl();
    }

    @Bean
    public SpringSecurityUserServiceImpl springSecurityUserService() {
        return new SpringSecurityUserServiceImpl();
    }

    @Bean
    public InitAuthorization initAuthorization() {
        return new InitAuthorization();
    }

    @Bean
    @ConditionalOnMissingBean(SmsProvider.class)
    public AliSmsProvider aliSmsProvider() {
        return new AliSmsProvider();
    }
}
