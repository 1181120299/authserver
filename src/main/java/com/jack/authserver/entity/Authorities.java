package com.jack.authserver.entity;

import lombok.*;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;

import java.util.Date;

/**
 * 用户-权限
 * 
 * @author chenjiabao
 * @email 1181120299@qq.com
 * @date 2023-04-23 18:42:48
 */
@Data
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("authorities")
public class Authorities {
	
	/**
	* 用户名
	*/
	@TableId(value = "username", type = IdType.ASSIGN_UUID)
	private String username;

	/**
	* 权限
	*/
	private String authority;

}
