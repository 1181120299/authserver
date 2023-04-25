package com.jack.authserver.annotation;

import com.jack.authserver.util.FixedResponse;
import com.jack.utils.web.R;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * 接口返回增加固定格式，给auth server前端页面使用。增加容错
 */
@Slf4j
@Aspect
public class FixedResponseProcessor {

    @Pointcut("execution(public com.jack.utils.web.R com.jack.authserver.controller..*.*(..))")
    public void pointcut() {}

    /**
     * AfterReturning只切入方法成功执行，如果方法抛出异常（包括业务异常）则不管。
     */
    @AfterReturning(value = "pointcut()", returning = "resp")
    public void afterReturning(R resp) {
        if (resp.getCode() == R.getCodeOk()) {
            resp.put(FixedResponse.FIXED_CODE, FixedResponse.CODE_OK);
        } else {
            resp.put(FixedResponse.FIXED_CODE, FixedResponse.CODE_ERROR);
        }

        resp.put(FixedResponse.FIXED_MSG, resp.getMsg());
        resp.put(FixedResponse.FIXED_DATA, resp.getData());
    }
}
