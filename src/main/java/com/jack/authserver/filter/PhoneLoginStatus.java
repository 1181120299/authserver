package com.jack.authserver.filter;

/**
 * 手机号登录状态码
 */
public class PhoneLoginStatus {

    /**
     * 验证码已过期
     */
    public static final String EXPIRE = "EXPIRE";

    /**
     * 验证码不正确
     */
    public static final String INVALID = "INVALID";

    /**
     * 用户不存在
     */
    public static final String USER_NOT_EXIST = "USER_NOT_EXIST";
}
