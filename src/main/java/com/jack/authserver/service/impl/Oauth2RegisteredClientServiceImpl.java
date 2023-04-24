package com.jack.authserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jack.utils.web.RRException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.jack.authserver.mapper.Oauth2RegisteredClientMapper;
import com.jack.authserver.entity.Oauth2RegisteredClient;
import com.jack.authserver.service.Oauth2RegisteredClientService;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.UUID;

@Slf4j
@Service("oauth2RegisteredClientService")
public class Oauth2RegisteredClientServiceImpl extends ServiceImpl<Oauth2RegisteredClientMapper, Oauth2RegisteredClient> implements Oauth2RegisteredClientService {

    @Override
    public boolean save(Oauth2RegisteredClient entity) {
        Oauth2RegisteredClient existedClient = baseMapper.selectOne(new LambdaQueryWrapper<Oauth2RegisteredClient>()
                .eq(Oauth2RegisteredClient::getClientId, entity.getClientId()));
        if (existedClient != null) {
            throw new RRException("应用名称已存在");
        }

        entity.setRedirectUris(generateRedirectUris(entity.getRedirectUriSimple()));
        entity.setClientIdIssuedAt(new Date());
        entity.setClientSecret("{noop}secret");
        entity.setClientName(UUID.randomUUID().toString());
        entity.setClientAuthenticationMethods("client_secret_basic");
        entity.setAuthorizationGrantTypes("refresh_token,client_credentials,authorization_code");
        entity.setScopes("openid,profile,message.read,message.write");
        entity.setClientSettings("{\"@class\":\"java.util.Collections$UnmodifiableMap\",\"settings.client.require-proof-key\":false,\"settings.client.require-authorization-consent\":true}");
        entity.setTokenSettings("{\"@class\":\"java.util.Collections$UnmodifiableMap\",\"settings.token.reuse-refresh-tokens\":true,\"settings.token.id-token-signature-algorithm\":[\"org.springframework.security.oauth2.jose.jws.SignatureAlgorithm\",\"RS256\"],\"settings.token.access-token-time-to-live\":[\"java.time.Duration\",300.000000000],\"settings.token.access-token-format\":{\"@class\":\"org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat\",\"value\":\"self-contained\"},\"settings.token.refresh-token-time-to-live\":[\"java.time.Duration\",3600.000000000],\"settings.token.authorization-code-time-to-live\":[\"java.time.Duration\",300.000000000]}");

        return baseMapper.insert(entity) > 0;
    }

    private String generateRedirectUris(String simple) {
        if (!StringUtils.hasText(simple)) {
            return simple;
        }

        if (!simple.endsWith("/")) {
            simple += "/";
        }

        return simple + "authorized," + simple + "login/oauth2/code/jack-client-oidc";
    }

    @Override
    public boolean updateById(Oauth2RegisteredClient entity) {
        entity.setClientId(null);
        String clientSecret = entity.getClientSecret();
        if (clientSecret != null && !clientSecret.startsWith("{")) {
            clientSecret = "{noop}" + clientSecret;
            entity.setClientSecret(clientSecret);
        }

        entity.setRedirectUris(generateRedirectUris(entity.getRedirectUriSimple()));
        return baseMapper.updateById(entity) > 0;
    }
}
