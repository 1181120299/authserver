package com.jack.authserver.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.util.Assert;

import java.util.function.Consumer;

@Slf4j
public class FederatedIdentityConfigurer extends AbstractHttpConfigurer<FederatedIdentityConfigurer, HttpSecurity> {

    private String loginPageUrl = "/login";

    private String authorizationRequestUri;

    private Consumer<OAuth2User> oauth2UserHandler;

    private Consumer<OidcUser> oidcUserHandler;

    /**
     * @param oAuth2UserHandler The {@link java.util.function.Consumer} for performing JIT account provisioning with an OAuth 2.0 IDP
     * @return This configurer for additional configuration
     */
    public FederatedIdentityConfigurer oauth2UserHandler(Consumer<OAuth2User> oAuth2UserHandler) {
        Assert.notNull(oAuth2UserHandler, "oAuth2UserHandler cannot be null");
        this.oauth2UserHandler = oAuth2UserHandler;
        return this;
    }

    public FederatedIdentityConfigurer loginPageUrl(String loginPageUrl) {
        Assert.hasText(loginPageUrl, "loginPageUrl cannot be empty");
        this.loginPageUrl = loginPageUrl;
        return this;
    }

    public FederatedIdentityConfigurer authorizationRequestUri(String authorizationRequestUri) {
        Assert.hasText(authorizationRequestUri, "authorizationRequestUri cannot be empty");
        this.authorizationRequestUri = authorizationRequestUri;
        return this;
    }

    public FederatedIdentityConfigurer oidcUserHandler(Consumer<OidcUser> oidcUserHandler) {
        Assert.notNull(oidcUserHandler, "oidcUserHandler cannot be null");
        this.oidcUserHandler = oidcUserHandler;
        return this;
    }

    @Override
    public void init(HttpSecurity http) throws Exception {
        ApplicationContext applicationContext = http.getSharedObject(ApplicationContext.class);
        ClientRegistrationRepository clientRegistrationRepository = null;
        try {
            clientRegistrationRepository = applicationContext.getBean(ClientRegistrationRepository.class);
        } catch (BeansException e) {
            log.error("You have not config any third party client.", e);
            return;
        }

        FederatedIdentityAuthenticationEntryPoint authenticationEntryPoint = new FederatedIdentityAuthenticationEntryPoint(this.loginPageUrl, clientRegistrationRepository);
        if (this.authorizationRequestUri != null) {
            authenticationEntryPoint.setAuthorizationRequestUri(this.authorizationRequestUri);
        }

        FederatedIdentityAuthenticationSuccessHandler authenticationSuccessHandler = new FederatedIdentityAuthenticationSuccessHandler();
        if (this.oauth2UserHandler != null) {
            authenticationSuccessHandler.setOauth2UserHandler(this.oauth2UserHandler);
        }

        if (this.oidcUserHandler != null) {
            authenticationSuccessHandler.setOidcUserHandler(this.oidcUserHandler);
        }

        http
                .exceptionHandling(exceptionHandling ->
                    exceptionHandling.authenticationEntryPoint(authenticationEntryPoint)
                )
                .oauth2Login(oauth2Login -> {
                    oauth2Login.successHandler(authenticationSuccessHandler);
                    if (this.authorizationRequestUri != null) {
                        String baseUri = this.authorizationRequestUri.replace("/{registrationId}", "");
                        oauth2Login.authorizationEndpoint(authorizationEndpoint ->
                            authorizationEndpoint.baseUri(baseUri)
                        );
                    }
                });
    }
}
