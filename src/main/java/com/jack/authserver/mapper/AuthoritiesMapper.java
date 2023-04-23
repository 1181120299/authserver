package com.jack.authserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jack.authserver.entity.Authorities;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户-权限
 * 
 * @author chenjiabao
 * @email 1181120299@qq.com
 * @date 2023-04-23 18:42:48
 */
@Mapper
public interface AuthoritiesMapper extends BaseMapper<Authorities> {
	
}
