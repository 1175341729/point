package com.point.cart.common.rsp;

import com.point.cart.common.enums.CommonEnum;
import org.apache.commons.lang.StringUtils;

public class MessageUtil {
    public static MessageRsp success(Object data){
        MessageRsp rsp = new MessageRsp();
        CommonEnum.Message success = CommonEnum.Message.SUCCESS;
        rsp.setErrorcode(success.getCode());
        rsp.setErrormsg(success.getMessage());
        rsp.setData(data);
        return rsp;
    }

    public static MessageRsp error(String message){
        MessageRsp rsp = new MessageRsp();
        CommonEnum.Message success = CommonEnum.Message.ERROR;
        rsp.setErrorcode(success.getCode());
        if (StringUtils.isNotBlank(message)){
            rsp.setErrormsg(message);
        } else {
            rsp.setErrormsg(success.getMessage());
        }
        return rsp;
    }

    public static MessageRsp error(Object data,String message){
        MessageRsp rsp = new MessageRsp();
        CommonEnum.Message success = CommonEnum.Message.ERROR;
        rsp.setErrorcode(success.getCode());
        rsp.setData(data);
        if (StringUtils.isNotBlank(message)){
            rsp.setErrormsg(message);
        } else {
            rsp.setErrormsg(success.getMessage());
        }
        return rsp;
    }

    public static MessageRsp success(){
        MessageRsp rsp = new MessageRsp();
        CommonEnum.Message success = CommonEnum.Message.SUCCESS;
        rsp.setErrorcode(success.getCode());
        rsp.setErrormsg(success.getMessage());
        return rsp;
    }
}
