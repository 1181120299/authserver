package com.jack.authserver.annotation;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 登录页入口
 * <p></p>
 *
 * 如果要自定义登录页，请提供实现了此接口的Controller
 */
public interface LoginEntryProvider {

    /**
     * 返回登录页的模板，或者转发、重定向到登录页
     * @return  登录页模板
     */
    @GetMapping("/login")
    String login(HttpServletRequest request, HttpServletResponse response);
}
