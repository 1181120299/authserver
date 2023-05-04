package com.jack.authserver.filter;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jack.authserver.entity.SpringSecurityUser;
import com.jack.authserver.mapper.SpringSecurityUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private SpringSecurityUserMapper springSecurityUserMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return springSecurityUserMapper.selectOne(new LambdaQueryWrapper<SpringSecurityUser>()
                .eq(SpringSecurityUser::getUsername, username));
    }
}
