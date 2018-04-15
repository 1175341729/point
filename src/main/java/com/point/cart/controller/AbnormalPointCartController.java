package com.point.cart.controller;

import com.point.cart.common.rsp.MessageRsp;
import com.point.cart.model.AbnormalCartPoint;
import com.point.cart.service.AbnormalPointCartService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 *  异常点位
 */
@RestController
@RequestMapping("/abnormal")
public class AbnormalPointCartController {
    @Resource
    private AbnormalPointCartService abnormalPointCartService;
    /**
     * 省市区查询
     * 通用返回所有层级数据结构
     * @return MessageRsp
     */
    @GetMapping("/searchArea")
    public MessageRsp searchArea(@RequestParam(value = "areaType") String areaType,
                                 @RequestParam(value = "areaName",required = false)String areaName,
                                 @RequestParam(value = "level",required = false) Integer level){
        MessageRsp rsp = abnormalPointCartService.searchArea(areaType,areaName,level);
        return rsp;
    }

    /**
     * 列表
     * @param req 请求
     * @return 响应
     */
    @GetMapping("")
    public MessageRsp searchList(AbnormalCartPoint req
            ,@RequestParam(value = "limit",required = false) Integer limit
            ,@RequestParam(value = "offset",required = false) Integer offset){
        MessageRsp rsp = abnormalPointCartService.searchList(req,limit,offset);
        return rsp;
    }

    /**
     * 导出异常点位
     * @return
     */
    @GetMapping("/export")
    public MessageRsp export(AbnormalCartPoint req){
        MessageRsp rsp = abnormalPointCartService.export(req);
        return rsp;
    }
}
