package com.jack.authserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jack.authserver.entity.Oauth2RegisteredClient;
import com.jack.authserver.mapper.Oauth2RegisteredClientMapper;
import com.jack.authserver.service.Oauth2RegisteredClientService;
import com.jack.utils.web.RRException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
public class Oauth2RegisteredClientServiceImpl extends ServiceImpl<Oauth2RegisteredClientMapper, Oauth2RegisteredClient> implements Oauth2RegisteredClientService {

    /**
     * OAuth client是否需要授权确认页面。
     */
    private static final boolean requireAuthorizationConsent = false;

    @PostConstruct
    public void init() {
        List<Oauth2RegisteredClient> registeredClientList = baseMapper.selectList(new LambdaQueryWrapper<Oauth2RegisteredClient>()
                .select(Oauth2RegisteredClient::getId));
        if (CollectionUtils.isNotEmpty(registeredClientList)) {
            return;
        }

        log.info("Init one client for resource server.");
        Oauth2RegisteredClient entity = new Oauth2RegisteredClient();
        entity.setClientId("resource-server");
        entity.setRedirectUriSimple("http://192.168.1.101:9001/resource");
        entity.setRedirectUris(generateRedirectUris(entity.getRedirectUriSimple()));
        entity.setClientIdIssuedAt(new Date());
        entity.setClientSecret("{noop}secret");
        entity.setClientName(UUID.randomUUID().toString());
        entity.setClientAuthenticationMethods("client_secret_basic");
        entity.setAuthorizationGrantTypes("refresh_token,client_credentials,authorization_code");
        entity.setScopes("openid,profile,message.read,message.write");
        entity.setClientSettings("{\"@class\":\"java.util.Collections$UnmodifiableMap\",\"settings.client.require-proof-key\":false,\"settings.client.require-authorization-consent\":" + requireAuthorizationConsent + "}");
        entity.setTokenSettings("{\"@class\":\"java.util.Collections$UnmodifiableMap\",\"settings.token.reuse-refresh-tokens\":true,\"settings.token.id-token-signature-algorithm\":[\"org.springframework.security.oauth2.jose.jws.SignatureAlgorithm\",\"RS256\"],\"settings.token.access-token-time-to-live\":[\"java.time.Duration\",300.000000000],\"settings.token.access-token-format\":{\"@class\":\"org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat\",\"value\":\"self-contained\"},\"settings.token.refresh-token-time-to-live\":[\"java.time.Duration\",3600.000000000],\"settings.token.authorization-code-time-to-live\":[\"java.time.Duration\",300.000000000]}");
        entity.setDescription("资源服务器");
        baseMapper.insert(entity);
    }

    @Override
    public boolean save(Oauth2RegisteredClient entity) {
        Oauth2RegisteredClient existedClient = baseMapper.selectOne(new LambdaQueryWrapper<Oauth2RegisteredClient>()
                .eq(Oauth2RegisteredClient::getClientId, entity.getClientId()));
        if (existedClient != null) {
            throw new RRException("应用名称已存在");
        }

        // 要求应用必须设置server.servlet.contextPath，否则单点登录会有问题。
        checkSimpleRedirectUri(entity.getRedirectUriSimple());

        entity.setRedirectUris(generateRedirectUris(entity.getRedirectUriSimple()));
        entity.setClientIdIssuedAt(new Date());
        entity.setClientSecret("{noop}secret");
        entity.setClientName(UUID.randomUUID().toString());
        entity.setClientAuthenticationMethods("client_secret_basic");
        entity.setAuthorizationGrantTypes("refresh_token,client_credentials,authorization_code");
        entity.setScopes("openid,profile,message.read,message.write");
        entity.setClientSettings("{\"@class\":\"java.util.Collections$UnmodifiableMap\",\"settings.client.require-proof-key\":false,\"settings.client.require-authorization-consent\":" + requireAuthorizationConsent + "}");
        entity.setTokenSettings("{\"@class\":\"java.util.Collections$UnmodifiableMap\",\"settings.token.reuse-refresh-tokens\":true,\"settings.token.id-token-signature-algorithm\":[\"org.springframework.security.oauth2.jose.jws.SignatureAlgorithm\",\"RS256\"],\"settings.token.access-token-time-to-live\":[\"java.time.Duration\",300.000000000],\"settings.token.access-token-format\":{\"@class\":\"org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat\",\"value\":\"self-contained\"},\"settings.token.refresh-token-time-to-live\":[\"java.time.Duration\",3600.000000000],\"settings.token.authorization-code-time-to-live\":[\"java.time.Duration\",300.000000000]}");

        return baseMapper.insert(entity) > 0;
    }

    /**
     * 检查回调地址是否合法。
     * <p></p>
     *
     * 合法的地址示例：http://192.168.1.101:8080/myApp
     * <p></p>
     *
     * 由于单点登录的原因，myApp（应用上下文。即server.servlet.contextPath）是必要的。
     * <p></p>
     *
     * @param uri   回调地址
     * @throws RRException  如果回调地址非法
     */
    private void checkSimpleRedirectUri(String uri) {
        if (!StringUtils.hasText(uri)) {
            return;
        }

        if (uri.contains("127.0.0.1")) {
            throw new RRException("回调地址不能是127.0.0.1");
        }

        if (uri.toLowerCase().contains("localhost")) {
            throw new RRException("回调地址不能是localhost");
        }

       String regex = ".*\\d+\\.\\d+\\.\\d+\\.\\d+(:\\d+)?/.+";
       if (!uri.matches(regex)) {
           log.debug("Invalid redirect uri: {}", uri);
           throw new RRException("回调地址格式不正确。正确格式示例：http://192.168.1.101:8080/myApp");
       }
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

        checkSimpleRedirectUri(entity.getRedirectUriSimple());
        entity.setRedirectUris(generateRedirectUris(entity.getRedirectUriSimple()));
        return baseMapper.updateById(entity) > 0;
    }
}
