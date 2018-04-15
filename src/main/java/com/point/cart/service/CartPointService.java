package com.point.cart.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.point.cart.common.rsp.MessageRsp;
import com.point.cart.message.req.CartScreenInfoReq;
import com.point.cart.message.req.ModifyCartPointReq;
import com.point.cart.message.req.SearchScreenMediaInfoReq;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface CartPointService {
    MessageRsp searchArea(String areaType, String areaName, Integer level, String cartNumber);

    MessageRsp searchSellerTypeList();

    MessageRsp searchTradeTypeList();

    MessageRsp searchDev();

    MessageRsp searchBusinessAreaList(String province,String city,String area);

    MessageRsp searchScreenMediaInfo(SearchScreenMediaInfoReq req);

    MessageRsp addPointCart(SearchScreenMediaInfoReq req);

    MessageRsp getCartInfo(String cartNumber,Map<String,Object> giveAwayCondition);

    MessageRsp deleteBySelectScreen(CartScreenInfoReq req);

    MessageRsp cartScreenInfo(SearchScreenMediaInfoReq req);

    MessageRsp confirmOrder(String cartNumber, JSONObject req);

    MessageRsp compilePointCart(String cartNumber);

    MessageRsp exportCartPoint(String cartNumber) throws Exception;

    MessageRsp getOrderCartNumber();

    MessageRsp getCartPointParam(String cartNumber);

    MessageRsp importCartPoint(MultipartFile multipartFile, SearchScreenMediaInfoReq req) throws IOException, Exception;

    MessageRsp modifyCartPoint(ModifyCartPointReq req);

    MessageRsp addGiveAwayPoint(SearchScreenMediaInfoReq req);

    MessageRsp giveAwayPointCondition(String cartNumber, Map<String,String> giveAwayCondition);

    MessageRsp confirmPoint(String cartNumber, JSONObject req);

    MessageRsp deleteByCity(CartScreenInfoReq req);

    MessageRsp modifyGiveWayCartPoint(ModifyCartPointReq req);

    MessageRsp searchQuality();

    MessageRsp importGiveAwayCartPoint(MultipartFile multipartFile, SearchScreenMediaInfoReq req) throws Exception;

    MessageRsp getStatisticsByCartNumber(String cartNumber);

    MessageRsp getCartInfoList(String cartNumber, Map<String, Object> giveAwayCondition);

    MessageRsp getPutDate(String cartNumber);

    MessageRsp getCartInfoDetail(String cartNumber, Map<String, Object> giveAwayCondition);

    MessageRsp deleteAllPointType(String cartNumber);

    MessageRsp getStatistics(String cartNumber);

    MessageRsp contract(String cartNumber);

    MessageRsp splitCart(String cartNumber);

    MessageRsp playDeleteByCity(CartScreenInfoReq req);

    MessageRsp playDeleteBySelectScreen(CartScreenInfoReq req);

    MessageRsp playDeleteAllPointType(String cartNumber);

    MessageRsp playCartInfoList(String cartNumber, Map<String, Object> giveAwayCondition);

    MessageRsp playConfirmPoint(String cartNumber, CartScreenInfoReq req);

    MessageRsp playConfirmOrder(String cartNumber, CartScreenInfoReq req);

    MessageRsp getPlayCartInfoDetail(String cartNumber, Map<String, Object> giveAwayCondition);

    MessageRsp playExportCartPoint(String cartNumber,CartScreenInfoReq req) throws Exception;

    MessageRsp playSplitCart(CartScreenInfoReq req);

    MessageRsp cancel(String cartNumber);

    MessageRsp deleteInvalidPointCart(Integer system, List<String> cartNumberList);

    MessageRsp playSupervisionExportCartPoint(String cartNumber, CartScreenInfoReq req);
}
