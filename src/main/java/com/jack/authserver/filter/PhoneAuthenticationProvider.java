package com.jack.authserver.filter;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jack.authserver.entity.Authorities;
import com.jack.authserver.entity.SpringSecurityUser;
import com.jack.authserver.mapper.AuthoritiesMapper;
import com.jack.authserver.mapper.SpringSecurityUserMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.List;

/**
 * 主要实现 authenticate 方法，写我们自己的认证逻辑
 */
public class PhoneAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private SpringSecurityUserMapper springSecurityUserMapper;
    @Autowired
    private AuthoritiesMapper authoritiesMapper;

    /**
     * 手机号、验证码的认证逻辑
     * @param authentication 其实就是我们封装的 PhoneNumAuthenticationToken
     * @return
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        PhoneNumAuthenticationToken token = (PhoneNumAuthenticationToken) authentication;
        String phone = (String) authentication.getPrincipal();// 获取手机号
        String num = (String) authentication.getCredentials(); // 获取输入的验证码
        // 1. 从 session 中获取验证码
        HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String phoneNum = (String) req.getSession().getAttribute("phoneNum");
        if (!StringUtils.hasText(phoneNum)) {
            throw new BadCredentialsException("验证码已经过期，请重新发送验证码");
        }

        if (!phoneNum.equals(num)) {
            throw new BadCredentialsException("验证码不正确");
        }

        // 2. 根据手机号查询用户信息
        SpringSecurityUser loginUser = springSecurityUserMapper.selectOne(new LambdaQueryWrapper<SpringSecurityUser>()
                .eq(SpringSecurityUser::getPhone, phone));
        if (loginUser == null) {
            throw new BadCredentialsException("用户不存在，请注册");
        }

        List<Authorities> authoritiesList = authoritiesMapper.selectList(new LambdaQueryWrapper<Authorities>()
                .eq(Authorities::getUsername, loginUser.getUsername()));
        List<GrantedAuthority> grantedAuthorityList = new ArrayList<>(authoritiesList.size());
        authoritiesList.forEach(authorities -> grantedAuthorityList
                .add(new SimpleGrantedAuthority(authorities.getAuthority())));

        UsernamePasswordAuthenticationToken authenticationResult = new UsernamePasswordAuthenticationToken(loginUser,
                loginUser.getPassword(), grantedAuthorityList);
        authenticationResult.setDetails(token.getDetails());
        return authenticationResult;
    }

    /**
     * 判断是上面 authenticate 方法的 authentication 参数，是哪种类型
     * Authentication 是个接口，实现类有很多，目前我们最熟悉的就是 PhoneNumAuthenticationToken、UsernamePasswordAuthenticationToken
     * 很明显，我们只支持 PhoneNumAuthenticationToken，因为它封装的是手机号、验证码
     * @param authentication
     * @return
     */
    @Override
    public boolean supports(Class<?> authentication) {
        // 如果参数是 PhoneNumAuthenticationToken 类型，返回true
        return (PhoneNumAuthenticationToken.class.isAssignableFrom(authentication));
    }
}