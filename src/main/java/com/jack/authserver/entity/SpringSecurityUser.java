package com.jack.authserver.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * Spring Security 需要的用户信息
 * 
 * @author chenjiabao
 * @email 1181120299@qq.com
 * @date 2023-04-15 11:12:17
 */
@Data
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("users")
public class SpringSecurityUser {
	
	/**
	* 用户名
	*/
	@TableId(value = "username")
	private String username;

	/**
	* 密码
	*/
	private String password;

	/**
	* 是否启用。true: 启用
	*/
	private Boolean enabled;

}
