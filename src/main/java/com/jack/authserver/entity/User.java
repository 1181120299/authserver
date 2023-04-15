package com.jack.authserver.entity;

import lombok.*;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;

import java.util.Date;

/**
 * 用户信息（定义业务系统需要的用户信息）
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
@TableName("t_user")
public class User {
	
	/**
	* 用户名
	*/
	@TableId(value = "username")
	private String username;

	/**
	* 性别。1：男，0：女
	*/
	private Integer gender;

	/**
	* 爱好
	*/
	private String hobby;

}
