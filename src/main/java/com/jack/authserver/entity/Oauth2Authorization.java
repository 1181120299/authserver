package com.jack.authserver.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;

import java.util.Date;

/**
 * 
 * 
 * @author chenjiabao
 * @email 1181120299@qq.com
 * @date 2023-04-27 16:16:32
 */
@Data
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("oauth2_authorization")
public class Oauth2Authorization {
	
	/**
	* 
	*/
	@TableId(value = "id", type = IdType.ASSIGN_UUID)
	private String id;

	/**
	* 
	*/
	private String registeredClientId;

	/**
	* 
	*/
	private String principalName;

	/**
	* 
	*/
	private String authorizationGrantType;

	/**
	* 
	*/
	private String authorizedScopes;

	/**
	* 
	*/
	private byte[] attributes;

	/**
	* 
	*/
	private String state;

	/**
	* 
	*/
	private byte[] authorizationCodeValue;

	/**
	* 
	*/
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
	private Date authorizationCodeIssuedAt;

	/**
	* 
	*/
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
	private Date authorizationCodeExpiresAt;

	/**
	* 
	*/
	private byte[] authorizationCodeMetadata;

	/**
	* 
	*/
	private byte[] accessTokenValue;

	/**
	* 
	*/
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
	private Date accessTokenIssuedAt;

	/**
	* 
	*/
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
	private Date accessTokenExpiresAt;

	/**
	* 
	*/
	private byte[] accessTokenMetadata;

	/**
	* 
	*/
	private String accessTokenType;

	/**
	* 
	*/
	private String accessTokenScopes;

	/**
	* 
	*/
	private byte[] oidcIdTokenValue;

	/**
	* 
	*/
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
	private Date oidcIdTokenIssuedAt;

	/**
	* 
	*/
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
	private Date oidcIdTokenExpiresAt;

	/**
	* 
	*/
	private byte[] oidcIdTokenMetadata;

	/**
	* 
	*/
	private byte[] refreshTokenValue;

	/**
	* 
	*/
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
	private Date refreshTokenIssuedAt;

	/**
	* 
	*/
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
	private Date refreshTokenExpiresAt;

	/**
	* 
	*/
	private byte[] refreshTokenMetadata;

}
