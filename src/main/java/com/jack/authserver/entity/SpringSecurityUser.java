package com.jack.authserver.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

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
public class SpringSecurityUser implements Serializable, UserDetails {

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

	/**
	 * 手机号，不允许重复
	 */
	private String phone;

	/**
	 * 授予的应用权限
	 */
	@TableField(exist = false)
	private Set<GrantedAuthority> authorities;

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}
}
