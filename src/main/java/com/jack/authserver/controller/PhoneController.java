package com.jack.authserver.controller;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import com.jack.authserver.annotation.SmsProvider;
import com.jack.utils.web.R;
import com.jack.utils.web.RRException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Objects;
import java.util.Properties;

/**
 * 手机号登录相关的控制器
 * @since 1.1.0
 */
@RestController
@RequestMapping("/phone")
@Validated
public class PhoneController {

    /**
     * 验证码存放redis的key前缀
     */
    public static final String PREFIX_LOGIN_CODE = "jack.sms.login.code.";
    /**
     * 验证码发送时间限制，存放redis的key前缀
     */
    public static final String PREFIX_LOGIN_SEND_TIMES = "jack.sms.login.send.times.";

    @Autowired
    private SmsProvider smsProvider;
    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping("/code")
    public R phoneCode(@NotBlank(message = "手机号不能为空") String phoneNumber) {
        Object sendTimes = redisTemplate.opsForValue().get(PREFIX_LOGIN_SEND_TIMES + phoneNumber);
        if (sendTimes != null) {
            throw new RRException("发送时间过短，请稍后重试");
        }

        //验证码配置
        Properties properties = new Properties();
        properties.setProperty("kaptcha.image.width", "150");
        properties.setProperty("kaptcha.image.height", "50");
        properties.setProperty("kaptcha.textproducer.char.string", "0123456789");
        properties.setProperty("kaptcha.textproducer.char.length", "6");
        Config config = new Config(properties);
        DefaultKaptcha kaptcha = new DefaultKaptcha();
        kaptcha.setConfig(config);

        //生成验证码
        String code = kaptcha.createText();
        smsProvider.sendLoginCode(phoneNumber, code);
        redisTemplate.opsForValue().set(PREFIX_LOGIN_CODE + phoneNumber, code,
                Duration.of(5, ChronoUnit.MINUTES));
        redisTemplate.opsForValue().set(PREFIX_LOGIN_SEND_TIMES + phoneNumber, 1, Duration.ofSeconds(60));
        return R.ok("发送成功").setData(code);
    }
}
