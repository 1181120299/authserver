package com.jack.authserver.controller;

import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import javax.validation.constraints.NotNull;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;



import com.jack.authserver.entity.User;
import com.jack.authserver.dto.UserDto;
import com.jack.authserver.service.UserService;

import com.jack.utils.web.R;

import java.util.List;

/**
 * 用户信息（定义业务系统需要的用户信息）
 * 
 * @author chenjiabao
 * @email 1181120299@qq.com
 * @date 2023-04-15 11:12:17
 */
@Validated
@RestController
@RequestMapping("/user")
public class UserController {

	@Resource
	private UserService userService;

//	/**
//	 * 信息
//	 */
//	@GetMapping("/info/{username}")
//	public R info(@PathVariable("username") String username){
//		User user = userService.getById(username);
//
//		return R.ok().setData(user);
//	}
//
//	/**
//	 * 保存
//	 */
//	@PostMapping("/save")
//	public R save(@RequestBody @Validated UserDto userDto){
//		User user = new User();
//		BeanUtils.copyProperties(userDto, user);
//		userService.save(user);
//
//		return R.ok();
//	}
//
//	/**
//	 * 修改
//	 */
//	@PostMapping("/update")
//	public R update(@RequestBody User user){
//		userService.updateById(user);
//
//		return R.ok();
//	}
//
//	/**
//	 * 删除
//	 */
//	@GetMapping("/delete")
//	public R delete(@NotNull(message = "username不能为空") String username){
//		userService.removeById(username);
//
//		return R.ok();
//	}
//
//	/**
//	 * 列表
//	 */
//	@GetMapping("/page")
//	public R page(@RequestParam(required = false, defaultValue = "1") Integer current,
//				  @RequestParam(required = false, defaultValue = "10") Integer size) {
//		IPage<User> page = new Page<>(current, size);
//		LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
//				.orderByDesc(User::getUsername);
//		page = userService.page(page, wrapper);
//
//		return R.ok().setData(page);
//	}
}
