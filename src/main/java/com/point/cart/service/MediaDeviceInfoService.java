package com.point.cart.service;

import com.alibaba.fastjson.JSONObject;
import com.point.cart.common.rsp.MessageRsp;
import com.point.cart.model.MediaDeviceInfo;

public interface MediaDeviceInfoService {

    MessageRsp searchList(MediaDeviceInfo req, Integer limit, Integer offset);

    JSONObject mediaInfo(String province, String city, String area);

    JSONObject mediaInfoByMac(String mac);
}
