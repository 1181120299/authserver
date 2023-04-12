package com.jack.authserver.config;

import com.jack.authserver.security.FederatedIdentityConfigurer;
import com.jack.authserver.security.UserRepositoryOAuth2UserHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import javax.sql.DataSource;
import java.util.Objects;

@Slf4j
@EnableWebSecurity
@Configuration(proxyBeanMethods = false)
public class DefaultSecurityConfig {

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        FederatedIdentityConfigurer federatedIdentityConfigurer = new FederatedIdentityConfigurer()
                .oauth2UserHandler(new UserRepositoryOAuth2UserHandler());

        http
                .authorizeHttpRequests(authorize ->
                        authorize
                                .requestMatchers("/assets/**", "/webjars/**", "/login").permitAll()
                                .anyRequest().authenticated())
                // Form login handles the redirect to the login page from the authorization server filter chain
                .formLogin(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .apply(federatedIdentityConfigurer);

        return http.build();
    }

    /*@Bean
    public UserDetailsService userDetailsService() {
        UserDetails userDetails = User.withDefaultPasswordEncoder()
                .username("jack")
                .password("123456")
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(userDetails);
    }*/

    @Bean
    public UserDetailsManager userDetailsManager(DataSource dataSource) {
        JdbcUserDetailsManager manager = new JdbcUserDetailsManager(dataSource);
        String adminUsername = "jack";
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
}
