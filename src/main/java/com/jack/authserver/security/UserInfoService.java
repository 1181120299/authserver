package com.jack.authserver.security;

import com.alibaba.fastjson.JSON;
import com.jack.authserver.entity.User;
import com.jack.authserver.mapper.UserMapper;
import jakarta.annotation.Nullable;
import jakarta.annotation.Resource;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

/**
 * Example service to perform lookup of user info for customizing an {@code id_token}.
 */
@Service
public class UserInfoService {

    @Resource
    private UserMapper userMapper;

    @Nullable
    public OidcUserInfo loadUser(String name) {
        User user = userMapper.selectById(name);
        if (Objects.isNull(user)) {
            return null;
        }

        Map map =JSON.parseObject(JSON.toJSONString(user), Map.class);
        return new OidcUserInfo(map);
    }
}
