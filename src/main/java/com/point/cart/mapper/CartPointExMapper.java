package com.point.cart.mapper;

import com.point.cart.message.req.CartScreenInfoReq;
import com.point.cart.message.req.ScreenInfoReq;
import com.point.cart.message.req.SearchScreenMediaInfoReq;
import com.point.cart.message.rsp.ScreenMediaInfoRsp;
import com.point.cart.model.CartPoint;
import com.point.cart.model.PointStatistics;
import io.swagger.models.auth.In;
import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public interface CartPointExMapper {
    List<Map<String,Object>> searchArea(@Param("areaType") String areaType, @Param("areaName") String areaName,@Param("subLength") Integer subLength);

    List<Map<String,Object>> searchSellerTypeList();

    List<Map<String,Object>> searchTradeTypeList();

    List<Map<String,Object>> searchDev();

    List<Map<String,Object>> searchBusinessAreaList(Map<String,Object> param);

    List<ScreenMediaInfoRsp> searchScreenMediaInfo(SearchScreenMediaInfoReq req);

    Integer addPointCart(@Param("cartPointList") List<CartPoint> cartPointList);

    List<Map<String,Object>> getCartInfo(String cartNumber);

    Integer deleteCartScreen(@Param("cartNumber") String cartNumber, @Param("screenList") List<String> screenList, @Param("pointType") Integer pointType);

    List<CartPoint> searchCartPointList(Map<String, Object> param);

    Integer confirmOrder(Map<String,Object> param);

    Integer emptyTable(Map<String, Object> deleteParam);

    List<String> checkHasConfirmOrder(Map<String, Object> param);

    Integer copyCartPoint(String cartNumber);

    List<Map<String,Object>> searchAreaNew(Map<String,Object> param);

    List<Map<String,Object>> exportExcel(Map<String,Object> param);

    List<Map<String,String>> getOrderCartNumber();

    List<Map<String,String>> getScreenByCity(String cityName);

    int checkNumber(@Param("targetData") CartScreenInfoReq targetData, @Param("putData") CartScreenInfoReq putData,@Param("cartNumber") String cartNumber,@Param("pointType") Integer pointType,@Param("system") Integer system);

    int modifyCartPoint(@Param("targetData")CartScreenInfoReq targetData, @Param("putData")CartScreenInfoReq putData, @Param("cartNumber")String cartNumber,@Param("pointType") Integer pointType,@Param("system") Integer system);

    Map<String,Long> getDistrictByCartNumber(String cartNumber);

    List<Map<String,Object>> checkGiveWayCityParam(@Param("cartNumber") String cartNumber, @Param("screenArr") List<ScreenInfoReq> screenArr);

    List<String> checkGiveWayCity(@Param("cartNumber") String cartNumber, @Param("screenArr") List<ScreenInfoReq> screenArr);

    Integer addPointStatistics(@Param("pointStatisticsList")List<PointStatistics> pointStatisticsList);

    Integer deletePointStatistics(@Param("cartNumber") String cartNumber);

    List<PointStatistics> searchPointStatistics(@Param("cartNumber") String cartNumber,@Param("pointType") Integer pointType);

    Integer updateCartPointState(Map<String,Object> param);

    Integer deleteCartPoint(Map<String, Object> param);

    List<Map<String,String>> searchQuality();

    List<Map<String,Object>> getStatisticsByCartNumber(String cartNumber);

    Integer updateCartPointStateByParam(Map<String, Object> param);

    Integer deleteAllPointType(Map<String, Object> param);

    List<PointStatistics> contract(Map<String, Object> param);

    List<Map<String,Object>> splitCart(Map<String,Object> param);

    List<Map<String,Object>> getCartInfoByParam(Map<String, Object> param);

    int deletePointStatisticsBySystem(Map<String, Object> deleteStatisticsParam);

    List<PointStatistics> searchPointStatisticsByParam(Map<String, Object> param);

    Integer cancel(String cartNumber);

    List<Map<String,Object>> getCartInfoByOrder(Map<String, Object> param);

    Integer updateCartPointStateByCartNumber(String cartNumber);

    Integer deleteInvalidPointCart(Map<String, Object> param);

    List<Map<String,String>> searchScreenState();

    List<Map<String,Object>> exportSupervisionExcel(Map<String, Object> param);
}
