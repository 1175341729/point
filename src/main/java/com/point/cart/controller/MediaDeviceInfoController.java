package com.point.cart.controller;

import com.alibaba.fastjson.JSONObject;
import com.point.cart.common.rsp.MessageRsp;
import com.point.cart.model.MediaDeviceInfo;
import com.point.cart.service.MediaDeviceInfoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/device")
public class MediaDeviceInfoController {

    @Resource
    private MediaDeviceInfoService mediaDeviceInfoService;
    /**
     * 列表
     * @param req 请求
     * @return 响应
     */
    @GetMapping("")
    public MessageRsp searchList(MediaDeviceInfo req
            , @RequestParam(value = "limit",required = false,defaultValue = "10") Integer limit
            , @RequestParam(value = "offset",required = false) Integer offset){
        MessageRsp rsp = mediaDeviceInfoService.searchList(req,limit,offset);
        return rsp;
    }

    /**
     * 播控提供接口
     * @param province 省
     * @param city 市
     * @param area 区
     * @return 响应
     */
    @GetMapping("/info")
    public JSONObject mediaInfo(@RequestParam(required = false) String province, @RequestParam(required = false) String city, @RequestParam(required = false) String area) {
        JSONObject rsp = mediaDeviceInfoService.mediaInfo(province, city, area);
        return rsp;
    }

    /**
     * 播控提供接口
     * @param mac mac地址
     * @return 响应
     */
    @GetMapping("/mediaInfoByMac")
    public JSONObject mediaInfoByMac(@RequestParam(required = false) String mac) {
        JSONObject rsp = mediaDeviceInfoService.mediaInfoByMac(mac);
        return rsp;
    }
}
