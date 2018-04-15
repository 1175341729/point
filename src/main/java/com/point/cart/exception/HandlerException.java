package com.point.cart.exception;

import com.point.cart.common.rsp.MessageRsp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class HandlerException {
    private Logger logger = LoggerFactory.getLogger(HandlerException.class);
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public MessageRsp handle(Exception e){
        logger.error(e.getMessage());
        e.printStackTrace();
        MessageRsp message = new MessageRsp();
        if (e instanceof GlobalException){
            GlobalException exception = (GlobalException) e;
            message.setErrorcode(exception.getCode());
            message.setErrormsg(exception.getMessage());
        } else {
            message.setErrorcode(-1);
            message.setErrormsg("服务器异常");
        }
        return  message;
    }
}
