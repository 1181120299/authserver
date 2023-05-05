package com.jack.authserver.filter;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jack.authserver.entity.Authorities;
import com.jack.authserver.entity.SpringSecurityUser;
import com.jack.authserver.mapper.AuthoritiesMapper;
import com.jack.authserver.mapper.SpringSecurityUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Objects;

public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private SpringSecurityUserMapper springSecurityUserMapper;
    @Autowired
    private AuthoritiesMapper authoritiesMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SpringSecurityUser user = springSecurityUserMapper.selectOne(new LambdaQueryWrapper<SpringSecurityUser>()
                .eq(SpringSecurityUser::getUsername, username));
        if (Objects.isNull(user)) {
            return null;
        }

        List<Authorities> authoritiesList = authoritiesMapper.selectList(new LambdaQueryWrapper<Authorities>()
                .eq(Authorities::getUsername, user.getUsername()));

        return User.withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(authoritiesList.stream().map(Authorities::getAuthority).toList().toArray(new String[]{}))
                .build();
    }
}
