package com.jack.authserver.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import org.hibernate.validator.constraints.Length;

import java.util.Date;

/**
 * OAuth2已注册客户端
 * 
 * @author chenjiabao
 * @email 1181120299@qq.com
 * @date 2023-04-12 15:02:36
 */
@Data
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("oauth2_registered_client")
public class Oauth2RegisteredClient {
	
	/**
	* 主键id
	*/
	@TableId(value = "id", type = IdType.ASSIGN_UUID)
	private String id;

	/**
	* 客户端id，唯一不可重复。作为应用名称
	*/
	private String clientId;

	/**
	* 此客户端发布时间
	*/
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
	private Date clientIdIssuedAt;

	/**
	* 客户端秘钥
	*/
	private String clientSecret;

	/**
	* 此客户端秘钥过期时间
	*/
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
	private Date clientSecretExpiresAt;

	/**
	* uuid名称
	*/
	private String clientName;

	/**
	* 客户端认证方法
	*/
	private String clientAuthenticationMethods;

	/**
	* 授权类型
	*/
	private String authorizationGrantTypes;

	/**
	* 重定向地址，多个以英文逗号分割
	*/
	private String redirectUris;

	/**
	 * 重定向应用地址。例如http://localhost:8080/application
	 */
	private String redirectUriSimple;

	/**
	* 授予的作用域
	*/
	private String scopes;

	/**
	* 客户端设置
	*/
	private String clientSettings;

	/**
	* 访问令牌设置
	*/
	private String tokenSettings;

	/**
	* 客户端描述
	*/
	private String description;

}
