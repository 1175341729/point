package com.point.cart.common.rsp;

import com.fasterxml.jackson.annotation.JsonInclude;

public class MessageRsp {
    private Integer errorcode;
    private String errormsg;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Object data;

    public Integer getErrorcode() {
        return errorcode;
    }

    public void setErrorcode(Integer errorcode) {
        this.errorcode = errorcode;
    }

    public String getErrormsg() {
        return errormsg;
    }

    public void setErrormsg(String errormsg) {
        this.errormsg = errormsg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
