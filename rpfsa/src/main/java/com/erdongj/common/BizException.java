package com.erdongj.common;

import com.erdongj.pojo.ResultBean;

/**
 * 业务异常。由 {@link GlobalExceptionHandler} 统一转换为响应体，
 * 避免在 Controller / Service 里到处写 if-else + 手工拼 ResultBean。
 */
public class BizException extends RuntimeException {

    private final int code;

    public BizException(String message) {
        this(ResultBean.FAIL, message);
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
