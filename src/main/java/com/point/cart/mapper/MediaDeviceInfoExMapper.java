package com.point.cart.mapper;

import com.point.cart.message.rsp.MediaDeviceInfoRsp;

import java.util.List;
import java.util.Map;

public interface MediaDeviceInfoExMapper{
    Integer count(Map<String,Object> param);
    List<MediaDeviceInfoRsp> selectByExampleCustom(Map<String,Object> example);
}