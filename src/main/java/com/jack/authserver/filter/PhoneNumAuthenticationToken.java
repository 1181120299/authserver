package com.jack.authserver.filter;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * 封装前端传过来的手机号、验证码。
 */
public class PhoneNumAuthenticationToken extends AbstractAuthenticationToken {

    private final Object phone;//手机号

    private final Object num;//验证码

    public PhoneNumAuthenticationToken(Object phone, Object num) {
        super(null);
        this.phone = phone;
        this.num = num;
        setAuthenticated(false);
    }

    public PhoneNumAuthenticationToken(Object phone, Object num, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.phone = phone;
        this.num = num;
        super.setAuthenticated(true); // must use super, as we override
    }

    @Override
    public Object getCredentials() {
        return num;
    }

    @Override
    public Object getPrincipal() {
        return phone;
    }
}