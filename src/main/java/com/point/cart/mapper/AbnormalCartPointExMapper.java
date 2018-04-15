package com.point.cart.mapper;

import com.point.cart.message.req.CartScreenInfoReq;
import com.point.cart.message.req.ScreenInfoReq;
import com.point.cart.message.req.SearchScreenMediaInfoReq;
import com.point.cart.message.rsp.ScreenMediaInfoRsp;
import com.point.cart.model.CartPoint;
import com.point.cart.model.PointStatistics;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface AbnormalCartPointExMapper {
    List<Map<String,Object>> areaList(Map<String, Object> param);
}
