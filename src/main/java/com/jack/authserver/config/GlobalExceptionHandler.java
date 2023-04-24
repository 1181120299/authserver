package com.jack.authserver.config;

import com.jack.authserver.util.FixedResponse;
import com.jack.utils.web.R;
import com.jack.utils.web.RRException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
@Order(value = Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

    @ExceptionHandler(RRException.class)
    public R handleRRException(RRException e) {
        log.debug(e.getMessage(), e);
        R errorResp = R.error(e.getMessage());
        errorResp.put(FixedResponse.FIXED_CODE, FixedResponse.CODE_ERROR);
        errorResp.put(FixedResponse.FIXED_MSG, errorResp.getMsg());
        errorResp.put(FixedResponse.FIXED_DATA, errorResp.getData());
        return errorResp;
    }
}
