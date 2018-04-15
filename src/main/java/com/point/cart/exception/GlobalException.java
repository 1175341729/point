package com.point.cart.exception;
// 自定义全局异常
public class GlobalException extends RuntimeException {
    private  Integer code;

    public GlobalException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
