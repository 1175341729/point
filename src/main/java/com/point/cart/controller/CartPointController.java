package com.point.cart.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.point.cart.common.enums.CommonEnum;
import com.point.cart.common.rsp.MessageRsp;
import com.point.cart.common.rsp.MessageUtil;
import com.point.cart.message.req.CartScreenInfoReq;
import com.point.cart.message.req.ModifyCartPointReq;
import com.point.cart.message.req.SearchScreenMediaInfoReq;
import com.point.cart.service.CartPointService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class CartPointController {
    private Logger logger = LoggerFactory.getLogger(CartPointController.class);
    @Resource
    private CartPointService pointCartService;

    /**
     * 省市区查询
     * 通用返回所有层级数据结构
     * @return MessageRsp
     */
    @GetMapping("/searchArea")
    public MessageRsp searchArea(@RequestParam(value = "areaType") String areaType,
                                 @RequestParam(value = "areaName",required = false)String areaName,
                                 @RequestParam(value = "level",required = false) Integer level,
                                 @RequestParam(value = "cartNumber",required = false) String cartNumber){
        MessageRsp rsp = pointCartService.searchArea(areaType,areaName,level,cartNumber);
        return rsp;
    }

    /**
     * 商圈范围
     * @return MessageRsp
     */
    @GetMapping("/searchBusinessAreaList")
    public MessageRsp searchBusinessAreaList(@RequestParam(value = "province",required = false)String province,
                                             @RequestParam(value = "city",required = false)String city,
                                             @RequestParam(value = "area",required = false) String area){
        MessageRsp rsp = pointCartService.searchBusinessAreaList(province,city,area);
        return rsp;
    }

    /**
     * 商家类型
     * @return MessageRsp
     */
    @GetMapping("/searchSellerTypeList")
    public MessageRsp searchSellerTypeList(){
        MessageRsp rsp = pointCartService.searchSellerTypeList();
        return rsp;
    }

    /**
     * 行业类型
     * @return MessageRsp
     */
    @GetMapping("/searchTradeTypeList")
    public MessageRsp searchTradeTypeList(){
        MessageRsp rsp = pointCartService.searchTradeTypeList();
        return rsp;
    }

    /**
     * 搜索设备
     * @return MessageRsp
     */
    @GetMapping("/searchDev")
    public MessageRsp searchDev(){
        MessageRsp rsp = pointCartService.searchDev();
        return rsp;
    }

    /**
     * 刊位质量
     * @return 响应
     */
    @GetMapping("/searchQuality")
    public MessageRsp searchQuality(){
        MessageRsp rsp = pointCartService.searchQuality();
        return rsp;
    }

    /**
     * 查询可使用的屏信息
     * @return MessageRsp
     */
    @PostMapping("/searchScreenMediaInfo")
    public MessageRsp searchScreenMediaInfo(@RequestBody SearchScreenMediaInfoReq req){
        Long begin = System.currentTimeMillis();
        MessageRsp rsp = pointCartService.searchScreenMediaInfo(req);
        logger.info("查询用时：{}" ,System.currentTimeMillis() - begin);
        return rsp;
    }

    /**
     * 添加屏到购物车中
     * @param req 请求
     * @return MessageRsp
     */
    @PostMapping("/addPointCart")
    public MessageRsp addPointCart(@RequestBody SearchScreenMediaInfoReq req){
        req.setPointType(0);
        return pointCartService.addPointCart(req);
    }

    /**
     * 根据购物车编号按照市进行分组展示
     * 获取购物车详情
     * @return MessageRsp
     */
    @GetMapping("/{cartNumber}/getCartInfo")
    public MessageRsp getCartInfo(@PathVariable String cartNumber,
                                  @RequestParam(value = "scale",required = false) Float scale,
                                  @RequestParam(value = "way",required = false) Integer way,
                                  @RequestParam(value = "system",required = false) Integer system){
        Map<String,Object> giveAwayCondition = new HashMap<>();
        giveAwayCondition.put("scale",scale);
        giveAwayCondition.put("way",way);
        giveAwayCondition.put("system",system);
        return pointCartService.getCartInfo(cartNumber,giveAwayCondition);
    }

    /**
     * 根据购物车编号按照市进行列表展示(前端自己处理分组)
     * 获取购物车详情
     * @return MessageRsp
     */
    @GetMapping("/{cartNumber}/getCartInfoList")
    public MessageRsp getCartInfoList(@PathVariable String cartNumber,
                                  @RequestParam(value = "scale",required = false) Float scale,
                                  @RequestParam(value = "way",required = false) Integer way,
                                  @RequestParam(value = "system",required = false) Integer system){
        Map<String,Object> giveAwayCondition = new HashMap<>();
        giveAwayCondition.put("scale",scale);
        giveAwayCondition.put("way",way);
        giveAwayCondition.put("system",system);
        return pointCartService.getCartInfoList(cartNumber,giveAwayCondition);
    }

    /**
     * 播控系统 根据购物车编号按照市进行列表展示(前端自己处理分组)
     * 获取购物车详情
     * @return MessageRsp
     */
    @GetMapping("/play/{cartNumber}/getCartInfoList")
    public MessageRsp playCartInfoList(@PathVariable String cartNumber,
                                      @RequestParam(value = "scale",required = false) Float scale,
                                      @RequestParam(value = "way",required = false) Integer way,
                                      @RequestParam(value = "system",required = false) Integer system,
                                      @RequestParam(value = "number",required = false) Integer number,
                                      @RequestParam(value = "timeLength",required = false) Integer timeLength,
                                      @RequestParam(value = "startTime",required = false) Long startTime,
                                      @RequestParam(value = "endTime",required = false) Long endTime){
        Map<String,Object> param = new HashMap<>();
        param.put("scale",scale);
        param.put("way",way);
        param.put("system",system);
        if (number == null) return MessageUtil.error("刊位个数不能为空！");
        param.put("number",number);
        if (timeLength == null) return MessageUtil.error("刊位时长不能为空！");
        param.put("timeLength",timeLength);
        if (startTime == null) return MessageUtil.error("投放开始时间不能为空！");
        param.put("startTime",startTime);
        if (endTime == null) return  MessageUtil.error("投放结束时间不能为空！");
        param.put("endTime",endTime);
        return pointCartService.playCartInfoList(cartNumber,param);
    }

    /**
     * 销控 订单中查看购物车详情
     * 获取购物车详情
     * @return MessageRsp
     */
    @GetMapping("/{cartNumber}")
    public MessageRsp getCartInfoDetail(@PathVariable String cartNumber,
                                      @RequestParam(value = "scale",required = false) Float scale,
                                      @RequestParam(value = "way",required = false) Integer way,
                                      @RequestParam(value = "system",required = false) Integer system){
        Map<String,Object> giveAwayCondition = new HashMap<>();
        giveAwayCondition.put("scale",scale);
        giveAwayCondition.put("way",way);
        giveAwayCondition.put("system",system);
        return pointCartService.getCartInfoDetail(cartNumber,giveAwayCondition);
    }

    /**
     * 播控 订单中查看购物车详情
     * 获取购物车详情
     * @return MessageRsp
     */
    @GetMapping("/play/{cartNumber}")
    public MessageRsp getPlayCartInfoDetail(@PathVariable String cartNumber,
                                            @RequestParam(value = "number",required = false) Integer number,
                                            @RequestParam(value = "timeLength",required = false) Integer timeLength,
                                            @RequestParam(value = "startTime",required = false) Long startTime,
                                            @RequestParam(value = "endTime",required = false) Long endTime){
        Map<String,Object> param = new HashMap<>();
        if (number == null) return MessageUtil.error("刊位个数不能为空！");
        param.put("number",number);
        if (timeLength == null) return MessageUtil.error("刊位时长不能为空！");
        param.put("timeLength",timeLength);
        if (startTime == null) return MessageUtil.error("投放开始时间不能为空！");
        param.put("startTime",startTime);
        if (endTime == null) return  MessageUtil.error("投放结束时间不能为空！");
        param.put("endTime",endTime);
        return pointCartService.getPlayCartInfoDetail(cartNumber,param);
    }

    /**
     * 根据城市删除购物车
     * @return MessageRsp
     */
    @DeleteMapping("/{cartNumber}/{cityName}/deleteByCity")
    public MessageRsp deleteByCity(CartScreenInfoReq req){
        MessageRsp rsp = pointCartService.deleteByCity(req);
        return rsp;
    }

    /**
     * 播控系统根据城市删除购物车
     * @return MessageRsp
     */
    @DeleteMapping("/play/{cartNumber}/{cityName}/deleteByCity")
    public MessageRsp playDeleteByCity(CartScreenInfoReq req){
        req.setPointType(CommonEnum.PointType.CHOOSE.getPointType());
        MessageRsp rsp = pointCartService.playDeleteByCity(req);
        return rsp;
    }

    /**
     * 批量选择进行选择删除购物车中选中的屏信息
     * @return MessageRsp
     */
    @PostMapping("/deleteBySelectScreen")
    public MessageRsp deleteBySelectScreen(@RequestBody CartScreenInfoReq req){
        MessageRsp rsp = pointCartService.deleteBySelectScreen(req);
        return rsp;
    }

    /**
     * 播控系统批量选择进行选择删除购物车中选中的屏信息
     * @return MessageRsp
     */
    @PostMapping("/play/deleteBySelectScreen")
    public MessageRsp playDeleteBySelectScreen(@RequestBody CartScreenInfoReq req){
        req.setPointType(CommonEnum.PointType.CHOOSE.getPointType());
        MessageRsp rsp = pointCartService.playDeleteBySelectScreen(req);
        return rsp;
    }

    /**
     * 批量删除全部赠送点位
     * @return MessageRsp 响应
     */
    @DeleteMapping("/{cartNumber}")
    public MessageRsp deleteAllPointType(@PathVariable String cartNumber){
        MessageRsp rsp = pointCartService.deleteAllPointType(cartNumber);
        return rsp;
    }

    /**
     * 播控系统 批量删除全部赠送点位
     * 播控系统中有可能操作销控点位 需要单独走接口
     * @return MessageRsp 响应
     */
    @DeleteMapping("/play/{cartNumber}")
    public MessageRsp playDeleteAllPointType(@PathVariable String cartNumber){
        MessageRsp rsp = pointCartService.playDeleteAllPointType(cartNumber);
        return rsp;
    }

    /**
     * 城市下屏详情
     * @return MessageRsp
     */
    @PostMapping("/{cartNumber}/{cityName}/cartScreenInfo")
    public MessageRsp cartScreenInfo(@RequestBody SearchScreenMediaInfoReq req
            ,@PathVariable String cartNumber
            ,@PathVariable String cityName){
        req.setCartNumber(cartNumber);
        req.setCityName(cityName);
        req.setSystem(CommonEnum.SystemType.SALE.getSystemType()); // 销控系统
        MessageRsp rsp = pointCartService.cartScreenInfo(req);
        return rsp;
    }

    /**
     * 播控系统 城市下屏详情
     * 播控系统可能会看到销控系统的点位详情，存在赠送条件一致，但是是不同系统
     * system 参数会在点位详情中返回
     * @return MessageRsp
     */
    @PostMapping("/play/{cartNumber}/{cityName}/{system}/cartScreenInfo")
    public MessageRsp playCartScreenInfo(@RequestBody SearchScreenMediaInfoReq req
            ,@PathVariable String cartNumber
            ,@PathVariable String cityName
            ,@PathVariable Integer system){
        req.setCartNumber(cartNumber);
        req.setCityName(cityName);
        req.setSystem(system);
        MessageRsp rsp = pointCartService.cartScreenInfo(req);
        return rsp;
    }

    /**
     * 确认订单 将其发布到正式订单中
     * 需要排除存在已经存在订单中的数据
     * @return MessageRsp
     */
    @PutMapping("/{cartNumber}/confirmOrder")
    public MessageRsp confirmOrder(@PathVariable("cartNumber") String cartNumber,@RequestBody JSONObject req){
        MessageRsp rsp = pointCartService.confirmOrder(cartNumber, req);
        return rsp;
    }

    /**
     * 确认订单 将其发布到正式订单中
     * 需要排除存在已经存在订单中的数据
     * @return MessageRsp
     */
    @PutMapping("/play/{cartNumber}/confirmOrder")
    public MessageRsp playConfirmOrder(@PathVariable("cartNumber") String cartNumber,@RequestBody CartScreenInfoReq req){
        MessageRsp rsp = pointCartService.playConfirmOrder(cartNumber, req);
        return rsp;
    }

    /**
     * 编辑订单中的购物车(废弃)
     * 思路：将订单中的数据 拷贝到购物车中 后面的操作全部操作购物车 直到确认订单之后才会从新写到订单中
     * @return MessageRsp
     */
    @PutMapping("/{cartNumber}/compilePointCart")
    public MessageRsp compilePointCart(@PathVariable(value = "cartNumber") String cartNumber){
        MessageRsp rsp = pointCartService.compilePointCart(cartNumber);
        return rsp;
    }

    /**
     * 导出excel
     * @param cartNumber 购物车编号
     * @return MessageRsp
     */
    @GetMapping("/{cartNumber}/exportCartPoint")
    public MessageRsp exportCartPoint(@PathVariable(value = "cartNumber") String cartNumber){
        MessageRsp rsp;
        try {
            rsp = pointCartService.exportCartPoint(cartNumber);
        } catch (Exception e) {
            return MessageUtil.error("导出excel表格异常！");
        }
        return rsp;
    }

    /**
     * 播控系统导出excel
     * @param cartNumber 购物车编号
     * @return MessageRsp
     */
    @GetMapping("/play/{cartNumber}/exportCartPoint")
    public MessageRsp playExportCartPoint(@PathVariable(value = "cartNumber") String cartNumber,CartScreenInfoReq req){
        MessageRsp rsp;
        try {
            rsp = pointCartService.playExportCartPoint(cartNumber,req);
        } catch (Exception e) {
            return MessageUtil.error("导出excel表格异常！");
        }
        return rsp;
    }

    /**
     * 播控系统检播报告导出excel
     * @param cartNumber 购物车编号
     * @return MessageRsp
     */
    @GetMapping("/play/supervision/{cartNumber}")
    public MessageRsp playSupervisionExportCartPoint(@PathVariable(value = "cartNumber") String cartNumber,CartScreenInfoReq req){
        MessageRsp rsp;
        try {
            rsp = pointCartService.playSupervisionExportCartPoint(cartNumber,req);
        } catch (Exception e) {
            return MessageUtil.error("导出excel表格异常！");
        }
        return rsp;
    }

    /**
     * 返回所有订单中购物车编号(临时)
     * @return MessageRsp
     */
    @GetMapping("/getOrderCartNumber")
    public MessageRsp getOrderCartNumber(){
        MessageRsp rsp = pointCartService.getOrderCartNumber();
        return rsp;
    }

    /**
     * 获取参数
     * @param cartNumber 购物车编号
     * @return MessageRsp
     */
    @GetMapping("/{cartNumber}/getCartPointParam")
    public MessageRsp getCartPointParam(@PathVariable("cartNumber") String cartNumber){
        MessageRsp rsp = pointCartService.getCartPointParam(cartNumber);
        return rsp;
    }

    /**
     * 导入选择点位
     * @return MessageRsp
     */
    @PostMapping("/importCartPoint")
    public MessageRsp importCartPoint(HttpServletRequest request,SearchScreenMediaInfoReq req){
        MessageRsp rsp;
        try {
            MultipartHttpServletRequest mhsq = (MultipartHttpServletRequest) request;
            List<MultipartFile> importFiles = mhsq.getFiles("importFile");
            MultipartFile multipartFile = null;
            boolean flag = false;
            if (importFiles != null && importFiles.size() > 0){
                multipartFile = importFiles.get(0);
                if (!multipartFile.isEmpty()){
                    flag = true;
                }
            }
            if (flag){
                req.setPointType(0);
                rsp = pointCartService.importCartPoint(multipartFile, req);
            } else {
                rsp = MessageUtil.error("导入点位发生错误！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            rsp = MessageUtil.error("导入点位发生错误！");
        }

        return rsp;
    }

    /**
     * 导入赠送点位
     * @return MessageRsp
     */
    @PostMapping("/importGiveAwayCartPoint")
    public MessageRsp importGiveAwayCartPoint(HttpServletRequest request,SearchScreenMediaInfoReq req){
        MessageRsp rsp;
        try {
            MultipartHttpServletRequest mhsq = (MultipartHttpServletRequest) request;
            List<MultipartFile> importFiles = mhsq.getFiles("importFile");
            MultipartFile multipartFile = null;
            boolean flag = false;
            if (importFiles != null && importFiles.size() > 0){
                multipartFile = importFiles.get(0);
                if (!multipartFile.isEmpty()){
                    flag = true;
                }
            }
            if (flag){
                req.setPointType(1);
                rsp = pointCartService.importGiveAwayCartPoint(multipartFile, req);
            } else {
                rsp = MessageUtil.error("导入赠送点位发生错误！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            rsp = MessageUtil.error("导入赠送点位发生错误！");
        }

        return rsp;
    }

    /**
     * 修改选择点位的购物车基本信息
     * @return MessageRsp
     */
    @PutMapping("/{cartNumber}/{cityName}/modifyCartPoint")
    public MessageRsp modifyCartPoint(@RequestBody ModifyCartPointReq req
            ,@PathVariable(value = "cartNumber",required = false) String cartNumber
            ,@PathVariable(value = "cityName",required = false) String cityName){
        req.setCartNumber(cartNumber);
        req.setCityName(cityName);
        Integer pointType = CommonEnum.PointType.CHOOSE.getPointType();
        req.setPointType(pointType);
        return pointCartService.modifyCartPoint(req);
    }

    /**
     * 修改赠送点位购物车基本信息
     * @return MessageRsp
     */
    @PutMapping("/{cartNumber}/{cityName}/modifyGiveWayCartPoint")
    public MessageRsp modifyGiveWayCartPoint(@RequestBody ModifyCartPointReq req
            ,@PathVariable(value = "cartNumber",required = false) String cartNumber
            ,@PathVariable(value = "cityName",required = false) String cityName){
        req.setCartNumber(cartNumber);
        req.setCityName(cityName);
        return pointCartService.modifyGiveWayCartPoint(req);
    }

    /**
     * 赠送点位条件
     * @return 响应
     */
    @PostMapping("/{cartNumber}/giveAwayPointCondition")
    public MessageRsp giveAwayPointCondition(@PathVariable(value = "cartNumber",required = false) String cartNumber, @RequestBody Map<String,String> giveAwayCondition){
        MessageRsp rsp = pointCartService.giveAwayPointCondition(cartNumber, giveAwayCondition);
        return rsp;
    }

    /**
     * 添加赠送点位(销控下会判断条件)
     * @return
     */
    @PostMapping("/addGiveAwayPoint")
    public MessageRsp addGiveAwayPoint(@RequestBody SearchScreenMediaInfoReq req){
        req.setPointType(1);
        MessageRsp rsp = pointCartService.addGiveAwayPoint(req);
        return rsp;
    }

    /**
     * 确认定位 验证是否合法
     * param req
     * @return 响应
     */
    @PostMapping("/{cartNumber}/confirmCartPoint")
    public MessageRsp confirmPoint(@PathVariable(value = "cartNumber")String cartNumber,@RequestBody JSONObject req){
        MessageRsp rsp = pointCartService.confirmPoint(cartNumber,req);
        return rsp;
    }

    /**
     * 播控系统确认定位 验证是否合法
     * param req
     * @return 响应
     */
    @PutMapping("/play/{cartNumber}/confirmCartPoint")
    public MessageRsp playConfirmPoint(@PathVariable(value = "cartNumber")String cartNumber,@RequestBody CartScreenInfoReq req){
        MessageRsp rsp = pointCartService.playConfirmPoint(cartNumber,req);
        return rsp;
    }

    /**
     * 销控系统 获取购物车下统计
     * @return 响应
     */
    @GetMapping("/{cartNumber}/getStatisticsByCartNumber")
    public MessageRsp getStatisticsByCartNumber(@PathVariable String cartNumber){
        MessageRsp rsp = pointCartService.getStatisticsByCartNumber(cartNumber);
        return rsp;
    }

    /**
     * 获取购物车投放时间
     * @param cartNumber 购物车编号
     * @return 响应
     */
    @GetMapping("/{cartNumber}/date")
    public MessageRsp getPutDate(@PathVariable("cartNumber") String cartNumber){
        MessageRsp putDate = pointCartService.getPutDate(cartNumber);
        return putDate;
    }

    /**
     * 合同附件中点位详情
     * @param cartNumber 购物车编号
     * @return 响应
     */
    @GetMapping("/statistics/{cartNumber}")
    public MessageRsp getStatistics(@PathVariable String cartNumber){
        MessageRsp rsp = pointCartService.getStatistics(cartNumber);
        return rsp;
    }

    /**
     * 合同附件中统计
     * @param cartNumber 购物车编号
     * @return 响应
     */
    @GetMapping("/contract/{cartNumber}")
    public MessageRsp contract(@PathVariable String cartNumber){
        MessageRsp rsp = pointCartService.contract(cartNumber);
        return rsp;
    }

    /**
     * 拆分订单中购物车
     * @param cartNumber 购物车编号
     * @return 响应
     */
    @GetMapping("/split/{cartNumber}")
    public MessageRsp split(@PathVariable String cartNumber){
        MessageRsp rsp = pointCartService.splitCart(cartNumber);
        return rsp;
    }

    /**
     * 播控系统获取点位信息
     * @param param 参数信息
     * 解决fegin调用无法直接接收对象的方式  改为用map接受
     * @return 响应
     */
    @GetMapping("/play/split/{cartNumber}")
    public MessageRsp playSplit(@PathVariable("cartNumber") String cartNumber,@RequestParam Map param){
        String reqParam = JSON.toJSONString(param);
        CartScreenInfoReq req = JSON.parseObject(reqParam, CartScreenInfoReq.class);
        req.setCartNumber(cartNumber);
        MessageRsp rsp = pointCartService.playSplitCart(req);
        return rsp;
    }

    /**
     * 取消订单(全部)
     * @param cartNumber 购物车编号
     * @return 响应
     */
    @PutMapping("/cancel/{cartNumber}")
    public MessageRsp cancel(@PathVariable String cartNumber){
        MessageRsp rsp = pointCartService.cancel(cartNumber);
        return rsp;
    }

    /**
     * 定时删除购物车无效数据
     * @param system 所属系统
     * @return 响应
     */
    @PutMapping("/{system}")
    public MessageRsp deleteInvalidPointCart(@PathVariable Integer system, @RequestBody List<String> cartNumberList){
        MessageRsp rsp = pointCartService.deleteInvalidPointCart(system,cartNumberList);
        return rsp;
    }
}