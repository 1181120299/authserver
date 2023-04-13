package com.jack.authserver.dto;

import java.util.Date;

import lombok.*;


import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;
import javax.validation.constraints.NotNull;

/**
 * OAuth2已注册客户端Dto
 * 
 * @author chenjiabao
 * @email 1181120299@qq.com
 * @date 2023-04-12 15:02:36
 */
@Data
public class Oauth2RegisteredClientDto {
	
	/**
	* 客户端id，唯一不可重复。作为应用名称
	*/
	@Length(max = 100, message = "客户端id，唯一不可重复。作为应用名称最多100个字符")
	@NotBlank(message = "客户端id，唯一不可重复。作为应用名称不能为空")
	private String clientId;

	/**
	* 重定向地址，多个以英文逗号分割
	*/
	@Length(max = 1000, message = "重定向地址，多个以英文逗号分割最多1000个字符")
	private String redirectUris;

	/**
	* 客户端描述
	*/
	@Length(max = 2000, message = "客户端描述最多2000个字符")
	private String description;

}
