package com.point.cart.service;

import com.point.cart.common.rsp.MessageRsp;
import com.point.cart.model.AbnormalCartPoint;

public interface AbnormalPointCartService {

    MessageRsp searchArea(String areaType, String areaName, Integer level);

    MessageRsp searchList(AbnormalCartPoint req,Integer limit,Integer offset);

    MessageRsp export(AbnormalCartPoint req);
}
