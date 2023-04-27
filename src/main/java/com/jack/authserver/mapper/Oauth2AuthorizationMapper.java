package com.jack.authserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jack.authserver.entity.Oauth2Authorization;
import org.apache.ibatis.annotations.Mapper;

/**
 * 
 * 
 * @author chenjiabao
 * @email 1181120299@qq.com
 * @date 2023-04-27 16:16:32
 */
@Mapper
public interface Oauth2AuthorizationMapper extends BaseMapper<Oauth2Authorization> {
	
}
