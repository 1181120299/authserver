package com.jack.authserver.annotation;

/**
 * 短信服务提供者
 */
public interface SmsProvider {

    /**
     * 手机号登录系统，发送验证码短信
     * @param phone 手机号
     * @param code  验证码
     * @throws com.jack.utils.web.RRException   如果短信发送失败
     */
    void sendLoginCode(String phone, String code);
}
