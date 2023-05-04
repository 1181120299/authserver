package com.jack.authserver.config;

import com.jack.authserver.filter.PhoneAuthenticationProvider;
import com.jack.authserver.filter.PhoneNumAuthenticationFilter;
import com.jack.authserver.filter.UserDetailsServiceImpl;
import com.jack.authserver.security.FederatedIdentityConfigurer;
import com.jack.authserver.security.UserRepositoryOAuth2UserHandler;
import com.jack.authserver.service.impl.CustomUserMqMessageConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.SecurityContextConfigurer;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.DelegatingSecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.util.ReflectionUtils;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.List;

@Slf4j
@EnableWebSecurity
@Configuration
public class DefaultSecurityConfig {

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        FederatedIdentityConfigurer federatedIdentityConfigurer = new FederatedIdentityConfigurer()
                .oauth2UserHandler(new UserRepositoryOAuth2UserHandler());

        http
                .authorizeHttpRequests(authorize ->
                        authorize
                                .requestMatchers("/assets/**", "/webjars/**", "/login",
                                        "/phone/code", "/phone/login").permitAll()
                                .anyRequest().authenticated())
                // Form login handles the redirect to the login page from the authorization server filter chain
                .formLogin(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .apply(federatedIdentityConfigurer);

        addPhoneLoginFilter(http);
        return http.build();
    }

    // 添加手机号登录的支持
    private void addPhoneLoginFilter(HttpSecurity http) throws Exception {
        PhoneNumAuthenticationFilter authFilter = phoneNumAuthenticationFilter();
        SessionAuthenticationStrategy sessionAuthenticationStrategy = new CompositeSessionAuthenticationStrategy(List
                .of(new ChangeSessionIdAuthenticationStrategy()));
        authFilter.setSessionAuthenticationStrategy(sessionAuthenticationStrategy);

        SecurityContextConfigurer securityContextConfigurer = http.getConfigurer(SecurityContextConfigurer.class);
        if (securityContextConfigurer != null) {
            Field requireExplicitSaveField = ReflectionUtils.findField(securityContextConfigurer.getClass(), "requireExplicitSave");
            ReflectionUtils.makeAccessible(requireExplicitSaveField);
            boolean requireExplicitSave = (boolean) ReflectionUtils.getField(requireExplicitSaveField, securityContextConfigurer);
            if (requireExplicitSave) {
                SecurityContextRepository securityContextRepository = new DelegatingSecurityContextRepository(new RequestAttributeSecurityContextRepository(),
                        new HttpSessionSecurityContextRepository());
                authFilter.setSecurityContextRepository(securityContextRepository);
            }
        }

        http.addFilterAfter(authFilter, UsernamePasswordAuthenticationFilter.class)
                .authenticationProvider(phoneAuthenticationProvider());

        // Also support username password login.
        http.userDetailsService(authserverUserDetailsServiceImpl());
    }

    @Bean
    public UserDetailsManager userDetailsManager(DataSource dataSource) {
        JdbcUserDetailsManager manager = new JdbcUserDetailsManager(dataSource);
        String adminUsername = CustomUserMqMessageConsumer.EMBED_USERNAME;
        try {
            manager.loadUserByUsername(adminUsername);
        } catch (UsernameNotFoundException e) {
            log.info("Init admin user: {}", adminUsername);

            UserDetails adminUser = User.withUsername(adminUsername)
                    .password("{bcrypt}$2a$10$HNWcgEl9PAFeeN389VFntuVHy8tpx0h/PzXgIuRoQjI/0t3AldSyW")   // 123456
                    .roles("USER")
                    .build();
            manager.createUser(adminUser);
        }

        return manager;
    }

    public static void main(String[] args) {
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        String encodeStr = encoder.encode("123456");
        System.out.println(encodeStr);
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    private DefaultAuthenticationEventPublisher publisher;

    @Autowired
    public void setPublisher(DefaultAuthenticationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Bean
    public PhoneNumAuthenticationFilter phoneNumAuthenticationFilter() {
        PhoneNumAuthenticationFilter filter = new PhoneNumAuthenticationFilter();
        ProviderManager providerManager = new ProviderManager(phoneAuthenticationProvider());
        providerManager.setAuthenticationEventPublisher(publisher);
        filter.setAuthenticationManager(providerManager);

        // If you need return json data when login success or fail
        /*filter.setAuthenticationManager(new ProviderManager(phoneAuthenticationProvider()));
        filter.setAuthenticationSuccessHandler(((request, response, authentication) -> {
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write(JSONObject.toJSONString(R.ok().setData(authentication.getName())));
        }));

        filter.setAuthenticationFailureHandler(((request, response, exception) -> {
            log.error("Phone login fail.", exception);
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write(JSONObject.toJSONString(R.error(exception.getMessage())));
        }));*/

        filter.setAuthenticationSuccessHandler(new SavedRequestAwareAuthenticationSuccessHandler());
        filter.setAuthenticationFailureHandler(((request, response, exception) -> {
            DefaultRedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
            String url = "/login?loginType=phone&error";
            redirectStrategy.sendRedirect(request, response, url);
        }));

        return filter;
    }

    @Bean
    public PhoneAuthenticationProvider phoneAuthenticationProvider() {
        return new PhoneAuthenticationProvider();
    }

    @Bean
    public UserDetailsServiceImpl authserverUserDetailsServiceImpl() {
        return new UserDetailsServiceImpl();
    }
}
