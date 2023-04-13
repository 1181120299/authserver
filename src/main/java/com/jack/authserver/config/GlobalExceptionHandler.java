package com.jack.authserver.config;

import com.jack.utils.web.R;
import com.jack.utils.web.RRException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
@Order(value = Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

    @ExceptionHandler({MissingServletRequestParameterException.class, MissingServletRequestPartException.class, HttpRequestMethodNotSupportedException.class})
    public R missingServletRequestParameter(Exception e) {
        log.debug(e.getMessage(), e);
        return R.error(e.getMessage());
    }

    @ExceptionHandler(RRException.class)
    public R handleRRException(Exception e) {
        log.debug(e.getMessage(), e);
        return R.error(e.getMessage());
    }

    @ExceptionHandler({BindException.class})
    public R handleBindExceptionException(BindException e)  {
        log.debug(e.getMessage(), e);
        return R.error(e.getBindingResult().getFieldError().getDefaultMessage());
    }

    @ExceptionHandler({ConstraintViolationException.class})
    public R handleConstraintViolationException(ConstraintViolationException e)  {
        log.debug(e.getMessage(), e);
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        return R.error(message);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R handleMethodArgumentNotValidException(MethodArgumentNotValidException e){
        log.debug(e.getMessage(), e);
        String message = e.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return R.error(message);
    }
}
