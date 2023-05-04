package com.jack.authserver.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Properties;

/**
 * 手机号登录相关的控制器
 */
@RestController
@RequestMapping("/phone")
public class PhoneController {

    @GetMapping("/code")
    public String phoneCode(HttpSession session) throws IOException {
        //验证码配置
        Properties properties = new Properties();
        properties.setProperty("kaptcha.image.width", "150");
        properties.setProperty("kaptcha.image.height", "50");
        properties.setProperty("kaptcha.textproducer.char.string", "0123456789");
        properties.setProperty("kaptcha.textproducer.char.length", "4");
//        Config config = new Config(properties);
//        DefaultKaptcha kaptcha = new DefaultKaptcha();
//        kaptcha.setConfig(config);
//
//        //生成验证码
//        String code = kaptcha.createText();
        session.setAttribute("phoneNum", "陈家宝");
        return "陈家宝";
    }

    @PostMapping("/login")
    public String phoneLogin() {
        return "===================phoneLogin";
    }
}
