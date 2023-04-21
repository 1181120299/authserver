package com.jack.authserver.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jack.authserver.entity.SpringSecurityUser;
import com.jack.authserver.service.SpringSecurityUserService;
import com.jack.utils.web.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/springSecurityUser")
public class SpringSecurityUserController {

    @Autowired
    private SpringSecurityUserService springSecurityUserService;

    /**
     * 根据用户名，模糊查询用户信息。
     * <p></p>
     *
     * 如果不传用户名，则查询全部用户信息。
     *
     * <p></p>
     * @param username	用户名
     * @return	复合条件的用户信息
     */
    @GetMapping("/list")
    public R list(String username) {
        LambdaQueryWrapper<SpringSecurityUser> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(username)) {
            queryWrapper.like(SpringSecurityUser::getUsername, username);
        }

        List<SpringSecurityUser> userList = springSecurityUserService.list(queryWrapper);
        userList.forEach(user -> user.setPassword(null));
        return R.ok().setData(userList);
    }
}
