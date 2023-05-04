package com.jack.authserver.filter;

import com.alibaba.fastjson.JSONObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.collections4.MapUtils;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

/**
 * 支持手机号验证码登录的过滤器
 */
public class PhoneNumAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final AntPathRequestMatcher DEFAULT_ANT_PATH_REQUEST_MATCHER =
            new AntPathRequestMatcher("/phone/login", "POST");

    private final String phoneParameter = "phone";
    private final String numParameter = "num";


    public PhoneNumAuthenticationFilter() {
        super(DEFAULT_ANT_PATH_REQUEST_MATCHER);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        if (!"POST".equals(request.getMethod())) {
            throw new AuthenticationServiceException("请求方式有误: " + request.getMethod());
        }

        String phone;
        String num;

        switch (request.getContentType()) {
            case MediaType.APPLICATION_JSON_VALUE -> {
                Map<String, String> paramMap = JSONObject.parseObject(request.getInputStream(), Map.class);
                phone = MapUtils.getString(paramMap, phoneParameter);
                num = MapUtils.getString(paramMap, numParameter);
            }
            case MediaType.APPLICATION_FORM_URLENCODED_VALUE -> {
                phone = request.getParameter(phoneParameter);
                num = request.getParameter(numParameter);
            }
            default -> throw new UnsupportedOperationException("Unsupported media type: " + request.getContentType());
        }

        phone = Optional.ofNullable(phone).orElse("");
        num = Optional.ofNullable(num).orElse("");

        PhoneNumAuthenticationToken authRequest = new PhoneNumAuthenticationToken(phone, num);
        //设置ip、sessionId信息
        setDetails(request,authRequest);
        return this.getAuthenticationManager().authenticate(authRequest);
    }

    protected void setDetails(HttpServletRequest request, PhoneNumAuthenticationToken authRequest) {
        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
    }
}