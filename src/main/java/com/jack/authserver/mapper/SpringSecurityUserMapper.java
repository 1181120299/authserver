package com.jack.authserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jack.authserver.entity.SpringSecurityUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * Spring Security 需要的用户信息
 * 
 * @author chenjiabao
 * @email 1181120299@qq.com
 * @date 2023-04-15 11:12:17
 */
@Mapper
public interface SpringSecurityUserMapper extends BaseMapper<SpringSecurityUser> {
	
}
