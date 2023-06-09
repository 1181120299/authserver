package com.jack.authserver.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jack.authserver.entity.SpringSecurityUser;
import com.jack.authserver.mapper.SpringSecurityUserMapper;
import com.jack.authserver.service.SpringSecurityUserService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SpringSecurityUserServiceImpl extends ServiceImpl<SpringSecurityUserMapper, SpringSecurityUser> implements SpringSecurityUserService {

}
