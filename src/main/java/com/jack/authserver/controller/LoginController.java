package com.jack.authserver.controller;

import com.jack.authserver.annotation.LoginEntryProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@ConditionalOnMissingBean(LoginEntryProvider.class)
public class LoginController implements LoginEntryProvider {

    @Override
    public String login(HttpServletRequest request, HttpServletResponse response) {
        return "login";
    }
}
