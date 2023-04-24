package com.jack.authserver.util;

/**
 * 定义接口返回的固定格式
 */
public final class FixedResponse {

    public static final String FIXED_CODE = "fixedCode";
    public static final String FIXED_MSG = "fixedMsg";
    public static final String FIXED_DATA = "fixedData";

    public static final int CODE_OK = 200;
    public static final int CODE_ERROR = 500;

    private FixedResponse() {}
}
