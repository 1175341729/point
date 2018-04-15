package com.point.cart.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.point.cart.common.constant.CommonConstant;
import com.point.cart.common.enums.CommonEnum;
import com.point.cart.common.rsp.MessageRsp;
import com.point.cart.common.rsp.MessageUtil;
import com.point.cart.common.utils.AreaUtil;
import com.point.cart.common.utils.BeanCopyUtil;
import com.point.cart.common.utils.OssUtil;
import com.point.cart.common.utils.RedisUtil;
import com.point.cart.exception.GlobalException;
import com.point.cart.feign.SaleSetClient;
import com.point.cart.mapper.CartPointExMapper;
import com.point.cart.mapper.CartPointMapper;
import com.point.cart.message.req.CartScreenInfoReq;
import com.point.cart.message.req.ModifyCartPointReq;
import com.point.cart.message.req.ScreenInfoReq;
import com.point.cart.message.req.SearchScreenMediaInfoReq;
import com.point.cart.message.rsp.*;
import com.point.cart.model.CartPoint;
import com.point.cart.model.PointStatistics;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.Clock;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CartPointServiceImpl implements CartPointService {
    private Logger logger = LoggerFactory.getLogger(CartPointServiceImpl.class);
    @Resource
    private JdbcTemplate jdbcTemplate;
    @Resource
    private CartPointMapper cartPointMapper;
    @Resource
    private CartPointExMapper cartPointExMapper;
    @Resource
    private RedisUtil redisUtil;
    @Resource
    private SaleSetClient saleSetClient;
    // 从配置文件中获取总数
    @Value("${totalTime}")
    private Integer totalTime;
    @Value("${fileSize}")
    private Integer fileSize;

    /**
     * 地区查询
     *
     * @param areaType 类型
     * @param areaName 名称
     * @param level    层级
     * @return 响应
     */
    @Override
    public MessageRsp searchArea(String areaType, String areaName, Integer level, String cartNumber) {
        if ("province".equals(areaType) && level != null && level > 3) {
            return MessageUtil.error("查询省时层级不能大于3");
        } else if ("city".equals(areaType) && level != null && level > 2) {
            return MessageUtil.error("查询市时层级不能大于2");
        } else if ("area".equals(areaType) && level != null && level > 1) {
            return MessageUtil.error("查询区县时层级不能大于1");
        }

        if ("city".equals(areaType) && level != null) {
            level += 1;
        }
        if ("area".equals(areaType) && level != null) {
            level += 2;
        }
        if (level == null) {
            level = 3;
        }

        Map<String, Object> param = new HashMap<>();
        if (StringUtils.isNotBlank(areaType)) param.put("areaType", areaType);
        if (StringUtils.isNotBlank(areaName)) param.put("areaName", areaName);
        if (StringUtils.isNotBlank(cartNumber)) param.put("cartNumber", cartNumber);
        param.put("level", level);
        Map<String, Map<String, List<String>>> provinceMap = new HashMap<>();
        List<Map<String, Object>> areaMapList = cartPointExMapper.searchAreaNew(param);
        areaMapList.forEach(map -> {
            String province = String.valueOf(map.get("province"));
            String city = map.get("city") == null ? null : String.valueOf(map.get("city"));
            String area = map.get("area") == null ? null : String.valueOf(map.get("area"));
            Map<String, List<String>> cityMap = provinceMap.get(province);
            if (cityMap == null) {
                cityMap = new HashMap<>();
            }

            List<String> areaList = cityMap.get(city);
            if (areaList == null) {
                areaList = new ArrayList<>();
            }
            if (StringUtils.isNotBlank(area)) {
                areaList.add(area);
            }
            if (StringUtils.isNotBlank(city)) {
                cityMap.put(city, areaList);
            }
            provinceMap.put(province, cityMap);
        });
        List<Map<String, Object>> resultData = AreaUtil.createResultData(provinceMap);
        return MessageUtil.success(resultData);
    }

    /**
     * 商家类型
     */
    @Override
    public MessageRsp searchSellerTypeList() {
        List<Map<String, Object>> sellerTypeList = cartPointExMapper.searchSellerTypeList();
        return MessageUtil.success(sellerTypeList);
    }

    /**
     * 行业类型
     */
    @Override
    public MessageRsp searchTradeTypeList() {
        List<Map<String, Object>> tradeTypeList = cartPointExMapper.searchTradeTypeList();
        return MessageUtil.success(tradeTypeList);
    }

    /**
     * 设备类型和安装位置
     */
    @Override
    public MessageRsp searchDev() {
        Map<String, List<String>> result = new HashMap<>();
        // 查询数据
        List<Map<String, Object>> devList = cartPointExMapper.searchDev();
        if (devList != null && devList.size() > 0) {
            for (int i = 0, length = devList.size(); i < length; i++) {
                Map<String, Object> map = devList.get(i);
                String devType = map.get("dev_type") + "";
                String screenLocation = map.get("screen_name") + "";

                List<String> list = result.get(devType);
                if (list == null) {
                    list = new ArrayList<>();
                }

                list.add(screenLocation);
                result.put(devType, list);
            }
        }

        List<Map<String, Object>> resultList = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("devType", "全部");
        map.put("screenLocation", new ArrayList<String>() {
            {
                add("全部");
            }
        });
        resultList.add(map);

        Set<Map.Entry<String, List<String>>> entries = result.entrySet();
        for (Map.Entry<String, List<String>> entry : entries) {
            map = new HashMap<>();
            String key = entry.getKey();
            List<String> value = entry.getValue();
            value.add(0, "全部");

            map.put("devType", key);
            map.put("screenLocation", value);
            resultList.add(map);
        }
        return MessageUtil.success(resultList);
    }

    /**
     * 商圈
     */
    @Override
    public MessageRsp searchBusinessAreaList(String province, String city, String area) {
        Map<String, Object> param = new HashMap<>();
        boolean flag = AreaUtil.checkArea(province, city, area);
        if (!flag) {
            return MessageUtil.error("投放区域参数错误");
        }

        param.put("province", province);
        param.put("city", city);
        param.put("area", area);
        List<Map<String, Object>> businessList = cartPointExMapper.searchBusinessAreaList(param);
        return MessageUtil.success(businessList);
    }

    /**
     *  获取刊位质量
     * @return 响应
     */
    @Override
    public MessageRsp searchQuality() {
        List<Map<String, String>> quality = cartPointExMapper.searchQuality();
        return MessageUtil.success(quality);
    }

    /**
     * 搜索可用屏信息
     */
    @Override
    public MessageRsp searchScreenMediaInfo(SearchScreenMediaInfoReq req) {
        // 验证参数正确
        String errorMsg = checkSearchParam(req, "search");
        if (StringUtils.isNotBlank(errorMsg)) {
            return MessageUtil.error(errorMsg);
        }
        if (req.getPointType() == 1 && StringUtils.isBlank(req.getCartNumber())) return MessageUtil.error("非法操作！");
        // 从配置文件中读取 logger.info("可售时长：{}",totalTime);
        req.setTotalTime(totalTime);
        Map<String, Object> resultMap = new HashMap<>();

        /*// 如果是播控 同时该购物车是从销控过来的
        Integer system = req.getSystem();
        if (system == CommonEnum.SystemType.PLAY.getSystemType()){
            boolean fromSale = this.checkFromSaleSystem(req.getCartNumber());
            req.setFromSale(fromSale);
        }*/

        List<ScreenMediaInfoRsp> screenList = cartPointExMapper.searchScreenMediaInfo(req);
        if (screenList != null) {
            logger.info("共计{}条数据", screenList.size());

            /** update dengwei 2017/11/24 新版取消参数验证
             String cartNumber = req.getCartNumber();
             if (StringUtils.isNotBlank(cartNumber)){
             this.createParamCache(cartNumber);
             }*/

            resultMap.put("total", screenList.size());
            resultMap.put("screenList", screenList);
        }
        return MessageUtil.success(resultMap);
    }

    /**
     * 参数缓存(废弃)
     */
    private void createParamCache(String cartNumber) {
        List<CartPoint> cartPointList = this.searchCartPointList(cartNumber, null, null, null, null, null, null, null);
        if (cartPointList != null && cartPointList.size() > 0) {
            CartPoint cartPoint = cartPointList.get(0);
            redisUtil.set(new StringBuilder(CommonConstant.SEARCH_SCREEN_PARAM).append(cartNumber).toString(), JSON.toJSONString(cartPoint), null);
        } else {
            redisUtil.delete(new StringBuilder(CommonConstant.SEARCH_SCREEN_PARAM).append(cartNumber).toString());
        }
    }

    /**
     * 验证条件
     */
    private String checkSearchParam(SearchScreenMediaInfoReq req, String type) {
        String errorMsg = "";
        List<String> errorMsgList = new ArrayList<>();
        Integer selectMode = req.getSelectMode();
        Integer playMode = req.getPlayMode();
        if (selectMode == null) {
            errorMsgList.add("请选择选点模式！");
        } else if (selectMode == 1) {
            if (playMode == null) errorMsgList.add("超级模式下请选择播放方式！");
        }

        Long startTime = req.getStartTime();
        Long endTime = req.getEndTime();
        if (startTime == null || endTime == null || endTime < startTime) errorMsgList.add("投放时间不能为空，或者结束时间不能大于开始时间！");

        Integer timeLength = req.getTimeLength();
        if (timeLength == null) errorMsgList.add("请选择投放时长！");

        Integer pointType = req.getPointType();
        if (pointType == null) errorMsgList.add("点位类型不能为空！");

        Integer orderType = req.getOrderType();
        if (orderType == null) errorMsgList.add("请选择订单类型！");
        if (orderType != null && orderType == CommonEnum.OrderType.TRY.getOrderType() && startTime != null && endTime != null){
            // 如果是试投订单有效期只有7天
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(startTime * 1000L);
            calendar.add(Calendar.DAY_OF_MONTH,7);
            Long tryEndTime = calendar.getTimeInMillis() / 1000L;
            if (endTime - tryEndTime > 0) errorMsgList.add("试投订单周期最多不超过7天！");
        }

        Integer number = req.getNumber();
        if (number == null) {
            errorMsgList.add("请输入刊位数量！");
        } /*else if (number > 1) {
            Integer purpose = req.getPurpose();
            if (purpose == null) errorMsgList.add("刊位数量大于1，请选择刊位用途！");
        }*/

        if ("search".equals(type)) {
            /*String devType = req.getDevType();
            if (StringUtils.isBlank(devType)){
                errorMsgList.add("请选择设备类型！");
            }
            String screenLocation = req.getScreenLocation();
            if (StringUtils.isBlank(screenLocation)){
                errorMsgList.add("请选择屏幕位置！");
            }*/

            // 人均消费
            Float startConsume = req.getStartConsume();
            Float endConsume = req.getEndConsume();
            if (startConsume != null && endConsume != null && startConsume - endConsume > 0) errorMsgList.add("开始人均消费需小于结束人均消费！");
            // 可流量
            Float startCustomerFlow = req.getStartCustomerFlow();
            Float endCustomerFlow = req.getEndCustomerFlow();
            if (startCustomerFlow != null && endCustomerFlow != null && startCustomerFlow - endCustomerFlow > 0) errorMsgList.add("开始日客流浪需要小于结束日客流浪！");


            // 投放区域
            String area = req.getArea();// 区
            String city = req.getCity();// 市
            String province = req.getProvince();//省
            boolean flag = AreaUtil.checkArea(province, city, area);
            if (!flag) {
                errorMsgList.add("投放区域异常");
            }
        } else if ("add".equals(type)) {
            Integer userId = req.getUserId();
            if (userId == null) errorMsgList.add("用户id不能为空！");

            Integer system = req.getSystem();
            if (system == null) errorMsgList.add("所属系统不能为空！");

            List<ScreenInfoReq> screenArr = req.getScreenArr();
            if (screenArr != null) {
                boolean flag = true;
                for (int i = 0, length = screenArr.size(); i < length; i++) {
                    ScreenInfoReq screenInfoReq = screenArr.get(i);
                    if (StringUtils.isBlank(screenInfoReq.getCityName()) || StringUtils.isBlank(screenInfoReq.getCityCode()) || StringUtils.isBlank(screenInfoReq.getScreenNumber())) {
                        flag = false;
                        break;
                    }
                }
                if (!flag) errorMsgList.add("选择屏信息传递参数有误！");
            }
        }
        if (errorMsgList.size() > 0) {
            errorMsg = StringUtils.join(errorMsgList, ",");
        }

        return errorMsg;
    }

    /**
     * 添加屏信息到购物车
     * 添加时候已经排除了不能满足条件的点位数据
     */
    @Override
    @Transactional
    public MessageRsp addPointCart(SearchScreenMediaInfoReq req) {
        // 验证参数正确
        String errorMsg = checkSearchParam(req, "add");
        if (StringUtils.isNotBlank(errorMsg)) {
            return MessageUtil.error(errorMsg);
        }
        if (req.getPointType() == 1 && StringUtils.isBlank(req.getCartNumber())) return MessageUtil.error("非法操作！");

        List<ScreenInfoReq> screenArr = req.getScreenArr();
        Integer randomNumber = req.getRandomNumber();
        if (randomNumber != null && randomNumber > 0) { // 表示随机选点
            // 之前通过缓存选择有问题
            req.setTotalTime(totalTime);
            List<ScreenMediaInfoRsp> screenList = cartPointExMapper.searchScreenMediaInfo(req);
            if (screenList != null && screenList.size() > 0) {
                if (randomNumber > screenList.size()) {
                    return MessageUtil.error("随机选择点位数量不能大于已有的设备！");
                }
            }
            screenArr = this.getScreenByRandom(randomNumber, screenList);
            if(screenArr.size() == 0) return MessageUtil.error("随机选点无数据！");
            req.setScreenArr(screenArr);
        } else if (screenArr == null || screenArr.size() == 0) {
            return MessageUtil.error("请选择屏或者随机选择点位！");
        }

        // 需要添加到购物车数据集合
        List<CartPoint> cartPointList = new ArrayList<>();
        CartPoint cartPoint;
        // 如果购物车编号存在则使用当前编号
        List<String> existScreenNumberList = null;
        String cartNumber = req.getCartNumber();
        Integer pointType = req.getPointType(); // 点位类型
        Integer system = req.getSystem();// 系统
        Integer playMode = req.getPlayMode();// 播放模式
        if (StringUtils.isBlank(cartNumber)) {
            cartNumber = UUID.randomUUID().toString().replaceAll("-", "");// 生成随机购物车编号
        } else {
            // 1、如果是销控,并且是赠送条件 需要验证是城市是否一致 在选中的屏中排除选中点位城市下的屏信息
            if (pointType == CommonEnum.PointType.GIVE_AWAY.getPointType() && system == CommonEnum.SystemType.SALE.getSystemType()) {
                List<Map<String, Object>> list = cartPointExMapper.checkGiveWayCityParam(cartNumber, screenArr);
                if (list != null && list.size() > 0) {
                    return MessageUtil.error("不能赠送选中城市之外的点位！");
                }
            }

            // 2、如果是播控系统，需要验证必要参数是否一致
            if (system == CommonEnum.SystemType.PLAY.getSystemType()){
                boolean fromSale = this.checkFromSaleSystem(cartNumber);
                boolean checkAddParam = this.checkPlayAddParam(req,fromSale);
                if (!checkAddParam) return MessageUtil.error("参数不一致！");
            }

            existScreenNumberList = this.getCartScreenNumberList(cartNumber, null, req.getPointType(), req.getCityName(), req.getNumber(), req.getTimeLength(), req.getStartTime(), req.getEndTime());
        }
        // 获取 新增数据 屏编号集合
        List<String> addScreenNumberList = screenArr.stream().map(ScreenInfoReq::getScreenNumber).collect(Collectors.toList());
        // 根据相关条件获取购物车数据库中id集合
        // List<Integer> cartPointIdList = this.getCartPointIds(cartNumber, null, req.getPointType(), req.getCityName(), req.getNumber(), req.getTimeLength(), req.getStartTime(), req.getEndTime());
        // 判断时间是否满足(播控 超级选点 插播不限制)
        List<String> refuseScreenNumberList = null;
        if (playMode == null || playMode != CommonEnum.PlayMode.INSERT.getPlayType()){
            refuseScreenNumberList = this.refuseScreenNumberList(addScreenNumberList, null, null, req.getStartTime(), req.getEndTime(), req.getNumber(), req.getTimeLength());
        }
        // 验证重复点位(同一购物车中存在时间一致，但是刊位时长不一致的情况，需要重复选择同一点位)
        String screenNumber;
        long createTime = System.currentTimeMillis() / 1000;
        int length = screenArr.size();
        // 试投订单验证参数
        Integer orderType = req.getOrderType();
        if (orderType == CommonEnum.OrderType.TRY.getOrderType()){
            String errorMessage = this.checkTryOrderNumber(length, cartNumber, req);
            if (StringUtils.isNotBlank(errorMessage)) return MessageUtil.error(errorMessage);
        }
        for (int i = 0; i < length; i++) {
            cartPoint = new CartPoint();
            ScreenInfoReq screenInfoReq = screenArr.get(i);
            // 验证是否重复添加
            screenNumber = screenInfoReq.getScreenNumber();
            if (existScreenNumberList != null && existScreenNumberList.contains(screenNumber))
                return MessageUtil.error("存在重复的点位，请重新选择！");
            // 验证是否满足时间限制
            if (refuseScreenNumberList != null && refuseScreenNumberList.contains(screenNumber))
                return MessageUtil.error("存在满足投放时间限制的点位！");
            // 属性拷贝
            BeanCopyUtil.copyBean(cartPoint, req, "timeId", "timeLength", "number", "purpose", "startTime", "endTime", "timeBucket", "selectMode", "playMode", "system", "pointType", "userId","orderType");
            cartPoint.setCityCode(screenInfoReq.getCityCode());
            cartPoint.setCityName(screenInfoReq.getCityName());
            cartPoint.setScreenNumber(screenNumber);
            cartPoint.setCartNumber(cartNumber);
            cartPoint.setState(0);
            cartPoint.setCreateTime(createTime);
            cartPoint.setTimeTotal(req.getTimeLength() * req.getNumber());// 记录总时间
            cartPointList.add(cartPoint);
        }
        // 保存数据到数据库
        this.batchAddPointCart(cartNumber, cartPointList);

        Map<String, String> result = new HashMap<>();
        result.put("cartNumber", cartNumber);
        return MessageUtil.success(result);
    }

    /**
     * 试投订单验证
     * 1、500个
     * 2、一个城市、同一时间段、同一刊位数、统一刊位时长
     * @param length 添加个数
     * @param cartNumber 购物车编号
     * @param req
     * @return 响应
     */
    private String checkTryOrderNumber(int length, String cartNumber, SearchScreenMediaInfoReq req) {
        List<CartPoint> cartPoints = this.searchCartPointList(cartNumber, null, null, null, null, null, null, null);
        List<ScreenInfoReq> screenArrReq = req.getScreenArr();
        List<String> cityNameList = screenArrReq.stream().map(ScreenInfoReq::getCityName).distinct().collect(Collectors.toList());

        // 查询数据库中的信息
        String timeCityParam = "";
        if (cartPoints != null && cartPoints.size() > 0){
            length += cartPoints.size();

            List<String> dbParam = cartPoints.stream()
                    .map(cartPoint -> cartPoint.getCityName() + "-" + cartPoint.getNumber() + "-" + cartPoint.getTimeLength() + "-" + cartPoint.getStartTime() + "-" + cartPoint.getEndTime())
                    .distinct()
                    .collect(Collectors.toList());
            if (dbParam != null){
                if (dbParam.size() > 1) return "数据有误！";
                else timeCityParam = dbParam.get(0);
            }
        }

        if (cityNameList != null) {
            if (cityNameList.size() > 1) return "试投订单所选城市只能存在一个！";
            if (cityNameList.size() == 1){
                String cityName = cityNameList.get(0);
                Integer number = req.getNumber();
                Integer timeLength = req.getTimeLength();
                Long startTime = req.getStartTime();
                Long endTime = req.getEndTime();
                String tryParamCheck = cityName + "-" + number + "-" + timeLength + "-" + startTime + "-" + endTime;
                if (StringUtils.isNotBlank(timeCityParam) && !tryParamCheck.equals(timeCityParam)) return "试投订单投放时间、时长、刊位数、城市必须一致！";
            }
        }
        if (length > 500) return "试投订单最多投放500个点位！";
        return null;
    }

    /**
     * 批量添加点位
     *
     * @param cartNumber    购物车编号
     * @param cartPointList 添加数据集合
     * @return 响应
     */
    private Integer batchAddPointCart(String cartNumber, List<CartPoint> cartPointList) {
        Integer addSuccess = 0;
        int addNumber = cartPointList.size();
        if (addNumber > 0) {
            // 因为pgsql一次插入数据量太大会导致数据库异常，通过程序分批次进行处理
            int page = (addNumber % CommonConstant.BATCH_NUMBER == 0) ? (addNumber / CommonConstant.BATCH_NUMBER) : (addNumber / CommonConstant.BATCH_NUMBER) + 1;
            for (int i = 1; i <= page; i++) {
                // 模拟分批次提交
                List<CartPoint> batchCartPointList = cartPointList.stream().skip((i - 1) * CommonConstant.BATCH_NUMBER).limit(CommonConstant.BATCH_NUMBER).collect(Collectors.toList());
                addSuccess = cartPointExMapper.addPointCart(batchCartPointList);
                logger.info("成功添加购物车{}屏到购物车{}个", cartNumber, addSuccess);
            }
            // 删除缓存
            redisUtil.batchDelete(new StringBuilder(CommonConstant.CART_NUMBER).append("-").append(cartNumber).toString());
        } else MessageUtil.error("添加购物车失败，无数据！");

        return addSuccess;
    }

    /**
     * 17-11-24 废弃方法
     * 验证添加的参数是否一致
     */
    private boolean checkAddParam(String cartNumber, SearchScreenMediaInfoReq req) {
        boolean flag = true;
        String param = (String) redisUtil.get(new StringBuilder(CommonConstant.SEARCH_SCREEN_PARAM).append(cartNumber).toString());
        if (StringUtils.isNotBlank(param)) {
            CartPoint searchParam = JSON.parseObject(param, CartPoint.class);
            Long startTime = searchParam.getStartTime();
            Long endTime = searchParam.getEndTime();
            Integer timeLength = searchParam.getTimeLength();
            Integer number = searchParam.getNumber();
            if (startTime.longValue() != req.getStartTime().longValue()
                    || endTime.longValue() != req.getEndTime().longValue()
                    || timeLength.intValue() != req.getTimeLength().intValue()
                    || number.intValue() != req.getNumber().intValue()) {
                flag = false;
            }
        }
        return flag;
    }

    /**
     * 播控系统需要验证添加参数是否一直
     * @param req 请求参数
     * @return 响应
     */
    private boolean checkPlayAddParam(SearchScreenMediaInfoReq req, boolean fromSale) {
        boolean flag = false;
        String paramStr = JSON.toJSONString(req);
        Map<String,Object> param = JSON.parseObject(paramStr, HashMap.class);
        param.put("system",null);
        param.put("pointType",null);
        param.put("cityName",null);
        List<CartPoint> list = cartPointExMapper.searchCartPointList(param);
        if (fromSale){
            if (list != null && list.size() > 0) flag = true;
        } else {
            List<CartPoint> cartPointList = this.searchCartPointList(req.getCartNumber(), null, null, null, null, null, null, null);
            if (cartPointList == null || cartPointList.size() == 0 || list.size() > 0) flag = true;
        }
        return flag;
    }

    /**
     * 随机选点生成选择随机的屏信息
     */
    private List<ScreenInfoReq> getScreenByRandom(Integer randomNumber, List<ScreenMediaInfoRsp> list) {
        ScreenInfoReq screen;
        List<ScreenInfoReq> screenList = new ArrayList<>();
        Map<String, Integer> check = new HashMap<>();// 区分是否重复
        int i = 1;
        if (list != null && list.size() > 0) {
            while (i <= randomNumber) {
                // 生成随机数
                Random random = new Random();
                int index = random.nextInt(list.size());
                ScreenMediaInfoRsp screenInfo = list.get(index);
                String screenNumber = screenInfo.getScreenNumber();
                String city = screenInfo.getCityName();
                String code = screenInfo.getCityCode();
                if (screenNumber == null) break;
                if (check.get(screenNumber) == null) {
                    screen = new ScreenInfoReq();
                    screen.setCityName(city);
                    screen.setCityCode(code);
                    screen.setScreenNumber(screenNumber);
                    screenList.add(screen);
                    // 验证是否重复
                    check.put(screenNumber, 1);
                    ++i;
                }
            }
        }
        return screenList;
    }

    /**
     * 获取购物车详情(废弃)
     */
    @Override
    @Transactional
    public MessageRsp getCartInfo(String cartNumber,Map<String,Object> giveAwayCondition) {
        if (StringUtils.isBlank(cartNumber)) return MessageUtil.error("购物车编号不能为空！");
        Map<String, Object> rsp = new HashMap<>();
        Integer totalPoint = 0;
        Float totalTime = 0.0F;
        Float totalMoney = 0.0F;
        boolean standards = true;
        List<Map<String, Object>> cartInfoList = cartPointExMapper.getCartInfo(cartNumber);
        if (cartInfoList != null && cartInfoList.size() > 0) {
            // 删除点位统计
            this.deletePointStatistics(cartNumber);
            // 选择点位
            List<Map<String, Object>> choosePointList = cartInfoList.stream().filter(map -> this.getPointType(map.get("pointtype")) == CommonEnum.PointType.CHOOSE.getPointType()).collect(Collectors.toList());
            if (choosePointList != null && choosePointList.size() > 0) {
                Map<String, Object> choosePoint = this.getCartInfoAndSaleTime(choosePointList, cartNumber, CommonEnum.PointType.CHOOSE);
                rsp.put("choosePoint", choosePoint);

                totalPoint += Integer.valueOf(choosePoint.get("totalPoint") + "");
                totalTime += Float.valueOf(choosePoint.get("totalTime") + "");
                totalMoney += Float.valueOf(choosePoint.get("totalMoney") + "");
                standards = Boolean.valueOf(choosePoint.get("standards") + "");
            }
            // 赠送点位
            List<Map<String, Object>> giveWayPointList = cartInfoList.stream()
                    .filter(map -> this.getPointType(map.get("pointtype")) == CommonEnum.PointType.GIVE_AWAY.getPointType())
                    .collect(Collectors.toList());
            if (giveWayPointList != null && giveWayPointList.size() > 0) {
                // 存放数据
                String scale = giveAwayCondition.get("scale") == null ? "" : giveAwayCondition.get("scale").toString();
                String way = giveAwayCondition.get("way") == null ? "" : giveAwayCondition.get("way").toString();
                StringBuilder sb = new StringBuilder();
                sb.append(CommonConstant.GIVE_AWAY).append("-").append(cartNumber);
                JSONObject condition = new JSONObject();
                condition.put("scale", scale);
                condition.put("way", way);
                redisUtil.set(sb.toString(), condition.toJSONString(), null);

                // 构造返回值
                Map<String, Object> giveWayPoint = this.getCartInfoAndSaleTime(giveWayPointList, cartNumber, CommonEnum.PointType.GIVE_AWAY);
                rsp.put("giveAwayPoint", giveWayPoint);

                totalPoint += Integer.valueOf(giveWayPoint.get("totalPoint") + "");
                totalTime += Float.valueOf(giveWayPoint.get("totalTime") + "");
                totalMoney += Float.valueOf(giveWayPoint.get("totalMoney") + "");
                standards = standards && Boolean.valueOf(giveWayPoint.get("standards") + "");
            }
        }
        // 赠送条件
        String condition = (String) redisUtil.get(new StringBuilder().append(CommonConstant.GIVE_AWAY).append("-").append(cartNumber).toString());
        if (StringUtils.isNotBlank(condition)) {
            rsp.put("giveWayCondition", JSON.parseObject(condition));
        }
        rsp.put("cartNumber", cartNumber);
        rsp.put("totalPoint",totalPoint);
        rsp.put("totalMoney",totalMoney);
        rsp.put("totalTime",totalTime);
        rsp.put("standards",standards);
        return MessageUtil.success(rsp);
    }

    /**
     * 销控系统
     * 获取购物车详情 按照集合返回 前端自己处理  getCartInfo直接返回层级前端不好处理
     * @param cartNumber 购物车编号
     * @param giveAwayCondition 置换条件
     * @return 响应
     */
    @Override
    @Transactional
    public MessageRsp getCartInfoList(String cartNumber, Map<String, Object> giveAwayCondition) {
        if (StringUtils.isBlank(cartNumber)) return MessageUtil.error("购物车编号不能为空！");
        // 删除点位统计
        Map<String,Object> param = new HashMap<>();
        param.put("cartNumber",cartNumber);
        param.put("system",CommonEnum.SystemType.SALE.getSystemType());
        this.deletePointStatisticsBySystem(param);
        // 删除统计结束

        Map<String, Object> rsp = new HashMap<>();
        List<PointStatistics> pointStatisticsList = new ArrayList<>();

        Integer totalPoint = 0;
        Float totalTime = 0.0F;
        Float totalMoney = 0.0F;
        boolean standards = true;
        // 根据参数获取数据
        List<Map<String, Object>> cartInfoList = cartPointExMapper.getCartInfoByParam(param);
        if (cartInfoList != null && cartInfoList.size() > 0) {
            // 选择点位
            List<Map<String, Object>> choosePointList = cartInfoList.stream()
                    .filter(map -> (this.getPointType(map.get("pointtype")) == CommonEnum.PointType.CHOOSE.getPointType()))
                    .collect(Collectors.toList());
            if (choosePointList != null && choosePointList.size() > 0) {
                Map<String, Object> choosePoint = this.getCartInfoAndSaleTimeByList(choosePointList, pointStatisticsList, cartNumber, CommonEnum.PointType.CHOOSE);
                List<Map<String,Object>> pointList = (List<Map<String, Object>>) choosePoint.get("list");
                rsp.put("choosePointList", pointList);

                totalPoint += Integer.valueOf(choosePoint.get("totalPoint") + "");
                totalTime += Float.valueOf(choosePoint.get("time") + "");
                totalMoney += Float.valueOf(choosePoint.get("money") + "");
                standards = Boolean.valueOf(choosePoint.get("standards") + "");
            }
            // 赠送点位
            List<Map<String, Object>> giveWayPointList = cartInfoList.stream()
                    .filter(map -> this.getPointType(map.get("pointtype")) == CommonEnum.PointType.GIVE_AWAY.getPointType())
                    .collect(Collectors.toList());
            if (giveWayPointList != null && giveWayPointList.size() > 0) {
                // 设置总送点位比例数据
                String scale = giveAwayCondition.get("scale") == null ? "" : giveAwayCondition.get("scale").toString();
                String way = giveAwayCondition.get("way") == null ? "" : giveAwayCondition.get("way").toString();
                if (StringUtils.isNotBlank(scale)){
                    StringBuilder sb = new StringBuilder();
                    sb.append(CommonConstant.GIVE_AWAY).append("-").append(cartNumber);
                    JSONObject condition = new JSONObject();
                    condition.put("scale", scale);
                    condition.put("way", way);
                    redisUtil.set(sb.toString(), condition.toJSONString(), null);
                }

                // 构造返回值
                Map<String, Object> giveWayPoint = this.getCartInfoAndSaleTimeByList(giveWayPointList, pointStatisticsList, cartNumber, CommonEnum.PointType.GIVE_AWAY);
                List<Map<String,Object>> pointList = (List<Map<String, Object>>) giveWayPoint.get("list");
                rsp.put("giveAwayPointList", pointList);

                totalPoint += Integer.valueOf(giveWayPoint.get("totalPoint") + "");
                totalTime += Float.valueOf(giveWayPoint.get("time") + "");
//                totalMoney += Float.valueOf(giveWayPoint.get("money") + "");
                standards = standards && Boolean.valueOf(giveWayPoint.get("standards") + "");
            }
        } else standards = false;
        // 赠送条件
        String condition = (String) redisUtil.get(new StringBuilder().append(CommonConstant.GIVE_AWAY).append("-").append(cartNumber).toString());
        if (StringUtils.isNotBlank(condition)) {
            rsp.put("giveWayCondition", JSON.parseObject(condition));
        }
        rsp.put("cartNumber", cartNumber);
        rsp.put("totalpoint",totalPoint);
        rsp.put("money",totalMoney);
        rsp.put("time",totalTime);
        rsp.put("satisfy",standards);
        return MessageUtil.success(rsp);
    }

    /**
     * 播控系统 详情
     * @param cartNumber 购物车编号
     * @param param 条件
     * @return 响应
     */
    @Override
    @Transactional
    public MessageRsp playCartInfoList(String cartNumber, Map<String, Object> param) {
        if (StringUtils.isBlank(cartNumber)) return MessageUtil.error("购物车编号不能为空！");
        Map<String, Object> rsp = new HashMap<>();
        Integer totalPoint = 0;
        Float totalTime = 0.0F;
        Float totalMoney = 0.0F;
        // 删除点位统计
        Map<String,Object> deleteStatisticsParam = new HashMap<>();
        deleteStatisticsParam.put("cartNumber",cartNumber);
        deleteStatisticsParam.put("system",CommonEnum.SystemType.PLAY.getSystemType());
        this.deletePointStatisticsBySystem(deleteStatisticsParam);
        // 删除统计结束
        boolean fromSale = this.checkFromSaleSystem(cartNumber);
        param.put("cartNumber",cartNumber);
        if (fromSale){
            rsp.put("fromSale",true);
            param.put("system",CommonEnum.SystemType.SALE.getSystemType());
            List<Map<String, Object>> cartInfoList = cartPointExMapper.getCartInfoByParam(param);// 销控系统数据
            if (cartInfoList != null && cartInfoList.size() > 0) {
                // 销控选择点位
                List<Map<String, Object>> choosePointList = cartInfoList.stream()
                        .filter(map -> this.getPointType(map.get("pointtype")) == CommonEnum.PointType.CHOOSE.getPointType())
                        .collect(Collectors.toList());
                if (choosePointList != null && choosePointList.size() > 0) {
                    Map<String, Object> choosePoint = this.getPlayCartInfoAndSaleTimeByOrder(choosePointList,CommonEnum.PointType.CHOOSE.getPointType(),CommonEnum.SystemType.SALE.getSystemType(), param);
                    rsp.put("saleChoosePointList", choosePoint);
                    totalPoint += Integer.valueOf(choosePoint.get("totalPoint") + "");
                    totalTime += Float.valueOf(choosePoint.get("time") + "");
                    totalMoney += Float.valueOf(choosePoint.get("money") + "");
                }
                // 销控赠送点位
                List<Map<String, Object>> giveWayPointList = cartInfoList.stream()
                        .filter(map -> this.getPointType(map.get("pointtype")) == CommonEnum.PointType.GIVE_AWAY.getPointType())
                        .collect(Collectors.toList());
                if (giveWayPointList != null && giveWayPointList.size() > 0) {
                    Map<String, Object> giveWayPoint = this.getPlayCartInfoAndSaleTimeByOrder(giveWayPointList, CommonEnum.PointType.GIVE_AWAY.getPointType(),CommonEnum.SystemType.SALE.getSystemType(),param);
                    rsp.put("saleGiveAwayPointList", giveWayPoint);
                    totalPoint += Integer.valueOf(giveWayPoint.get("totalPoint") + "");
                    totalTime += Float.valueOf(giveWayPoint.get("time") + "");
                    totalMoney += Float.valueOf(giveWayPoint.get("money") + "");
                }
            }
        }
        // 播控系统选择数据 需要单独处理
        param.put("system",CommonEnum.SystemType.PLAY.getSystemType());
        param.put("pointType",CommonEnum.PointType.CHOOSE.getPointType());
        List<Map<String, Object>> cartInfoList = cartPointExMapper.getCartInfoByParam(param);
        if (cartInfoList != null && cartInfoList.size() > 0){
            Map<String, Object> playGiveAway = this.getPlayCartInfoAndSaleTimeByList(cartInfoList, cartNumber, CommonEnum.PointType.CHOOSE);
            rsp.put("playChoosePointList", playGiveAway);
            totalPoint += Integer.valueOf(playGiveAway.get("totalPoint") + "");
            totalTime += Float.valueOf(playGiveAway.get("time") + "");
            totalMoney += Float.valueOf(playGiveAway.get("money") + "");
        }

        rsp.put("cartNumber", cartNumber);
        rsp.put("totalPoint",totalPoint);
        rsp.put("totalTime",totalTime);
        rsp.put("totalMoney",totalMoney);
        return MessageUtil.success(rsp);
    }

    /**
     * 销控下订单下详情 销控下可能存在在播控中又进行添加数据的情况 但是最终只是展示销控系统中自己添加的数据
     * @param cartNumber 购物车列表
     * @param giveAwayCondition 条件
     * @return 响应
     */
    @Override
    public MessageRsp getCartInfoDetail(String cartNumber, Map<String, Object> giveAwayCondition) {
        if (StringUtils.isBlank(cartNumber)) return MessageUtil.error("购物车编号不能为空！");
        Map<String, Object> rsp = new HashMap<>();
        Integer totalPoint = 0;
        Float totalTime = 0.0F;
        Float totalMoney = 0.0F;
        boolean standards = true;
        List<Map<String, Object>> cartInfoList = cartPointExMapper.getCartInfo(cartNumber);
        if (cartInfoList != null && cartInfoList.size() > 0) {
            // 选择点位(只展示销控系统下自身添加的点位)
            List<Map<String, Object>> choosePointList = cartInfoList.stream()
                    .filter(map -> (this.getPointType(map.get("pointtype")) == CommonEnum.PointType.CHOOSE.getPointType()
                            && this.getSystemType(map.get("system")) == CommonEnum.SystemType.SALE.getSystemType()))
                    .collect(Collectors.toList());
            if (choosePointList != null && choosePointList.size() > 0) {
                Map<String, Object> choosePoint = this.getCartInfoAndSaleTimeByOrder(choosePointList, cartNumber, CommonEnum.PointType.CHOOSE.getPointType(),CommonEnum.SystemType.SALE.getSystemType());
                rsp.put("choosePointList", choosePoint);

                totalPoint += Integer.valueOf(choosePoint.get("totalPoint") + "");
                totalTime += Float.valueOf(choosePoint.get("time") + "");
                totalMoney += Float.valueOf(choosePoint.get("money") + "");
                standards = Boolean.valueOf(choosePoint.get("standards") + "");
            }
            // 赠送点位
            List<Map<String, Object>> giveWayPointList = cartInfoList.stream()
                    .filter(map -> this.getPointType(map.get("pointtype")) == CommonEnum.PointType.GIVE_AWAY.getPointType())
                    .collect(Collectors.toList());
            if (giveWayPointList != null && giveWayPointList.size() > 0) {
                Map<String, Object> giveWayPoint = this.getCartInfoAndSaleTimeByOrder(giveWayPointList, cartNumber, CommonEnum.PointType.GIVE_AWAY.getPointType(),CommonEnum.SystemType.SALE.getSystemType());
                rsp.put("giveAwayPointList", giveWayPoint);

                totalPoint += Integer.valueOf(giveWayPoint.get("totalPoint") + "");
                totalTime += Float.valueOf(giveWayPoint.get("time") + "");
                totalMoney += Float.valueOf(giveWayPoint.get("money") + "");
                standards = standards && Boolean.valueOf(giveWayPoint.get("standards") + "");
            }
        } else standards = false;
        rsp.put("cartNumber", cartNumber);
        rsp.put("totalPoint",totalPoint);
        rsp.put("totalMoney",totalMoney);
        rsp.put("totalTime",totalTime);
        rsp.put("standards",standards);
        return MessageUtil.success(rsp);
    }

    /** 播控系统购物车详情
     * 能够看到播控系统系统选择的点位
     * @param cartNumber 购物车编号
     * @return 响应
     */
    @Override
    public MessageRsp getPlayCartInfoDetail(String cartNumber,Map<String,Object> param) {
        if (StringUtils.isBlank(cartNumber)) return MessageUtil.error("购物车编号不能为空！");
        Map<String, Object> rsp = new HashMap<>();
        Integer totalPoint = 0;
        Float totalTime = 0.0F;
        Float totalMoney = 0.0F;
        boolean standards = true;
        param.put("cartNumber",cartNumber);
        List<Map<String, Object>> cartInfoList = cartPointExMapper.getCartInfoByParam(param);// 播控系统详情
        if (cartInfoList != null && cartInfoList.size() > 0) {
            // 销控选择点位
            List<Map<String, Object>> choosePointList = cartInfoList.stream()
                    .filter(map -> (this.getPointType(map.get("pointtype")) == CommonEnum.PointType.CHOOSE.getPointType()
                                    && this.getSystemType(map.get("system")) == CommonEnum.SystemType.SALE.getSystemType()))
                    .collect(Collectors.toList());
            if (choosePointList != null && choosePointList.size() > 0) {
                Map<String, Object> choosePoint = this.getPlayCartInfoAndSaleTimeByOrder(choosePointList, CommonEnum.PointType.CHOOSE.getPointType(),CommonEnum.SystemType.SALE.getSystemType(),param);
                rsp.put("saleChoosePointList", choosePoint);

                totalPoint += Integer.valueOf(choosePoint.get("totalPoint") + "");
                totalTime += Float.valueOf(choosePoint.get("time") + "");
                totalMoney += Float.valueOf(choosePoint.get("money") + "");
                standards = Boolean.valueOf(choosePoint.get("standards") + "");
            }
            // 销控赠送点位
            List<Map<String, Object>> giveWayPointList = cartInfoList.stream()
                    .filter(map -> this.getPointType(map.get("pointtype")) == CommonEnum.PointType.GIVE_AWAY.getPointType())
                    .collect(Collectors.toList());
            if (giveWayPointList != null && giveWayPointList.size() > 0) {
                Map<String, Object> giveWayPoint = this.getPlayCartInfoAndSaleTimeByOrder(giveWayPointList, CommonEnum.PointType.GIVE_AWAY.getPointType(),CommonEnum.SystemType.SALE.getSystemType(),param);
                rsp.put("saleGiveAwayPointList", giveWayPoint);

                totalPoint += Integer.valueOf(giveWayPoint.get("totalPoint") + "");
                totalTime += Float.valueOf(giveWayPoint.get("time") + "");
                totalMoney += Float.valueOf(giveWayPoint.get("money") + "");
                standards = standards && Boolean.valueOf(giveWayPoint.get("standards") + "");
            }
            // 播控选择点位
            List<Map<String, Object>> playGiveWayPointList = cartInfoList.stream()
                    .filter(map -> this.getPointType(map.get("pointtype")) == CommonEnum.PointType.CHOOSE.getPointType()
                            && this.getSystemType(map.get("system")) == CommonEnum.SystemType.PLAY.getSystemType())
                    .collect(Collectors.toList());
            if (playGiveWayPointList != null && playGiveWayPointList.size() > 0) {
                Map<String, Object> playGiveWayPoint = this.getPlayCartInfoAndSaleTimeByOrder(playGiveWayPointList, CommonEnum.PointType.CHOOSE.getPointType(),CommonEnum.SystemType.PLAY.getSystemType(),param);
                rsp.put("playChoosePointList", playGiveWayPoint);

                totalPoint += Integer.valueOf(playGiveWayPoint.get("totalPoint") + "");
                totalTime += Float.valueOf(playGiveWayPoint.get("time") + "");
                totalMoney += Float.valueOf(playGiveWayPoint.get("money") + "");
                standards = standards && Boolean.valueOf(playGiveWayPoint.get("standards") + "");
            }
        } else standards = false;
        rsp.put("cartNumber", cartNumber);
        rsp.put("totalPoint",totalPoint);
        rsp.put("totalMoney",totalMoney);
        rsp.put("totalTime",totalTime);
        rsp.put("standards",standards);
        return MessageUtil.success(rsp);
    }

    /**
     * 销控 获取投放时间
     * @param cartNumber 购物车编号
     * @return 响应
     */
    @Override
    public MessageRsp getPutDate(String cartNumber) {
        Map<String, Long> districtByCartNumber = cartPointExMapper.getDistrictByCartNumber(cartNumber);
        if (districtByCartNumber == null) return MessageUtil.error("当前购物车无选择点位！");
        return MessageUtil.success(districtByCartNumber);
    }

    /**
     * 删除点位统计
     * @param cartNumber 购物车编号
     */
    public void deletePointStatistics(String cartNumber){
        Integer deleteNumber = cartPointExMapper.deletePointStatistics(cartNumber);
        logger.info("成功【删除】{}点位统计统计{}条数据！",cartNumber, deleteNumber);

        // 删除统计缓存
        redisUtil.batchDelete(new StringBuffer().append(CommonConstant.CART_NUMBER).append("-").append(cartNumber).append("-STATISTTCS").toString());
    }

    /**
     * 不影响之前的代码重写
     * @param deleteStatisticsParam 删除参数
     */
    private void deletePointStatisticsBySystem(Map<String, Object> deleteStatisticsParam) {
        int deleteNumber = cartPointExMapper.deletePointStatisticsBySystem(deleteStatisticsParam);
        logger.info("成功删除点位统计{}条",deleteNumber);

        // 删除统计缓存
        String cartNumber = deleteStatisticsParam.get("cartNumber") + "";
        redisUtil.batchDelete(new StringBuffer().append(CommonConstant.CART_NUMBER).append("-").append(cartNumber).append("-STATISTTCS").toString());
    }

    /**
     * 添加统计
     *
     * @param cartNumber          购物车编号
     * @param pointStatisticsList 统计集合
     */
    private void addPointStatistics(String cartNumber, List<PointStatistics> pointStatisticsList,CommonEnum.PointType pointType) {
        Integer number = cartPointExMapper.addPointStatistics(pointStatisticsList);
        logger.info("成功【添加】{}点位类型{}统计{}条数据！",cartNumber, pointType.getTypeName(), number);
    }

    /**
     * 获取选点模式
     *
     * @param pointType 点位类型
     * @return 响应
     */
    private int getPointType(Object pointType) {
        int type;
        if (pointType == null || StringUtils.isBlank(pointType + "")) type = 0;
        else type = Integer.valueOf(pointType + "");
        return type;
    }

    /**
     * 获取选点模式
     *
     * @param systemType 系统类型
     * @return 响应
     */
    private int getSystemType(Object systemType) {
        int system;
        if (systemType == null || StringUtils.isBlank(systemType + "")) system = 1;
        else system = Integer.valueOf(systemType + "");
        return system;
    }

    /**
     * 点位详情
     *
     * @param cartInfoList        点位集合列表
     * @param cartNumber          购物车编号
     * @return 响应
     */
    private Map<String, Object> getCartInfoAndSaleTime(List<Map<String, Object>> cartInfoList,String cartNumber, CommonEnum.PointType pointType) {
        PointStatistics pointStatistics = null;
        List<PointStatistics> statisticsSaleSetList = new ArrayList<>();
        List<PointStatistics> statisticsCartList = new ArrayList<>();
        Map<String, Object> rsp = new HashMap<>();
        List<Map<String, Object>> resultList = new ArrayList<>();
        Integer allTotalPoint = 0;// 通过程序计算总数
        Float allTotalTime = 0.0F;// 总时间
        Float allTotalMoney = 0.0F;// 总金额
        Integer allStandardsNumber = 0;// 是否达标

        Map<String, String> cityLevelMap = new HashMap<>();// 城市级别
        Map<String, Integer> citySatisfyNumberMap = new HashMap<>(); // 判断是否达标
        Map<String, Float> cityTotalTime = new HashMap<>();// 城市级别总时长
        Map<String, Float> cityTotalMoney = new HashMap<>();// 城市级别总金额
        Map<Integer, Object[]> cityTimeMoney = new HashMap<>();// 每个城市对应的时长和金额、达标
        int length = cartInfoList.size();
        if (cartInfoList.size() > 0) {
            // 调用获取金额和时长接口
            JSONArray params = getSaleParam(cartInfoList);
            JSONObject saleResult = null;
            try {
                saleResult = saleSetClient.getTimeAndAmountAndStatus(params);
//              saleResult = JerseyUtil.sendPostByJson(saleUrl, params);
            } catch (Exception e) {
                logger.error("调用获取金额发生错误！");
                System.out.println(e.getMessage());
            }
            if (saleResult != null && saleResult.getInteger("errorcode") == 0) {
                JSONArray data = saleResult.getJSONArray("data");
                for (int i = 0; i < data.size(); i++) {
                    JSONObject result = data.getJSONObject(i);
                    String adCode = result.getString("adCode");// 城市代码
                    String cityLevel = result.getString("cityLevel");// 城市级别
                    Float playHours = result.getFloat("playHours");// 时长
                    Float amount = result.getFloat("amount");// 金额
                    String satisfy = result.getString("satisfy");// 是否达标

                    // 设置时长价格统计开始
                    pointStatistics = new PointStatistics();
                    pointStatistics.setCityLevel(cityLevel);
                    pointStatistics.setPointTime(playHours);
                    pointStatistics.setPointMoney(amount);
                    pointStatistics.setSatisfy(satisfy);
                    statisticsSaleSetList.add(pointStatistics);
                    // 时长价格统计结束

                    // key:下标  value:时间、金额、是否达标
                    Object[] timeMoneySatisfy = new Object[]{playHours, amount, satisfy};
                    cityTimeMoney.put(i, timeMoneySatisfy);

                    // key:城市级别  value:总时长
                    Float totalTime = cityTotalTime.get(cityLevel);
                    if (totalTime == null) totalTime = 0.0F;
                    cityTotalTime.put(cityLevel, (totalTime + playHours));

                    // key:城市级别 value:总金额
                    Float totalMoney = cityTotalMoney.get(cityLevel);
                    if (totalMoney == null) totalMoney = 0.0F;
                    cityTotalMoney.put(cityLevel, (totalMoney + amount));

                    // key:城市代码  value:城市级别
                    cityLevelMap.put(adCode, cityLevel);

                    // 判断是否达标  key:城市级别 value:达标个数
                    if (Boolean.valueOf(satisfy)) {
                        Integer number = citySatisfyNumberMap.get(cityLevel);
                        if (number == null) number = 0;
                        citySatisfyNumberMap.put(cityLevel, ++number);
                    }
                }
            } else {
                rsp.put("errorMessage", saleResult == null ? "调用获取金额服务错误！" : saleResult.getString("errormsg"));
            }

            List<String> cityLevelList = new ArrayList<>();
            Map<String, Integer> cityTotalPoint = new HashMap<>();
            // 重新购物车构造数据
            Map<String, List<Map<String, Object>>> cityScreenMap = new HashMap<>();
            for (int i = 0; i < length; i++) {
                pointStatistics = new PointStatistics();
                Map<String, Object> cartPointMap = cartInfoList.get(i);
                Object[] timeMoneySatisfy = cityTimeMoney.get(i);
                if (timeMoneySatisfy != null){
                    cartPointMap.put("time", timeMoneySatisfy[0]);
                    cartPointMap.put("money", timeMoneySatisfy[1]);
                    cartPointMap.put("satisfy",pointType.getPointType() == 1 ? null : timeMoneySatisfy[2]);
                }

                int pointNumber = Integer.valueOf(cartPointMap.get("totalpoint") + ""); // 点位个数
                Integer timeLength = Integer.valueOf(cartPointMap.get("timelength") + "");// 刊位时长
                Integer number = Integer.valueOf(cartPointMap.get("number") + "");// 个数
                Long startTime = Long.valueOf(cartPointMap.get("starttime") + "");// 开始时间
                Long entTime = Long.valueOf(cartPointMap.get("endtime") + ""); // 结束时间
                String cityCode = String.valueOf(cartPointMap.get("citycode"));// 城市代码
                String cityName = String.valueOf(cartPointMap.get("cityname"));// 城市名称
                // 购物车统计开始
                pointStatistics.setPointNumber(pointNumber);
                pointStatistics.setTimeLength(timeLength);
                pointStatistics.setNumber(number);
                pointStatistics.setStartTime(startTime);
                pointStatistics.setEndTime(entTime);
                pointStatistics.setCityCode(cityCode);
                pointStatistics.setCityName(cityName);
                statisticsCartList.add(pointStatistics);
                // 购物车统计结束

                // 城市级别下点位个数 key:城市级别  value:点位个数
                String cityLevel = cityLevelMap.get(cityCode);
                Integer cityLevelNumber = cityTotalPoint.get(cityLevel);
                if (cityLevelNumber == null) cityLevelNumber = 0;
                cityTotalPoint.put(cityLevel, (cityLevelNumber + pointNumber));

                // 封装数据 key：城市级别 value：点位信息集合
                List<Map<String, Object>> list = cityScreenMap.get(cityLevel);
                if (list == null) {
                    cityLevelList.add(cityLevel);
                    list = new ArrayList<>();
                }
                list.add(cartPointMap);
                cityScreenMap.put(cityLevel, list);
            }
            // 添加统计数据
            this.addStatistics(statisticsSaleSetList, statisticsCartList, cartNumber, pointType);
            
            // 排序
            cityLevelList.sort((city1, city2) -> city1.compareTo(city2));
            // 构造返回值
            Map<String, Object> resultMap;
            // 赠送需要判断是否满足条件
            Map<String, Boolean> giveAwayMap = null;
            if (pointType.getPointType() == 1) giveAwayMap = this.checkGiveAway(cartNumber);

            for (int i = 0, size = cityLevelList.size(); i < size; i++) {
                resultMap = new HashMap<>();
                String cityLevelName = cityLevelList.get(i);
                // 城市级别下总个数
                Integer totalPoint = cityTotalPoint.get(cityLevelName);
                allTotalPoint += totalPoint;
                // 城市级别下总时间
                Float totalTime = cityTotalTime.get(cityLevelName);
                if (totalTime != null) allTotalTime += totalTime;
                // 城市级别下总金额
                Float totalMoney = cityTotalMoney.get(cityLevelName);
                if (totalMoney != null) allTotalMoney += totalMoney;
                // 城市级别下达标的个数
                Integer standardsNumber = citySatisfyNumberMap.get(cityLevelName);
                if (standardsNumber != null) allStandardsNumber += standardsNumber;
                // 城市级别下总个数
                List<Map<String, Object>> list = cityScreenMap.get(cityLevelName);
                resultMap.put("totalPoint", totalPoint);
                resultMap.put("totalTime", totalTime);
                resultMap.put("totalMoney", totalMoney);
                if (pointType.getPointType() == 0){
                    resultMap.put("satisfy", standardsNumber != null && standardsNumber == list.size());
                } else {
                    Boolean flag = giveAwayMap.get(cityLevelName);
                    resultMap.put("satisfy", flag == null ? false : flag);
                }
                resultMap.put("cityLevel", cityLevelName);
                resultMap.put("list", list);
                resultList.add(resultMap);
            }
        }
        // 时长和金额
        rsp.put("totalPoint", allTotalPoint);
        rsp.put("totalTime", allTotalTime);
        rsp.put("totalMoney", allTotalMoney);
        rsp.put("standards", allStandardsNumber == length);
        rsp.put("cartNumber", cartNumber);
        rsp.put("total", length);
        rsp.put("list", resultList);
        return rsp;
    }

    /**
     * 销控点位详情(集合形式)
     * 会添加数据到统计
     * @param cartInfoList        点位集合列表
     * @param pointStatisticsList 点位统计集合
     * @param cartNumber          购物车编号
     * @return 响应
     */
    private Map<String, Object> getCartInfoAndSaleTimeByList(List<Map<String, Object>> cartInfoList, List<PointStatistics> pointStatisticsList, String cartNumber, CommonEnum.PointType pointType) {
        PointStatistics pointStatistics = null;
        List<PointStatistics> statisticsSaleSetList = new ArrayList<>();
        List<PointStatistics> statisticsCartList = new ArrayList<>();
        Map<String, Object> rsp = new HashMap<>();
        Integer allTotalPoint = 0;// 通过程序计算总数
        Float allTotalTime = 0.0F;// 总时间
        Float allTotalMoney = 0.0F;// 总金额
        Boolean allStandards = true;
        Integer allStandardsNumber = 0;// 是否达标

        Map<String, String> cityLevelMap = new HashMap<>();// 城市级别
        Map<String, Integer> citySatisfyNumberMap = new HashMap<>(); // 判断是否达标
        Map<String, Float> cityTotalTime = new HashMap<>();// 城市级别总时长
        Map<String, Float> cityTotalMoney = new HashMap<>();// 城市级别总金额
        Map<Integer, Object[]> cityTimeMoney = new HashMap<>();// 每个城市对应的时长和金额、达标
        int length = cartInfoList.size();
        if (cartInfoList.size() > 0) {
            // 调用获取金额和时长接口
            JSONArray params = getSaleParam(cartInfoList);
            JSONObject saleSetResult;
            try {
                saleSetResult = saleSetClient.getTimeAndAmountAndStatus(params);// feign调用服务
            } catch (Exception e) {
                logger.error("调用获取金额发生错误！");
                System.out.println(e.getMessage());
                throw new GlobalException(CommonEnum.Message.ERROR.getCode(),"调用获取金额发生错误！");
            }
            if (saleSetResult != null && saleSetResult.getInteger("errorcode") == 0) {
                JSONArray data = saleSetResult.getJSONArray("data");
                for (int i = 0,size = data.size(); i < size; i++) {
                    JSONObject result = data.getJSONObject(i);
                    String adCode = result.getString("adCode");// 城市代码
                    String cityLevel = result.getString("cityLevel");// 城市级别
                    Float playHours = this.setNumberPrecision(result.getFloat("playHours"));// 时长
                    Float amount = this.setNumberPrecision(pointType.getPointType() == CommonEnum.PointType.GIVE_AWAY.getPointType() ? 0.0F : result.getFloat("amount"));// 金额保留一位小数
                    String satisfy = result.getString("satisfy");// 是否达标

                    // 设置统计开始
                    pointStatistics = new PointStatistics();
                    pointStatistics.setCityLevel(cityLevel);
                    pointStatistics.setPointTime(playHours);
                    pointStatistics.setPointMoney(pointType.getPointType() == CommonEnum.PointType.GIVE_AWAY.getPointType() ? 0.0F : amount); // 赠送不需要计算金额
                    pointStatistics.setSatisfy(satisfy);
                    statisticsSaleSetList.add(pointStatistics);
                    // 统计结束

                    // key:下标  value:时间、金额、是否达标
                    Object[] timeMoneySatisfy = new Object[]{playHours, amount, satisfy};
                    cityTimeMoney.put(i, timeMoneySatisfy);

                    // key:城市级别  value:总时长
                    Float totalTime = cityTotalTime.get(cityLevel);
                    if (totalTime == null) totalTime = 0.0F;
                    cityTotalTime.put(cityLevel, (totalTime + playHours));

                    // key:城市级别 value:总金额
                    Float totalMoney = cityTotalMoney.get(cityLevel);
                    if (totalMoney == null) totalMoney = 0.0F;
                    cityTotalMoney.put(cityLevel, (totalMoney + amount));

                    // key:城市代码  value:城市级别
                    cityLevelMap.put(adCode, cityLevel);

                    // 判断是否达标  key:城市级别 value:达标个数
                    if (Boolean.valueOf(satisfy)) {
                        Integer number = citySatisfyNumberMap.get(cityLevel);
                        if (number == null) number = 0;
                        citySatisfyNumberMap.put(cityLevel, ++number);
                    }
                }
            } else {
                String errorMsg = saleSetResult == null ? "调用获取金额服务错误！" : saleSetResult.getString("errormsg");
                rsp.put("errorMessage", errorMsg);
                throw new GlobalException(CommonEnum.Message.ERROR.getCode(),errorMsg);
            }

            List<String> cityLevelList = new ArrayList<>();
            Map<String, Integer> cityTotalPoint = new HashMap<>();
            // 重新购物车构造数据
            Map<String, List<Map<String, Object>>> cityScreenMap = new HashMap<>();
            for (int i = 0; i < length; i++) {
                pointStatistics = new PointStatistics();
                Map<String, Object> cartPointMap = cartInfoList.get(i);
                Object[] timeMoneySatisfy = cityTimeMoney.get(i);
                if (timeMoneySatisfy != null){
                    cartPointMap.put("time", timeMoneySatisfy[0]);
                    cartPointMap.put("money", pointType.getPointType() == CommonEnum.PointType.GIVE_AWAY.getPointType() ? 0.0 : timeMoneySatisfy[1]);// 赠送点位不需要计算金钱
                    cartPointMap.put("satisfy",pointType.getPointType() == CommonEnum.PointType.GIVE_AWAY.getPointType() ? null : Boolean.valueOf(timeMoneySatisfy[2].toString()));// 赠送点位城市下不需要展示是否达标
                } /*else { // TODO 正式环境取消测试代码
                    cartPointMap.put("time", 12.6);
                    cartPointMap.put("money", 65.2);
                    cartPointMap.put("satisfy",true);
                }*/

                int pointNumber = Integer.valueOf(cartPointMap.get("totalpoint") + ""); // 点位个数
                Integer timeLength = Integer.valueOf(cartPointMap.get("timelength") + "");// 刊位时长
                Integer number = Integer.valueOf(cartPointMap.get("number") + "");// 个数
                Long startTime = Long.valueOf(cartPointMap.get("starttime") + "");// 开始时间
                Long entTime = Long.valueOf(cartPointMap.get("endtime") + ""); // 结束时间
                String cityCode = String.valueOf(cartPointMap.get("citycode"));// 城市代码
                String cityName = String.valueOf(cartPointMap.get("cityname"));// 城市名称
                // 统计开始
                pointStatistics.setPointNumber(pointNumber);
                pointStatistics.setTimeLength(timeLength);
                pointStatistics.setNumber(number);
                pointStatistics.setStartTime(startTime);
                pointStatistics.setEndTime(entTime);
                pointStatistics.setCityCode(cityCode);
                pointStatistics.setCityName(cityName);
                pointStatistics.setSystem(CommonEnum.SystemType.SALE.getSystemType());
                statisticsCartList.add(pointStatistics);
                // 统计结束

                // 城市级别下点位个数 key:城市级别  value:点位个数
                String cityLevel = cityLevelMap.get(cityCode);
                // TODO 构造城市级别(正式环境注释)
                /*if (StringUtils.isBlank(cityLevel)){
                    List<String> firstLevel = Stream.of("310100", "510100", "110100", "440100", "440300").collect(Collectors.toList());
                    if (firstLevel.contains(cityCode)){
                        cityLevel = "一线城市";
                    } else {
                        cityLevel = "二线城市";
                    }
                }*/
                cartPointMap.put("cityLevel",cityLevel); // 设置城市级别

                // 城市级别下点位个数 key:城市级别  value:点位个数
                Integer cityLevelNumber = cityTotalPoint.get(cityLevel);
                if (cityLevelNumber == null) cityLevelNumber = 0;
                cityTotalPoint.put(cityLevel, (cityLevelNumber + pointNumber));

                // 封装数据 key：城市级别 value：点位信息集合
                List<Map<String, Object>> list = cityScreenMap.get(cityLevel);
                if (list == null) {
                    cityLevelList.add(cityLevel);
                    list = new ArrayList<>();
                }
                list.add(cartPointMap);
                cityScreenMap.put(cityLevel, list);
            }

            // 添加统计数据
            this.addStatistics(statisticsSaleSetList, statisticsCartList, cartNumber, pointType);
            // 排序
            cityLevelList.sort((city1, city2) -> city1.compareTo(city2));
            // 构造返回值
            Map<String, Object> resultMap;
            // 赠送需要判断是否满足条件
            Map<String, Boolean> giveAwayMap = null;
            if (pointType.getPointType() == 1) giveAwayMap = this.checkGiveAway(cartNumber);

            // 按照集合方式回显给前端
            List<Map<String, Object>> allList = new ArrayList<>();
            for (int i = 0, size = cityLevelList.size(); i < size; i++) {
                resultMap = new HashMap<>();
                String cityLevelName = cityLevelList.get(i);
                // 城市级别下总个数
                Integer totalPoint = cityTotalPoint.get(cityLevelName);
                allTotalPoint += totalPoint;
                // 城市级别下总时间
                Float totalTime = cityTotalTime.get(cityLevelName);
                if (totalTime != null) allTotalTime += totalTime;
                // 城市级别下总金额
                Float totalMoney = cityTotalMoney.get(cityLevelName);
                if (totalMoney != null) allTotalMoney += totalMoney;
                // 城市级别下达标的个数
                Integer standardsNumber = citySatisfyNumberMap.get(cityLevelName);
                if (standardsNumber != null) allStandardsNumber += standardsNumber;
                // 城市级别下总个数
                List<Map<String, Object>> list = cityScreenMap.get(cityLevelName);
                allList.addAll(list);
                // 追加单个城市级别总统计
                resultMap.put("totalPoint", totalPoint);
                resultMap.put("time", this.setNumberPrecision(totalTime));
                resultMap.put("money", this.setNumberPrecision(totalMoney));
                resultMap.put("cityLevel", cityLevelName);
                if (pointType.getPointType() == CommonEnum.PointType.CHOOSE.getPointType()){
                    Boolean stan = standardsNumber != null && standardsNumber == list.size();
                    allStandards = allStandards && stan;
                    resultMap.put("satisfy", stan);
                } else {
                    Boolean flag = giveAwayMap.get(cityLevelName);
                    allStandards = allStandards && flag;
                    resultMap.put("satisfy", flag == null ? false : flag);
                }
                allList.add(resultMap);// 追加到集合中
            }
            // 追加选择或是赠送总统计
            resultMap = new HashMap<>();
            resultMap.put("totalPoint", allTotalPoint);
            resultMap.put("time", this.setNumberPrecision(allTotalTime));
            resultMap.put("money", this.setNumberPrecision(allTotalMoney));
            resultMap.put("satisfy", allStandards);
            allList.add(resultMap);
            rsp.put("list",allList);
        }
        // 时长和金额
        rsp.put("totalPoint", allTotalPoint);
        rsp.put("time", this.setNumberPrecision(allTotalTime));
        rsp.put("money", this.setNumberPrecision(allTotalMoney));
        rsp.put("standards", allStandards);
        rsp.put("cartNumber", cartNumber);
        rsp.put("total", length);
        return rsp;
    }


    /**
     * 播控系统点位详情(集合形式)
     * @param cartInfoList        点位集合列表
     * @param cartNumber          购物车编号
     * @return 响应
     */
    private Map<String, Object> getPlayCartInfoAndSaleTimeByList(List<Map<String, Object>> cartInfoList, String cartNumber, CommonEnum.PointType pointType) {
        PointStatistics pointStatistics;
        List<PointStatistics> statisticsSaleSetList = new ArrayList<>();
        List<PointStatistics> statisticsCartList = new ArrayList<>();
        Map<String, Object> rsp = new HashMap<>();
        Integer allTotalPoint = 0;// 通过程序计算总数
        Float allTotalTime = 0.0F;// 总时间
        Float allTotalMoney = 0.0F;// 总金额

        Map<String, String> cityLevelMap = new HashMap<>();// 城市级别
        Map<String, Float> cityTotalTime = new HashMap<>();// 城市级别总时长
        Map<String, Float> cityTotalMoney = new HashMap<>();// 城市级别总金额
        Map<Integer, Object[]> cityTimeMoney = new HashMap<>();// 每个城市对应的时长和金额、达标
        int length = cartInfoList.size();
        if (cartInfoList.size() > 0) {
            JSONArray params = getSaleParam(cartInfoList);
            // 调用获取金额和时长接口
            JSONObject saleSetResult = null;
            try {
                saleSetResult = saleSetClient.getTimeAndAmountAndStatus(params);// feign调用服务
            } catch (Exception e) {
                logger.error("调用获取金额发生错误！");
                System.out.println(e.getMessage());
                throw new GlobalException(1,"调用获取金额发生错误！");
            }
            if (saleSetResult != null && saleSetResult.getInteger("errorcode") == 0) {
                JSONArray data = saleSetResult.getJSONArray("data");
                for (int i = 0,size = data.size(); i < size; i++) {
                    JSONObject result = data.getJSONObject(i);
                    String adCode = result.getString("adCode");// 城市代码
                    String cityLevel = result.getString("cityLevel");// 城市级别
                    Float playHours = this.setNumberPrecision(result.getFloat("playHours"));// 时长 保留一位
                    Float amount = this.setNumberPrecision(result.getFloat("amount"));// 金额保留一位

                    // 设置统计开始
                    pointStatistics = new PointStatistics();
                    pointStatistics.setCityLevel(cityLevel);
                    pointStatistics.setPointTime(playHours);
                    pointStatistics.setPointMoney(amount);
                    statisticsSaleSetList.add(pointStatistics);
                    // 统计结束

                    // key:下标  value:时间、金额、是否达标
                    Object[] timeMoneySatisfy = new Object[]{playHours, amount};
                    cityTimeMoney.put(i, timeMoneySatisfy);

                    // key:城市级别  value:总时长
                    Float totalTime = cityTotalTime.get(cityLevel);
                    if (totalTime == null) totalTime = 0.0F;
                    cityTotalTime.put(cityLevel, (totalTime + playHours));

                    // key:城市级别 value:总金额
                    Float totalMoney = cityTotalMoney.get(cityLevel);
                    if (totalMoney == null) totalMoney = 0.0F;
                    cityTotalMoney.put(cityLevel, (totalMoney + amount));

                    // key:城市代码  value:城市级别
                    cityLevelMap.put(adCode, cityLevel);
                }
            } else {
                String errorMessage = saleSetResult == null ? "调用获取金额服务错误！" : saleSetResult.getString("errormsg");
                rsp.put("errorMessage", errorMessage);
                throw new GlobalException(1,errorMessage);
            }

            List<String> cityLevelList = new ArrayList<>();
            Map<String, Integer> cityTotalPoint = new HashMap<>();
            // 重新购物车构造数据
            Map<String, List<Map<String, Object>>> cityScreenMap = new HashMap<>();
            for (int i = 0; i < length; i++) {
                pointStatistics = new PointStatistics();
                Map<String, Object> cartPointMap = cartInfoList.get(i);
                Object[] timeMoneySatisfy = cityTimeMoney.get(i);
                if (timeMoneySatisfy != null){
                    cartPointMap.put("time", timeMoneySatisfy[0]);
                    cartPointMap.put("money", timeMoneySatisfy[1]);// 赠送点位不需要计算金钱
                }

                int pointNumber = Integer.valueOf(cartPointMap.get("totalpoint") + ""); // 点位个数
                Integer timeLength = Integer.valueOf(cartPointMap.get("timelength") + "");// 刊位时长
                Integer number = Integer.valueOf(cartPointMap.get("number") + "");// 个数
                Long startTime = Long.valueOf(cartPointMap.get("starttime") + "");// 开始时间
                Long entTime = Long.valueOf(cartPointMap.get("endtime") + ""); // 结束时间
                String cityCode = String.valueOf(cartPointMap.get("citycode"));// 城市代码
                String cityName = String.valueOf(cartPointMap.get("cityname"));// 城市名称
                // 统计开始
                pointStatistics.setPointNumber(pointNumber);
                pointStatistics.setTimeLength(timeLength);
                pointStatistics.setNumber(number);
                pointStatistics.setStartTime(startTime);
                pointStatistics.setEndTime(entTime);
                pointStatistics.setCityCode(cityCode);
                pointStatistics.setCityName(cityName);
                pointStatistics.setSystem(CommonEnum.SystemType.PLAY.getSystemType()); // 统计设置为播控
                statisticsCartList.add(pointStatistics);
                // 统计结束

                // 城市级别下点位个数 key:城市级别  value:点位个数
                String cityLevel = cityLevelMap.get(cityCode);
                cartPointMap.put("cityLevel",cityLevel); // 设置城市级别

                // 城市级别下点位个数 key:城市级别  value:点位个数
                Integer cityLevelNumber = cityTotalPoint.get(cityLevel);
                if (cityLevelNumber == null) cityLevelNumber = 0;
                cityTotalPoint.put(cityLevel, (cityLevelNumber + pointNumber));

                // 封装数据 key：城市级别 value：点位信息集合
                List<Map<String, Object>> list = cityScreenMap.get(cityLevel);
                if (list == null) {
                    cityLevelList.add(cityLevel);
                    list = new ArrayList<>();
                }
                list.add(cartPointMap);
                cityScreenMap.put(cityLevel, list);
            }

            // 添加统计数据
            this.addStatistics(statisticsSaleSetList, statisticsCartList, cartNumber, pointType);
            // 排序
            cityLevelList.sort((city1, city2) -> city1.compareTo(city2));
            // 构造返回值
            Map<String, Object> resultMap;
            // 按照集合方式回显给前端
            List<Map<String, Object>> allList = new ArrayList<>();
            for (int i = 0, size = cityLevelList.size(); i < size; i++) {
                resultMap = new HashMap<>();
                String cityLevelName = cityLevelList.get(i);
                // 城市级别下总个数
                Integer totalPoint = cityTotalPoint.get(cityLevelName);
                allTotalPoint += totalPoint;
                // 城市级别下总时间
                Float totalTime = cityTotalTime.get(cityLevelName);
                if (totalTime != null) allTotalTime += totalTime;
                // 城市级别下总金额
                Float totalMoney = cityTotalMoney.get(cityLevelName);
                if (totalMoney != null) allTotalMoney += totalMoney;
                // 城市级别下总个数
                List<Map<String, Object>> list = cityScreenMap.get(cityLevelName);
                allList.addAll(list);
                // 追加单个城市级别总统计
                resultMap.put("totalPoint", totalPoint);
                resultMap.put("time", this.setNumberPrecision(totalTime));
                resultMap.put("money", this.setNumberPrecision(totalMoney));
                resultMap.put("cityLevel", cityLevelName);
                allList.add(resultMap);// 追加到集合中
            }
            // 追加选择或是赠送总统计
            resultMap = new HashMap<>();
            resultMap.put("totalPoint", allTotalPoint);
            resultMap.put("time", this.setNumberPrecision(allTotalTime));
            resultMap.put("money", this.setNumberPrecision(allTotalMoney));
            allList.add(resultMap);
            rsp.put("list",allList);
        }
        // 时长和金额
        rsp.put("totalPoint", allTotalPoint);
        rsp.put("time", this.setNumberPrecision(allTotalTime));
        rsp.put("money", this.setNumberPrecision(allTotalMoney));
        rsp.put("cartNumber", cartNumber);
        rsp.put("total", length);
        return rsp;
    }

    /**
     * 订单下点位详情(从数据库中获取)
     * @param cartInfoList 点位列表
     * @param cartNumber 购物车编号
     * @param pointType 点位类型
     * @param system 系统类别
     * @return 响应
     */
    private Map<String,Object> getCartInfoAndSaleTimeByOrder(List<Map<String, Object>> cartInfoList, String cartNumber, int pointType, int system) {
        Map<String, Object> rsp = new HashMap<>();
        Integer allTotalPoint = 0;// 通过程序计算总数
        Float allTotalTime = 0.0F;// 总时间
        Float allTotalMoney = 0.0F;// 总金额
        Boolean allStandards = true; // 总达标与否
        Integer allStandardsNumber = 0;// 是否达标

        Map<String, String> cityLevelMap = new HashMap<>();// 城市级别
        Map<String, Integer> citySatisfyNumberMap = new HashMap<>(); // 判断是否达标
        Map<String, Float> cityTotalTime = new HashMap<>();// 城市级别总时长
        Map<String, Float> cityTotalMoney = new HashMap<>();// 城市级别总金额
        Map<Integer, Object[]> cityTimeMoney = new HashMap<>();// 每个城市对应的时长和金额、达标
        int length = cartInfoList.size();
        if (cartInfoList.size() > 0) {
            // 查询点位统计数据
            List<PointStatistics> statisticsList = this.getPointStatistics(cartNumber, pointType);
            // 过滤
            statisticsList = statisticsList.stream().filter(pointSta -> pointSta.getSystem() == system).collect(Collectors.toList());
            if (statisticsList != null && statisticsList.size() > 0) {
                for (int i = 0,size = statisticsList.size(); i < size; i++) {
                    PointStatistics pointStatistics = statisticsList.get(i);
                    String adCode = pointStatistics.getCityCode();// 城市代码
                    String cityLevel = pointStatistics.getCityLevel();// 城市级别

                    Float playHours = this.setNumberPrecision(pointStatistics.getPointTime() == null ? 0.0F : pointStatistics.getPointTime());// 时长
                    Float amount = this.setNumberPrecision((pointStatistics.getPointMoney() == null || pointType == CommonEnum.PointType.GIVE_AWAY.getPointType()) ? 0.0F : pointStatistics.getPointMoney());// 金额 赠送不需要计算金额
                    Boolean satisfy = Boolean.valueOf(pointStatistics.getSatisfy());// 是否达标

                    // key:下标  value:时间、金额、是否达标
                    Object[] timeMoneySatisfy = new Object[]{playHours, amount, satisfy};
                    cityTimeMoney.put(i, timeMoneySatisfy);

                    // key:城市级别  value:总时长
                    Float totalTime = cityTotalTime.get(cityLevel);
                    if (totalTime == null) totalTime = 0.0F;
                    cityTotalTime.put(cityLevel, (totalTime + playHours));

                    // key:城市级别 value:总金额
                    Float totalMoney = cityTotalMoney.get(cityLevel);
                    if (totalMoney == null) totalMoney = 0.0F;
                    cityTotalMoney.put(cityLevel, (totalMoney + amount));

                    // key:城市代码  value:城市级别
                    cityLevelMap.put(adCode, cityLevel);

                    // 判断是否达标  key:城市级别 value:达标个数
                    if (satisfy) {
                        Integer number = citySatisfyNumberMap.get(cityLevel);
                        if (number == null) number = 0;
                        citySatisfyNumberMap.put(cityLevel, ++number);
                    }
                }
            } else {
                rsp.put("errorMessage", "获取金额发生异常！");
            }

            List<String> cityLevelList = new ArrayList<>();
            Map<String, Integer> cityTotalPoint = new HashMap<>();
            // 重新购物车构造数据
            Map<String, List<Map<String, Object>>> cityScreenMap = new HashMap<>();
            for (int i = 0; i < length; i++) {
                Map<String, Object> cartPointMap = cartInfoList.get(i);
                Object[] timeMoneySatisfy = cityTimeMoney.get(i);
                if (timeMoneySatisfy != null){
                    cartPointMap.put("time", timeMoneySatisfy[0]);
                    cartPointMap.put("money", pointType == CommonEnum.PointType.GIVE_AWAY.getPointType() ? 0.0 : timeMoneySatisfy[1]);// 赠送不需要计算金额
                    cartPointMap.put("satisfy",pointType == CommonEnum.PointType.GIVE_AWAY.getPointType() ? null : Boolean.valueOf(timeMoneySatisfy[2].toString())); // 赠送单个城市不需要计算是否达标
                } /*else {// TODO 测试环境注释测试代码
                    cartPointMap.put("time", 12.6);
                    cartPointMap.put("money", 65.2);
                    cartPointMap.put("satisfy",true);
                }*/

                int pointNumber = Integer.valueOf(cartPointMap.get("totalpoint") + ""); // 点位个数
                String cityCode = String.valueOf(cartPointMap.get("citycode"));// 城市代码

                // 城市级别下点位个数 key:城市级别  value:点位个数
                String cityLevel = cityLevelMap.get(cityCode);
                // TODO 构造城市级别(正式环境注释测试代码)
                /*if (StringUtils.isBlank(cityLevel)){
                    List<String> firstLevel = Stream.of("310100", "510100", "110100", "440100", "440300").collect(Collectors.toList());
                    if (firstLevel.contains(cityCode)){
                        cityLevel = "一线城市";
                    } else {
                        cityLevel = "二线城市";
                    }
                }*/
                cartPointMap.put("cityLevel",cityLevel); // 设置城市级别
                cartPointMap.put("system",system);// 设置所属系统

                // 获取总的点位个数
                Integer number = cityTotalPoint.get(cityLevel);
                if (number == null) number = 0;
                cityTotalPoint.put(cityLevel, (number + pointNumber));

                // 封装数据 key：城市级别 value：点位信息集合
                List<Map<String, Object>> list = cityScreenMap.get(cityLevel);
                if (list == null) {
                    cityLevelList.add(cityLevel);
                    list = new ArrayList<>();
                }
                list.add(cartPointMap);
                cityScreenMap.put(cityLevel, list);
            }
            // 排序
            cityLevelList.sort((city1, city2) -> city1.compareTo(city2));
            // 构造返回值
            Map<String, Object> resultMap;
            // 赠送需要判断是否满足条件
            Map<String, Boolean> giveAwayMap = null;
            if (pointType == 1) giveAwayMap = this.checkGiveAway(cartNumber);

            // 按照集合方式回显给前端
            List<Map<String, Object>> allList = new ArrayList<>();
            for (int i = 0, size = cityLevelList.size(); i < size; i++) {
                resultMap = new HashMap<>();
                String cityLevelName = cityLevelList.get(i);
                // 城市级别下总个数
                Integer totalPoint = cityTotalPoint.get(cityLevelName);
                allTotalPoint += totalPoint;
                // 城市级别下总时间
                Float totalTime = cityTotalTime.get(cityLevelName);
                if (totalTime != null) allTotalTime += totalTime;
                // 城市级别下总金额
                Float totalMoney = cityTotalMoney.get(cityLevelName);
                if (totalMoney != null) allTotalMoney += totalMoney;
                // 城市级别下达标的个数
                Integer standardsNumber = citySatisfyNumberMap.get(cityLevelName);
                if (standardsNumber != null) allStandardsNumber += standardsNumber;
                // 城市级别下总个数
                List<Map<String, Object>> list = cityScreenMap.get(cityLevelName);
                allList.addAll(list);
                // 追加单个城市级别总统计
                resultMap.put("totalPoint", totalPoint);
                resultMap.put("time", this.setNumberPrecision(totalTime));
                resultMap.put("money", this.setNumberPrecision(totalMoney));
                resultMap.put("cityLevel", cityLevelName);
                if (pointType == 0){
                    Boolean sta = standardsNumber != null && standardsNumber == list.size();
                    allStandards = allStandards && sta;
                    resultMap.put("satisfy", sta);
                } else {
                    Boolean flag = giveAwayMap.get(cityLevelName);
                    allStandards = allStandards && flag;
                    resultMap.put("satisfy", flag == null ? false : flag);
                }
                allList.add(resultMap);// 追加到集合中
            }
            // 追加选择或是赠送总统计
            resultMap = new HashMap<>();
            resultMap.put("totalPoint", allTotalPoint);
            resultMap.put("time", this.setNumberPrecision(allTotalTime));
            resultMap.put("money", this.setNumberPrecision(allTotalMoney));
            resultMap.put("satisfy", allStandards);
            allList.add(resultMap);
            rsp.put("list",allList);
        }
        // 时长和金额
        rsp.put("totalPoint", allTotalPoint);
        rsp.put("time", this.setNumberPrecision(allTotalTime));
        rsp.put("money", this.setNumberPrecision(allTotalMoney));
        rsp.put("standards", allStandards);
        rsp.put("cartNumber", cartNumber);
        rsp.put("total", length);
        return rsp;
    }

    /**
     * 播控订单下点位详情(从数据库中获取)
     * @param cartInfoList 点位列表
     * @param pointType 点位类型
     * @param system 所属系统
     * @param param 参数  @return 响应
     */
    private Map<String,Object> getPlayCartInfoAndSaleTimeByOrder(List<Map<String, Object>> cartInfoList, Integer pointType, Integer system, Map<String, Object> param) {
        Map<String, Object> rsp = new HashMap<>();
        Integer allTotalPoint = 0;// 通过程序计算总数
        Float allTotalTime = 0.0F;// 总时间
        Float allTotalMoney = 0.0F;// 总金额
        Boolean allStandards = true; // 总达标与否
        Integer allStandardsNumber = 0;// 是否达标

        Map<String, String> cityLevelMap = new HashMap<>();// 城市级别
        Map<String, Integer> citySatisfyNumberMap = new HashMap<>(); // 判断是否达标
        Map<String, Float> cityTotalTime = new HashMap<>();// 城市级别总时长
        Map<String, Float> cityTotalMoney = new HashMap<>();// 城市级别总金额
        Map<Integer, Object[]> cityTimeMoney = new HashMap<>();// 每个城市对应的时长和金额、达标
        int length = cartInfoList.size();
        if (cartInfoList.size() > 0) {
            // 查询参数获取点位统计数据
            param.put("pointType",pointType);
            List<PointStatistics> statisticsList = this.getPointStatisticsByParam(param);
            if (statisticsList != null && statisticsList.size() > 0) {
                for (int i = 0,size = statisticsList.size(); i < size; i++) {
                    PointStatistics pointStatistics = statisticsList.get(i);
                    String adCode = pointStatistics.getCityCode();// 城市代码
                    String cityLevel = pointStatistics.getCityLevel();// 城市级别

                    Float playHours = this.setNumberPrecision(pointStatistics.getPointTime() == null ? 0.0F : pointStatistics.getPointTime());// 时长
                    Float amount = this.setNumberPrecision((pointStatistics.getPointMoney() == null || pointType == CommonEnum.PointType.GIVE_AWAY.getPointType()) ? 0.0F : pointStatistics.getPointMoney());// 金额 赠送不需要计算金额
                    Boolean satisfy = Boolean.valueOf(pointStatistics.getSatisfy());// 是否达标

                    // key:下标  value:时间、金额、是否达标
                    Object[] timeMoneySatisfy = new Object[]{playHours, amount, satisfy};
                    cityTimeMoney.put(i, timeMoneySatisfy);

                    // key:城市级别  value:总时长
                    Float totalTime = cityTotalTime.get(cityLevel);
                    if (totalTime == null) totalTime = 0.0F;
                    cityTotalTime.put(cityLevel, (totalTime + playHours));

                    // key:城市级别 value:总金额
                    Float totalMoney = cityTotalMoney.get(cityLevel);
                    if (totalMoney == null) totalMoney = 0.0F;
                    cityTotalMoney.put(cityLevel, (totalMoney + amount));

                    // key:城市代码  value:城市级别
                    cityLevelMap.put(adCode, cityLevel);

                    // 判断是否达标  key:城市级别 value:达标个数
                    /*if (satisfy) {
                        Integer number = citySatisfyNumberMap.get(cityLevel);
                        if (number == null) number = 0;
                        citySatisfyNumberMap.put(cityLevel, ++number);
                    }*/
                }
            } else {
                rsp.put("errorMessage", "获取金额发生异常！");
            }

            List<String> cityLevelList = new ArrayList<>();
            Map<String, Integer> cityTotalPoint = new HashMap<>();
            // 重新购物车构造数据
            Map<String, List<Map<String, Object>>> cityScreenMap = new HashMap<>();
            for (int i = 0; i < length; i++) {
                Map<String, Object> cartPointMap = cartInfoList.get(i);
                Object[] timeMoneySatisfy = cityTimeMoney.get(i);
                if (timeMoneySatisfy != null){
                    cartPointMap.put("time", timeMoneySatisfy[0]);
                    cartPointMap.put("money", pointType == CommonEnum.PointType.GIVE_AWAY.getPointType() ? 0.0 : timeMoneySatisfy[1]);// 赠送不需要计算金额
                    // cartPointMap.put("satisfy",pointType == CommonEnum.PointType.GIVE_AWAY.getPointType() ? null : Boolean.valueOf(timeMoneySatisfy[2].toString())); // 赠送单个城市不需要计算是否达标
                } /*else {// TODO 测试环境注释测试代码
                    cartPointMap.put("time", 12.6);
                    cartPointMap.put("money", 65.2);
                    cartPointMap.put("satisfy",true);
                }*/

                int pointNumber = Integer.valueOf(cartPointMap.get("totalpoint") + ""); // 点位个数
                String cityCode = String.valueOf(cartPointMap.get("citycode"));// 城市代码

                // 城市级别下点位个数 key:城市级别  value:点位个数
                String cityLevel = cityLevelMap.get(cityCode);
                cartPointMap.put("cityLevel",cityLevel); // 设置城市级别
                cartPointMap.put("system",system);// 设置所属系统

                // 获取总的点位个数
                Integer number = cityTotalPoint.get(cityLevel);
                if (number == null) number = 0;
                cityTotalPoint.put(cityLevel, (number + pointNumber));

                // 封装数据 key：城市级别 value：点位信息集合
                List<Map<String, Object>> list = cityScreenMap.get(cityLevel);
                if (list == null) {
                    cityLevelList.add(cityLevel);
                    list = new ArrayList<>();
                }
                list.add(cartPointMap);
                cityScreenMap.put(cityLevel, list);
            }
            // 排序
            cityLevelList.sort((city1, city2) -> city1.compareTo(city2));
            // 构造返回值
            Map<String, Object> resultMap;
            // 赠送需要判断是否满足条件
            Map<String, Boolean> giveAwayMap = null;
            if (pointType == 1) giveAwayMap = this.checkGiveAway(param.get("cartNumber") + "");

            // 按照集合方式回显给前端
            List<Map<String, Object>> allList = new ArrayList<>();
            for (int i = 0, size = cityLevelList.size(); i < size; i++) {
                resultMap = new HashMap<>();
                String cityLevelName = cityLevelList.get(i);
                // 城市级别下总个数
                Integer totalPoint = cityTotalPoint.get(cityLevelName);
                allTotalPoint += totalPoint;
                // 城市级别下总时间
                Float totalTime = cityTotalTime.get(cityLevelName);
                if (totalTime != null) allTotalTime += totalTime;
                // 城市级别下总金额
                Float totalMoney = cityTotalMoney.get(cityLevelName);
                if (totalMoney != null) allTotalMoney += totalMoney;
                // 城市级别下达标的个数
                Integer standardsNumber = citySatisfyNumberMap.get(cityLevelName);
                if (standardsNumber != null) allStandardsNumber += standardsNumber;
                // 城市级别下总个数
                List<Map<String, Object>> list = cityScreenMap.get(cityLevelName);
                allList.addAll(list);
                // 追加单个城市级别总统计
                resultMap.put("totalPoint", totalPoint);
                resultMap.put("time", this.setNumberPrecision(totalTime));
                resultMap.put("money", this.setNumberPrecision(totalMoney));
                resultMap.put("cityLevel", cityLevelName);
                /*if (pointType == 0){
                    Boolean sta = standardsNumber != null && standardsNumber == list.size();
                    allStandards = allStandards && sta;
                    resultMap.put("satisfy", sta);
                } else {
                    Boolean flag = giveAwayMap.get(cityLevelName);
                    allStandards = allStandards && flag;
                    resultMap.put("satisfy", flag == null ? false : flag);
                }*/
                allList.add(resultMap);// 追加到集合中
            }
            // 追加选择或是赠送总统计
            resultMap = new HashMap<>();
            resultMap.put("totalPoint", allTotalPoint);
            resultMap.put("time", this.setNumberPrecision(allTotalTime));
            resultMap.put("money", this.setNumberPrecision(allTotalMoney));
            resultMap.put("satisfy", allStandards);
            allList.add(resultMap);
            rsp.put("list",allList);
        }
        // 时长和金额
        rsp.put("totalPoint", allTotalPoint);
        rsp.put("time", allTotalTime);
        rsp.put("money", allTotalMoney);
        rsp.put("standards", allStandards);
        rsp.put("cartNumber", param.get("cartNumber") + "");
        rsp.put("total", length);
        return rsp;
    }

    /**
     * 播控系统
     * @param param 参数
     * @return 响应
     */
    private List<PointStatistics> getPointStatisticsByParam(Map<String, Object> param) {
        return cartPointExMapper.searchPointStatisticsByParam(param);
    }

    /**
     * 查询点位统计 放入缓存
     * @param cartNumber 购物车编号
     * @param pointType 点位类型
     * @return 响应
     */
    private List<PointStatistics> getPointStatistics(String cartNumber, Integer pointType) {
        List<PointStatistics> statisticsList;
        StringBuffer key = new StringBuffer();
        key.append(CommonConstant.CART_NUMBER).append("-").append(cartNumber).append("-STATISTTCS");
        if (pointType != null){
            key.append("-").append(pointType);
        }

        String redisResult = (String) redisUtil.get(key.toString());
        if (StringUtils.isNotBlank(redisResult)){
            statisticsList = JSON.parseArray(redisResult,PointStatistics.class);
        } else {
            statisticsList = cartPointExMapper.searchPointStatistics(cartNumber, pointType);
            if (statisticsList != null && statisticsList.size() > 0){
                redisUtil.set(key.toString(),JSON.toJSONString(statisticsList),3600L);
            }
        }

        return statisticsList;
    }

    /**
     * 添加统计
     * @param statisticsSaleSetList 计算价格统计集合
     * @param statisticsCartList 购物车集合
     * @param cartNumber 购物车编号
     * @param pointType 点位类型
     */
    private void addStatistics(List<PointStatistics> statisticsSaleSetList, List<PointStatistics> statisticsCartList, String cartNumber, CommonEnum.PointType pointType) {
        for(int i = 0,length = statisticsCartList.size();i < length;i++){
            PointStatistics pointStatistics = statisticsCartList.get(i);
            pointStatistics.setCartNumber(cartNumber);
            pointStatistics.setPointType(pointType.getPointType());

            if (statisticsSaleSetList.size() == length){
                PointStatistics saleStatistics = statisticsSaleSetList.get(i);
                if (saleStatistics != null){
                    pointStatistics.setPointTime(saleStatistics.getPointTime());
                    pointStatistics.setPointMoney(saleStatistics.getPointMoney());
                    pointStatistics.setSatisfy(saleStatistics.getSatisfy());
                    pointStatistics.setCityLevel(saleStatistics.getCityLevel());
                }
            }
        }

        this.addPointStatistics(cartNumber,statisticsCartList,pointType);
    }

    /**
     * 添加统计
     * @param statisticsCityCode   城市代码集合
     * @param cityLevelMap         城市分类集合
     * @param statisticsCityName   城市名称集合
     * @param statisticsCityTime   城市时间
     * @param statisticsCityNumber 城市点位个数
     * @param cartNumber           购物车编号
     * @param pointType            点位类型
     */
    @Transactional
    public void addStatistics(List<PointStatistics> pointStatisticsList, List<String> statisticsCityCode,
                              Map<String, String> cityLevelMap, List<String> statisticsCityName, Map<String, Float> statisticsCityTime,
                              Map<String, Integer> statisticsCityNumber, String cartNumber, int pointType) {
        PointStatistics pointStatistics;
        for (int i = 0, length = statisticsCityCode.size(); i < length; i++) {
            pointStatistics = new PointStatistics();
            String cityCode = statisticsCityCode.get(i);
            String cityLevel = cityLevelMap.get(cityCode);
            String cityName = statisticsCityName.get(i);
            Float cityTime = statisticsCityTime.get(cityCode);
            Integer cityNumber = statisticsCityNumber.get(cityCode);

            pointStatistics.setCartNumber(cartNumber);
            pointStatistics.setCityCode(cityCode);
            pointStatistics.setCityLevel(cityLevel);
            pointStatistics.setCityName(cityName);
            pointStatistics.setPointNumber(cityNumber);
            pointStatistics.setPointTime(cityTime);
            pointStatistics.setPointType(pointType);

            pointStatisticsList.add(pointStatistics);
        }
    }

    /**
     * 调用计算价格接口
     *
     * @param cartInfoList 参数
     * @return 响应
     */
    private JSONArray getSaleParam(List<Map<String, Object>> cartInfoList) {
        JSONArray params = new JSONArray();
        JSONObject param;
        for (int i = 0, length = cartInfoList.size(); i < length; i++) {
            Map<String, Object> cartPointMap = cartInfoList.get(i);
            try {
                param = new JSONObject();
                param.put("adCode", String.valueOf(cartPointMap.get("citycode")));// 城市代码
                param.put("count", Integer.valueOf(cartPointMap.get("totalpoint") + ""));// 点位个数
                param.put("duration", Integer.valueOf(cartPointMap.get("timelength") + ""));// 刊位时长
                param.put("endDate", Long.valueOf(cartPointMap.get("endtime") + ""));// 结束时间
                param.put("startDate", Long.valueOf(cartPointMap.get("starttime") + ""));// 开始时间
                param.put("repeat", Integer.valueOf(cartPointMap.get("number") + ""));// 刊位个数
                param.put("mediumType", 3);
                params.add(param);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return params;
    }

    /**
     * 根据城市进行删除
     *
     * @param req 请求操作
     * @return 响应
     */
    @Override
    @Transactional
    public MessageRsp deleteByCity(CartScreenInfoReq req) {
        Map<String,Object> param = new HashMap<>();
        StringBuilder deleteParam = this.checkDeleteParam(req,param);
        if (StringUtils.isNotBlank(deleteParam.toString())) return MessageUtil.error(deleteParam.toString());

        Integer deleteNumber = cartPointExMapper.deleteCartPoint(param);
        logger.info("删除城市下点位{}条",deleteNumber);
        if (deleteNumber < 1){
            return MessageUtil.error("删除失败！");
        }
        // 删除缓存
        redisUtil.batchDelete(new StringBuilder(CommonConstant.CART_NUMBER).append("-").append(req.getCartNumber()).toString());
        return MessageUtil.success();
    }

    /**
     * 播控中 根据城市进行删除
     * 如果删除是销控选择的点位 不能操作
     * @param req 请求操作
     * @return 响应
     */
    @Override
    @Transactional
    public MessageRsp playDeleteByCity(CartScreenInfoReq req) {
        Map<String,Object> param = new HashMap<>();
        StringBuilder deleteParam = this.checkDeleteParam(req,param);
        if (StringUtils.isNotBlank(deleteParam.toString())) return MessageUtil.error(deleteParam.toString());

        // 默认只能删除播控中自己选中的点位
        param.put("system",CommonEnum.SystemType.PLAY.getSystemType());
        Integer deleteNumber = cartPointExMapper.deleteCartPoint(param);
        logger.info("删除播控系统城市下点位{}条",deleteNumber);
        if (deleteNumber < 1){
            return MessageUtil.error("删除失败！");
        }
        // 删除缓存
        redisUtil.batchDelete(new StringBuilder(CommonConstant.CART_NUMBER).append("-").append(req.getCartNumber()).toString());
        return MessageUtil.success();
    }

    /**
     * 选中屏资产编号进行删除
     */
    @Override
    @Transactional
    public MessageRsp deleteBySelectScreen(CartScreenInfoReq req) {
        Map<String,Object> param = new HashMap<>();
        StringBuilder deleteParam = this.checkDeleteParam(req,param);
        if (StringUtils.isNotBlank(deleteParam.toString())) return MessageUtil.error(deleteParam.toString());

        List<String> screenList = req.getScreenList();
        if (screenList == null || screenList.size() == 0) return MessageUtil.error("请选择要删除的点位！");
        param.put("screenList",screenList);

        Integer deleteNumber = cartPointExMapper.deleteCartPoint(param);
        logger.info("成功删除选中点位{}条数据", deleteNumber);
        if (deleteNumber < 1){
            return MessageUtil.error("删除失败！");
        }
        // 删除缓存
        redisUtil.batchDelete(new StringBuilder(CommonConstant.CART_NUMBER).append("-").append(req.getCartNumber()).toString());
        return MessageUtil.success();
    }

    /**
     * 播控系统  选中屏资产编号进行删除
     * @param req 请求
     * @return 响应
     */
    @Override
    @Transactional
    public MessageRsp playDeleteBySelectScreen(CartScreenInfoReq req) {
        Map<String,Object> param = new HashMap<>();
        StringBuilder deleteParam = this.checkDeleteParam(req,param);
        if (StringUtils.isNotBlank(deleteParam.toString())) return MessageUtil.error(deleteParam.toString());

        List<String> screenList = req.getScreenList();
        if (screenList == null || screenList.size() == 0) return MessageUtil.error("请选择要删除的点位！");
        param.put("screenList",screenList);
        param.put("system",CommonEnum.SystemType.PLAY.getSystemType());// 只能删除播控下点位

        Integer deleteNumber = cartPointExMapper.deleteCartPoint(param);
        logger.info("成功删除播控系统选中点位{}条数据", deleteNumber);
        if (deleteNumber < 1){
            return MessageUtil.error("删除失败！");
        }
        // 删除缓存
        redisUtil.batchDelete(new StringBuilder(CommonConstant.CART_NUMBER).append("-").append(req.getCartNumber()).toString());
        return MessageUtil.success();
    }

    /**
     * 批量删除全部赠送点位
     * @param cartNumber 购物车编号
     * @return 响应
     */
    @Override
    @Transactional
    public MessageRsp deleteAllPointType(String cartNumber) {
        if (StringUtils.isBlank(cartNumber)) return MessageUtil.error("购物车编号为空！");
        Map<String,Object> param = new HashMap<>();
        param.put("cartNumber",cartNumber);
        param.put("pointType",CommonEnum.PointType.GIVE_AWAY.getPointType());

        Integer deleteNumber = cartPointExMapper.deleteAllPointType(param);
        logger.info("删除赠送点位{}条",deleteNumber);
        if (deleteNumber < 1){
            return MessageUtil.error("删除失败！");
        }

        // 删除缓存
        redisUtil.batchDelete(new StringBuilder(CommonConstant.CART_NUMBER).append("-").append(cartNumber).toString());
        redisUtil.delete(new StringBuilder().append(CommonConstant.GIVE_AWAY).append("-").append(cartNumber).toString());
        return MessageUtil.success();
    }

    /**
     * 播控系统 批量删除全部点位(废弃)
     * @param cartNumber 购物车编号
     * @return 响应
     */
    @Override
    @Transactional
    public MessageRsp playDeleteAllPointType(String cartNumber) {
        if (StringUtils.isBlank(cartNumber)) return MessageUtil.error("购物车编号为空！");
        Map<String,Object> param = new HashMap<>();
        param.put("cartNumber",cartNumber);
        param.put("pointType",CommonEnum.PointType.CHOOSE.getPointType());
        param.put("system",CommonEnum.SystemType.PLAY.getSystemType());

        Integer deleteNumber = cartPointExMapper.deleteAllPointType(param);
        logger.info("删除播控系统赠送点位{}条",deleteNumber);
        if (deleteNumber < 1){
            return MessageUtil.error("删除失败！");
        }

        // 删除缓存
        redisUtil.batchDelete(new StringBuilder(CommonConstant.CART_NUMBER).append("-").append(cartNumber).toString());
        redisUtil.delete(new StringBuilder().append(CommonConstant.GIVE_AWAY).append("-").append(cartNumber).toString());
        return MessageUtil.success();
    }

    /**
     * 销控 合同附件中点位详情
     * @param cartNumber 购物车编号
     * @return 响应
     */
    @Override
    public MessageRsp getStatistics(String cartNumber) {
        if (StringUtils.isBlank(cartNumber)) return MessageUtil.error("购物车编号不能为空！");
        List<PointStatistics> pointStatistics = this.getPointStatistics(cartNumber, CommonEnum.PointType.CHOOSE.getPointType());
        pointStatistics = pointStatistics.stream().filter(statistics -> statistics.getSystem() == CommonEnum.SystemType.SALE.getSystemType()).collect(Collectors.toList());// 过滤
        return MessageUtil.success(pointStatistics);
    }

    /**
     * 合同附件点位统计
     * @param cartNumber 购物车编号
     * @return 响应
     */
    @Override
    public MessageRsp contract(String cartNumber) {
        if (StringUtils.isBlank(cartNumber)) return MessageUtil.error("购物车编号不能为空！");
        Map<String,Object> param = new HashMap<>();
        param.put("cartNumber",cartNumber);
        param.put("pointType",CommonEnum.PointType.CHOOSE.getPointType());
        param.put("system",CommonEnum.SystemType.SALE.getSystemType());
        List<PointStatistics> pointStatistics = cartPointExMapper.contract(param);
        List<ContractCartNumberRsp> rspList = new ArrayList<>();
        if (pointStatistics != null && pointStatistics.size() > 0){
            Map<String, List<PointStatistics>> timePointStatistics = pointStatistics.stream().collect(Collectors.groupingBy(statistics -> (statistics.getStartTime() + "-" + statistics.getEndTime()))); // 相同时间段分组
            Map<String, Double> totalTime = pointStatistics.stream().collect(Collectors.groupingBy(statistics -> (statistics.getStartTime() + "-" + statistics.getEndTime()), Collectors.summingDouble(PointStatistics::getPointTime)));// 相同时间段下时长
            // 循环遍历数据
            timePointStatistics.forEach((key,list) -> {
                ContractCartNumberRsp contractCartNumberRsp = new ContractCartNumberRsp();
                String[] startAndEnd = key.split("-");
                contractCartNumberRsp.setStartTime(Long.valueOf(startAndEnd[0])); // 开始时间
                contractCartNumberRsp.setEndTime(Long.valueOf(startAndEnd[1]));// 结束时间

                List<ContractTimeAndNumberRsp> contractTimeAndNumberList = new ArrayList<>();// 合同附件时长和刊位个数集合
                List<String> timeLengthAndNumberList = list.stream().map(statistics -> statistics.getTimeLength() + "-" + statistics.getNumber()).distinct().collect(Collectors.toList());
                timeLengthAndNumberList.forEach(str ->{
                    ContractTimeAndNumberRsp rsp = new ContractTimeAndNumberRsp();
                    String[] timeAndNumber = str.split("-");
                    rsp.setTimeLength(Integer.valueOf(timeAndNumber[0]));
                    rsp.setNumber(Integer.valueOf(timeAndNumber[1]));
                    contractTimeAndNumberList.add(rsp);
                });
                contractCartNumberRsp.setTimeAndNumberList(contractTimeAndNumberList);

                List<ContractCityTotalRsp> contractCityTotalList = new ArrayList<>();// 合同附件中城市和点位集合
                Map<String, Integer> cityAndTotal = list.stream().collect(Collectors.groupingBy(PointStatistics::getCityName, Collectors.summingInt(PointStatistics::getPointNumber)));// 城市下总点位个数
                cityAndTotal.forEach((city,value) -> {
                    ContractCityTotalRsp rsp = new ContractCityTotalRsp();
                    rsp.setCityName(city);
                    rsp.setTotal(value);
                    contractCityTotalList.add(rsp);
                });
                contractCartNumberRsp.setCityTotalRsp(contractCityTotalList);
                contractCartNumberRsp.setTime(totalTime.get(key));// 总时长

                List<String[]> screen = list.stream().map(statistics -> statistics.getScreenName().split(",")).distinct().collect(Collectors.toList());// 相同时间段下屏幕发布形式(去重)
                List<Integer> screenList = new ArrayList<>();
                screen.forEach(dev -> {
                    screenList.addAll(Stream.of(dev).map(str -> Integer.valueOf(str)).distinct().collect(Collectors.toList()));
                });
                List<Integer> devTypes = screenList.stream().distinct().collect(Collectors.toList());
                contractCartNumberRsp.setDevType(devTypes);

                rspList.add(contractCartNumberRsp);
            });
        }
        return MessageUtil.success(rspList);
    }

    /**
     * 拆分销控订单中购物车
     * @param cartNumber 购物车编号
     * @return 响应
     */
    @Override
    public MessageRsp splitCart(String cartNumber) {
        if (StringUtils.isBlank(cartNumber)) return MessageUtil.error("购物车编号不能为空！");
        Map<String,Object> param = new HashMap<>();
        param.put("cartNumber",cartNumber);
        List<Map<String, Object>> splitCart = cartPointExMapper.splitCart(param);
        // 拆分数据
        List<SplitCartRsp> listRsp = this.createPlayScreenData(splitCart);
        return MessageUtil.success(listRsp);
    }

    /**
     * 播控获取点位信息
     * @param req 请求参数
     * @return 响应
     */
    @Override
    public MessageRsp playSplitCart(CartScreenInfoReq req) {
        Map<String,Object> param = new HashMap<>();
        String errorMsg = this.getPlayParam(param, req);
        if (StringUtils.isNotBlank(errorMsg)) return MessageUtil.error(errorMsg);
        List<Map<String, Object>> splitCart = cartPointExMapper.splitCart(param);
        // 拆分数据
        List<SplitCartRsp> listRsp = this.createPlayScreenData(splitCart);
        return MessageUtil.success(listRsp);
    }

    /**
     * 取消订单(将购物车中所选点位状态设置为0)
     * @param cartNumber 购物车编号
     * @return 响应
     */
    @Override
    @Transactional
    public MessageRsp cancel(String cartNumber) {
        if (StringUtils.isBlank(cartNumber)) return MessageUtil.error("购物车编号有误！");
        Integer cancelNumber = cartPointExMapper.cancel(cartNumber);
        if (cancelNumber != null && cancelNumber > 0){
            logger.info("成功取消购物车编号【{}】下{}条数据",cartNumber,cancelNumber);
        } else {
            logger.error("取消无数据！");
            return MessageUtil.error("取消失败！");
        }
        return MessageUtil.success();
    }

    /**
     * 删除购物车中无用的数据
     * @param system 系统
     * @param cartNumberList 购物车编号集合
     * @return 响应
     */
    @Override
    public MessageRsp deleteInvalidPointCart(Integer system, List<String> cartNumberList) {
        if (system == null) return MessageUtil.error("系统不能为空！");
        Map<String,Object> param = new HashMap<>();
        param.put("system",system);
        param.put("cartNumberList",cartNumberList);
        Integer invalidNumber = cartPointExMapper.deleteInvalidPointCart(param);
        logger.info("成功删除{}系统点位个数{}",system,invalidNumber);
        return null;
    }

    /**
     * 拆分购物车给播控
     * @param splitCart 点位集合
     * @return 响应
     */
    private List<SplitCartRsp> createPlayScreenData(List<Map<String, Object>> splitCart) {
        List<SplitCartRsp> listRsp = new ArrayList<>();
        splitCart.forEach(map -> {
            SplitCartRsp splitCartRsp = new SplitCartRsp();
            Integer number = Integer.valueOf(map.get("number") + "");
            Integer timeLength = Integer.valueOf(map.get("time_length") + "");
            Long startTime = Long.valueOf(map.get("start_time") + "");
            Long endTime = Long.valueOf(map.get("end_time") + "");
            String screenNumberStr = map.get("screen_number") + "";
            String screenTypeStr = map.get("screen_type") + "";
            List<String> screenNumberList = Stream.of(screenNumberStr.split(",")).collect(Collectors.toList());
            List<String> screenTypeList = Stream.of(screenTypeStr.split(",")).collect(Collectors.toList());
            Map<String,List<String>> typeScreenMap = new HashMap<>();
            for(int i = 0,length = screenTypeList.size();i < length;i++){
                String type = screenTypeList.get(i);
                String screenNumber = screenNumberList.get(i);
                List<String> list = typeScreenMap.get(type);
                if (list == null) list = new ArrayList<>();
                list.add(screenNumber);
                typeScreenMap.put(type,list);
            }

            List<Map<String,Object>> typeScreenList = new ArrayList<>();
            typeScreenMap.forEach((key,list) ->{
                Map<String,Object> screenMap = new HashMap<>();
                screenMap.put("pointType",Integer.valueOf(key));
                screenMap.put("adPoint",list.stream().collect(Collectors.joining(",")));
                typeScreenList.add(screenMap);
            });

            splitCartRsp.setNumber(number);
            splitCartRsp.setTimeLength(timeLength);
            splitCartRsp.setStartTime(startTime);
            splitCartRsp.setEndTime(endTime);
            splitCartRsp.setTypeScreen(typeScreenList);
            listRsp.add(splitCartRsp);
        });
        return listRsp;
    }

    /**
     * 验证删除参数是否满足条件
     *
     * @param req 参数
     * @param param 查询参数
     * @return 响应
     */
    private StringBuilder checkDeleteParam(CartScreenInfoReq req, Map<String, Object> param) {
        StringBuilder paramSb = new StringBuilder();
        String cartNumber = req.getCartNumber();// 购物车编号
        param.put("cartNumber",cartNumber);
        if (StringUtils.isBlank(cartNumber)) paramSb.append("购物车编号不能为空！");

        String cityName = req.getCityName();
        param.put("cityName",cityName);
        if (StringUtils.isBlank(cityName)) paramSb.append("删除城市不能为空！");

        Integer pointType = req.getPointType();
        param.put("pointType",pointType);
        if (pointType == null) paramSb.append("点位类型不能为空！");

        // 如果城市名称不为空  表示是删除该购物车下城市所有选定的屏信息
        Integer timeLength = req.getTimeLength();
        param.put("timeLength",timeLength);
        if (timeLength == null) paramSb.append("时长参数有误！");

        Long startTime = req.getStartTime();
        param.put("startTime",startTime);
        if (startTime == null) paramSb.append("开始时间参数有误！");

        Long endTime = req.getEndTime();
        param.put("endTime",endTime);
        if (endTime == null) paramSb.append("结束时间参数有误！");

        Integer number = req.getNumber();
        param.put("number",number);
        if (number == null) paramSb.append("刊位个数有误！");
        return paramSb;
    }

    /**
     * 查询购物车中屏编号
     */
    private List<String> getCartScreenNumberList(String cartNumber, Integer system, Integer pointType,
                                                 String cityName, Integer number, Integer timeLength,
                                                 Long startTime, Long endTime) {
        List<CartPoint> cartPointList = searchCartPointList(cartNumber, system, pointType, cityName, number, timeLength, startTime, endTime);
        List<String> screenList = cartPointList.stream().map(CartPoint::getScreenNumber).collect(Collectors.toList());
        return screenList;
    }

    /**
     * 查询响应的id
     *
     * @return 响应
     */
    private List<Integer> getCartPointIds(String cartNumber, Integer system, Integer pointType,
                                          String cityName, Integer number, Integer timeLength,
                                          Long startTime, Long endTime) {
        List<CartPoint> cartPointList = searchCartPointList(cartNumber, system, pointType, cityName, number, timeLength, startTime, endTime);
        List<Integer> cartPointIds = cartPointList.stream().map(CartPoint::getCartPointId).collect(Collectors.toList());
        return cartPointIds;
    }

    /**
     * 查询满足条件的购物车数据
     */
    private List<CartPoint> searchCartPointList(String cartNumber, Integer system, Integer pointType,
                                                String cityName, Integer number, Integer timeLength,
                                                Long startTime, Long endTime) {
        List<CartPoint> cartPointList;
        StringBuilder sb = new StringBuilder(CommonConstant.CART_NUMBER);
        Map<String, Object> param = new HashMap<>();
        if (StringUtils.isNotBlank(cartNumber)) {
            param.put("cartNumber", cartNumber);
            sb.append("-").append(cartNumber);
        }
        if (system != null) {
            param.put("system", system);
            sb.append("-").append(system);
        }
        if (pointType != null) {
            param.put("pointType", pointType);
            sb.append("-").append(pointType);
        }
        if (StringUtils.isNotBlank(cityName)) {
            param.put("cityName", cityName);
            sb.append("-").append(cityName);
        }
        if (number != null) {
            param.put("number", number);
            sb.append("-").append(number);
        }
        if (timeLength != null) {
            param.put("timeLength", timeLength);
            sb.append("-").append(timeLength);
        }
        if (startTime != null) {
            param.put("startTime", startTime);
            sb.append("-").append(startTime);
        }
        if (endTime != null) {
            param.put("endTime", endTime);
            sb.append("-").append(endTime);
        }

        // 缓存中取数据
        String cartPointStr = (String) redisUtil.get(sb.toString());
        if (StringUtils.isNotBlank(cartPointStr)) {
            cartPointList = JSON.parseArray(cartPointStr, CartPoint.class);
        } else {
            cartPointList = cartPointExMapper.searchCartPointList(param);
            if (cartPointList != null && cartPointList.size() > 0)
                redisUtil.set(sb.toString(), JSON.toJSONString(cartPointList), CommonConstant.EXP_TIME);
        }
        return cartPointList;
    }

    /**
     * 获取城市下屏信息详情
     */
    @Override
    public MessageRsp cartScreenInfo(SearchScreenMediaInfoReq req) {
        String cartNumber = req.getCartNumber();// 购物车编号
        if (StringUtils.isBlank(cartNumber)) return MessageUtil.error("购物车编号不能为空");

        String cityName = req.getCityName();
        if (StringUtils.isBlank(cityName)) return MessageUtil.error("请选择需要查看的城市详情！");

        Integer timeLength = req.getTimeLength();
        if (timeLength == null) return MessageUtil.error("时长参数异常！");

        Long startTime = req.getStartTime();
        if (startTime == null) return MessageUtil.error("开始时间参数异常！");

        Long endTime = req.getEndTime();
        if (endTime == null) return MessageUtil.error("结束时间参数异常！");

        Integer pointType = req.getPointType();
        if (pointType == null) return MessageUtil.error("点位类型参数异常！");

        Integer number = req.getNumber();
        if (number == null) return MessageUtil.error("刊位个数参数异常！");

        // 查询当前购物车下当前城市选中的屏编号信息
        List<String> screenNumberListList = this.getCartScreenNumberList(cartNumber, req.getSystem(), pointType, cityName, number, timeLength, startTime, endTime);
        if (screenNumberListList == null || screenNumberListList.size() == 0){
            screenNumberListList = new ArrayList<>();
            screenNumberListList.add("-1");
        }

        req.setScreenNumberList(screenNumberListList);
        req.setSelectMode(null);// 处理：设置为1表示不会去排除不满足时间条件的数据，可能存在这条数据被其他订单预定而时间刚好无法满足，
                            // 导致查询出数据出现异常
        req.setDetail(true);
        List<ScreenMediaInfoRsp> screenMediaInfoList = cartPointExMapper.searchScreenMediaInfo(req);

        Map<String, Object> map = new HashMap<>();
        if (screenMediaInfoList != null) {
            map.put("screenList", screenMediaInfoList);
            map.put("total", screenMediaInfoList.size());
        }
        return MessageUtil.success(map);
    }

    /**
     * 销控确认订单(加锁)
     *
     * @param cartNumber 购物车编号
     * @param req      请求
     * @return 响应
     */
    @Override
    @Transactional
    public synchronized MessageRsp confirmOrder(String cartNumber, JSONObject req) {
        if (StringUtils.isBlank(cartNumber)) return MessageUtil.error("购物车编号不能为空！");
        List<CartPoint> cartPointList = this.searchCartPointList(cartNumber, null, null, null, null, null, null, null);
        if (cartPointList == null || cartPointList.size() == 0) return MessageUtil.error("该购物车中没有任何选中的屏数据！");

        // 试投订单不验证
        Integer orderType = cartPointList.get(0).getOrderType();
        if (orderType != CommonEnum.OrderType.TRY.getOrderType()){
            // 验证能否提交 赠送条件是否存在等
            Integer system = CommonEnum.SystemType.SALE.getSystemType();
            Map<String, Integer> systemReq = new HashMap<>();
            systemReq.put("system",system);
            // 判断赠送条件
            String errorMsg = this.checkGiveAwayParam(cartNumber, systemReq);
            String check = errorMsg; // 是否需要验证赠送
            if (StringUtils.isNotBlank(errorMsg) && !"check".equals(check)) return MessageUtil.error(errorMsg);

            // 销控系统 验证选择点位是否满足条件
            if (system == CommonEnum.SystemType.SALE.getSystemType()){
                errorMsg = this.checkChoose(cartNumber);
                if (StringUtils.isNotBlank(errorMsg)) return MessageUtil.error(errorMsg + "未达标！");
            }

            // 存在赠送点位需要验证是否满足赠送比例
            if ("check".equals(check)){
                //验证时间段是否满足条件(销控)
                if (system == CommonEnum.SystemType.SALE.getSystemType()){
                    errorMsg = this.checkDate(cartNumber);
                    if (StringUtils.isNotBlank(errorMsg)) return MessageUtil.error(errorMsg);
                }
                // 判断是否有不满足赠送条件的点位
                Map<String, Boolean> cityLevelMap = this.checkGiveAway(cartNumber);
                if (cityLevelMap != null && cityLevelMap.size() != 0){
                    String refuseCityLevel = this.getRefuseCityLevel(cityLevelMap);
                    if (StringUtils.isNotBlank(refuseCityLevel)) return MessageUtil.error(refuseCityLevel + "不满足赠送标准！");
                }
            }
        }

        // 1、验证当前购物车中的数据是否还是满足时间段条件  正式订单进行判断
        Integer state = req.getInteger("state");
        if (state == CommonEnum.StateType.FORMAL.getStateType()) {
            long commitTime = Clock.systemUTC().millis() / 1000;
            Map<String,Object> selectParam = new HashMap<>();
            selectParam.put("cartNumber",cartNumber);
            selectParam.put("system",CommonEnum.SystemType.SALE.getSystemType());
            selectParam.put("state",CommonEnum.StateType.DRAFT.getStateType());// 只修改草稿状态下的数据
            List<Map<String, Object>> groupCartInfo = cartPointExMapper.getCartInfoByOrder(selectParam);
            // 针对选择的点位判断时长是否满足(所有点位都需要计算时间)
            // List<Map<String, Object>> chooseGroupCartInfo = groupCartInfo.stream().filter(map -> getPointType(map.get("pointtype")) == 0).collect(Collectors.toList());
            for (Map<String, Object> map : groupCartInfo) {
                Integer timeLength = Integer.valueOf(map.get("timelength") + "");
                Integer number = Integer.valueOf(map.get("number") + "");
                Long startTime = Long.valueOf(map.get("starttime") + "");
                Long endTime = Long.valueOf(map.get("endtime") + "");
                List<String> screenNumberList = Stream.of((map.get("screennumberarr") + "").split(",")).collect(Collectors.toList());
                // String cartPointIdArr = map.get("cartpointid") + "";
                List<Integer> cartPointIdList = Stream.of((map.get("cartpointid") + "").split(",")).map(Integer::valueOf).collect(Collectors.toList());
                // 如果在当前条件下不存在不能添加点位的数据
                List<String> refuseScreenNumberList = this.refuseScreenNumberList(screenNumberList, null, null, startTime, endTime, number, timeLength);
                if (refuseScreenNumberList == null || refuseScreenNumberList.size() == 0) {
                    /*Map<String, Object> param = new HashMap<>();
                    param.put("cartPointIdArr",cartPointIdArr);
                    Integer updateNumber = cartPointExMapper.updateCartPointState(param);*/
                    int updateNumber = this.batchModifyState(cartPointIdList,commitTime);
                    logger.info("成功修改【{}】【{}】【{}】【{}】状态{}条", timeLength,number,startTime,endTime,updateNumber);
                } else {
                    logger.error("存在点位信息不满足时间条件！" + JSON.toJSONString(refuseScreenNumberList));
                    throw new GlobalException(CommonEnum.Message.ERROR.getCode(), "存在点位信息不满足时间条件！" + JSON.toJSONString(refuseScreenNumberList));
                }
            }
        }

        // 2、先清空之前的订单中的数据
       /* Map<String, Object> deleteParam = new HashMap<>();
        deleteParam.put("table", "t_order_cart_point");
        deleteParam.put("cartNumber", cartNumber);
        Integer emptyNumber = cartPointExMapper.emptyTable(deleteParam);
        logger.info("购物车编号【{}】删除{}数据库中{}条数据", "t_order_cart_point", cartNumber, emptyNumber);
        // 3、向订单中存放数据
        Map<String, Object> param = new HashMap<>();
        param.put("cartPointList", cartPointList);
        param.put("state", state);// 订单状态
        Integer addOrderNumber = cartPointExMapper.confirmOrder(param);
        logger.info("购物车编号【{}】成功添加{}到订单中，订单状态{}", cartNumber, addOrderNumber, state);*/

        // 删除缓存
        redisUtil.batchDelete(new StringBuilder(CommonConstant.CART_NUMBER).append("-").append(cartNumber).toString());
        return MessageUtil.success();
    }

    /**
     * 播控确认订单(加锁)
     *
     * @param cartNumber 购物车编号
     * @param req      请求
     * @return 响应
     */
    @Override
    @Transactional
    public MessageRsp playConfirmOrder(String cartNumber, CartScreenInfoReq req) {
        Map<String,Object> param = new HashMap<>();
        req.setCartNumber(cartNumber);
        String errorParam = this.getPlayParam(param, req);
        if (StringUtils.isNotBlank(errorParam)) return MessageUtil.error(errorParam);

        List<CartPoint> cartPointList = this.searchCartPointList(cartNumber, null, null, null, null, null, null, null);
        if (cartPointList == null || cartPointList.size() == 0) return MessageUtil.error("该购物车中没有任何选中的屏数据！");

        CartPoint cartPoint = cartPointList.get(0);
        Integer playMode = cartPoint.getPlayMode();
        // 如果不是超级模式 并且 不是插入的方式
        if (playMode != CommonEnum.PlayMode.INSERT.getPlayType()){
            // 1、验证当前购物车中的 播控点位 数据是否还是满足时间段条件  正式订单进行判断
            param.put("system",CommonEnum.SystemType.PLAY.getSystemType());
            param.put("state",CommonEnum.StateType.DRAFT.getStateType());
            long commitTime = Clock.systemUTC().millis() / 1000;
            List<Map<String, Object>> groupCartInfo = cartPointExMapper.getCartInfoByOrder(param);
            for (Map<String, Object> map : groupCartInfo) {
                Integer timeLength = Integer.valueOf(map.get("timelength") + "");
                Integer number = Integer.valueOf(map.get("number") + "");
                Long startTime = Long.valueOf(map.get("starttime") + "");
                Long endTime = Long.valueOf(map.get("endtime") + "");
                List<String> screenNumberList = Stream.of((map.get("screennumberarr") + "").split(",")).collect(Collectors.toList());
                List<Integer> cartPointIdList = Stream.of((map.get("cartpointid") + "").split(",")).map(Integer::valueOf).collect(Collectors.toList());
                // 如果在当前条件下不存在不能添加点位的数据
                List<String> refuseScreenNumberList = this.refuseScreenNumberList(screenNumberList, null, null, startTime, endTime, number, timeLength);
                if (refuseScreenNumberList == null || refuseScreenNumberList.size() == 0) {
                    Integer updateNumber = this.batchModifyState(cartPointIdList,commitTime);
                    // Map<String, Object> modifyParam = new HashMap<>();
                    // if (StringUtils.isNotBlank(cartPointIdArr)) modifyParam.put("cartPointIdArr",cartPointIdArr);
                    logger.info("成功修改【{}】【{}】【{}】【{}】状态{}条", timeLength,number,startTime,endTime,updateNumber);
                } else {
                    throw new GlobalException(500, "存在点位信息不满足时间条件！" + JSON.toJSONString(refuseScreenNumberList));
                }
            }
        }
        // 删除缓存
        redisUtil.batchDelete(new StringBuilder(CommonConstant.CART_NUMBER).append("-").append(cartNumber).toString());
        return MessageUtil.success();
    }

    /**
     * 批量修改状态
     * @param cartPointIdList 点位集合
     * @return 响应
     */
    public int batchModifyState(List<Integer> cartPointIdList,long commitTime) {
        long begin = System.currentTimeMillis();
        int updateNumber = 0;
        int length = cartPointIdList.size();
        int batchNumber = length % CommonConstant.BATCH_NUMBER == 0 ? length / CommonConstant.BATCH_NUMBER : (length / CommonConstant.BATCH_NUMBER) + 1;
        for (int i = 1; i <= batchNumber;i++){
            List<Integer> batchModifyIdList = cartPointIdList.stream().skip((i - 1) * CommonConstant.BATCH_NUMBER).limit(CommonConstant.BATCH_NUMBER).collect(Collectors.toList());
            Map<String, Object> modifyParam = new HashMap<>();
            modifyParam.put("commitTime",commitTime);
            modifyParam.put("cartPointIdArr",batchModifyIdList.stream().map(id -> id + "").collect(Collectors.joining(",")));
            updateNumber += cartPointExMapper.updateCartPointState(modifyParam);
        }
        logger.info("用时：{}",(System.currentTimeMillis() - begin));
        return updateNumber;
    }

    /**
     * 播控系统参数
     * @param param 参数
     * @param req 请求参数
     * @return 响应
     */
    private String getPlayParam(Map<String, Object> param, CartScreenInfoReq req) {
        String cartNumber = req.getCartNumber();
        if (StringUtils.isBlank(cartNumber)) return "购物车编号不能为空！";
        param.put("cartNumber",cartNumber);

        Integer number = req.getNumber();
        if (number == null) return "刊位个数不能为空！";
        param.put("number",number);

        Integer timeLength = req.getTimeLength();
        if (timeLength == null) return "刊位时长不能为空！";
        param.put("timeLength",timeLength);

        Long startTime = req.getStartTime();
        if (startTime == null) return "起始时间不能为空！";
        param.put("startTime",startTime);

        Long endTime = req.getEndTime();
        if (endTime == null) return "结束时间不能为空！";
        param.put("endTime",endTime);
        return null;
    }

    /**
     * 获取不满足提交的城市级别
     * @param cityLevelMap 城市级别map
     * @return 响应
     */
    private String getRefuseCityLevel(Map<String, Boolean> cityLevelMap) {
        List<String> errorCityList = new ArrayList<>();
        Set<Map.Entry<String, Boolean>> entries = cityLevelMap.entrySet();
        for (Map.Entry<String, Boolean> entry : entries){
            String key = entry.getKey();
            Boolean value = entry.getValue();
            if (!value) errorCityList.add(key);
        }
        return errorCityList.stream().collect(Collectors.joining(","));
    }

    /**
     * 验证选中的是否还满足添加到订单条件
     *
     * @param cartPointList 点位集合
     * @return 响应
     */
    private List<String> checkHasConfirmOrder(List<CartPoint> cartPointList) {
        CartPoint cartPoint = cartPointList.get(0);
        String cartNumber = cartPoint.getCartNumber();// 购物车编号
        Long startTime = cartPoint.getStartTime();// 开始时间
        Long endTime = cartPoint.getEndTime();// 结束时间
        Integer number = cartPoint.getNumber();// 个数
        Integer timeLength = cartPoint.getTimeLength();// 时长

        List<String> screenNumberList = new ArrayList<>();
        screenNumberList.add("-1");
        for (int i = 0, length = cartPointList.size(); i < length; i++) {
            screenNumberList.add(cartPointList.get(i).getScreenNumber());
        }

        return refuseScreenNumberList(screenNumberList, null, cartNumber, startTime, endTime, number, timeLength);
    }

    /**
     * 根据条件查询能否进行添加
     *
     * @param cartNumber       购物车编号
     * @param screenNumberList 屏集合
     * @param cartPointIdList  购物车点位集合
     * @param startTime        开始时间
     * @param endTime          结束时间
     * @param number           刊位个数
     * @param timeLength       刊位时长     @return
     */
    private List<String> refuseScreenNumberList(List<String> screenNumberList, List<Integer> cartPointIdList,
                                                String cartNumber, Long startTime, Long endTime,
                                                Integer number, Integer timeLength) {
        Map<String, Object> param = new HashMap<>();
        if (StringUtils.isNotBlank(cartNumber)) param.put("cartNumber", cartNumber);
        param.put("startTime", startTime);
        param.put("endTime", endTime);
        param.put("number", number);
        param.put("timeLength", timeLength);
        param.put("totalTime", totalTime);
        if (screenNumberList != null && screenNumberList.size() > 0) param.put("screenNumberList", screenNumberList);
        if (cartPointIdList != null && cartPointIdList.size() > 0) param.put("cartPointIdList", cartPointIdList);

        List<String> refuseScreenNumberList = cartPointExMapper.checkHasConfirmOrder(param);
        if (screenNumberList != null && screenNumberList.size() > 0){
            refuseScreenNumberList = refuseScreenNumberList.stream().filter(screenNumberList::contains).collect(Collectors.toList());
        }
        return refuseScreenNumberList;
    }

    /**
     * 编辑订单 将订单中数据copy到购物车中(废弃)
     */
    @Override
    @Transactional
    public synchronized MessageRsp compilePointCart(String cartNumber) {
        if (StringUtils.isBlank(cartNumber)) return MessageUtil.error("购物车编号不能为空");
        // 1、删除购物车中历史数据
        Map<String, Object> deleteParam = new HashMap<>();
        deleteParam.put("table", "t_cart_point");
        deleteParam.put("cartNumber", cartNumber);
        Integer emptyNumber = cartPointExMapper.emptyTable(deleteParam);
        logger.info("购物车编号【{}】删除{}数据库中{}条数据", cartNumber, "t_cart_point", emptyNumber);

        // 2、拷贝订单数据到购物车中
        Integer copyNumber = cartPointExMapper.copyCartPoint(cartNumber);
        logger.info("购物车编号【{}】成功拷贝{}到购物车中", cartNumber, copyNumber);

        // 3、删除缓存
        redisUtil.batchDelete(new StringBuilder(CommonConstant.CART_NUMBER).append("-").append(cartNumber).toString());
        // MessageRsp rsp = getCartInfo(cartNumber);
        return null;
    }

    /**
     * 导出excel
     * 销控系统
     * @param cartNumber 购物车编号
     * @return 响应
     */
    @Override
    public MessageRsp exportCartPoint(String cartNumber) throws Exception {
        // 查询数据
        Map<String,Object> param = new HashMap<>();
        if (StringUtils.isBlank(cartNumber)) return MessageUtil.error("购物车编号不能为空！");
        MessageRsp rsp;
        // 存入缓存
        StringBuffer key = new StringBuffer();
        key.append(CommonConstant.CART_NUMBER).append("-").append(cartNumber).append("-EXPORT-SALE");
        String rspMessage = (String) redisUtil.get(key.toString()); // 应为可能会涉及到设备状态会发送变化，使用缓存可能会导致数据异常
        if (StringUtils.isBlank(rspMessage)){
            param.put("cartNumber",cartNumber);
            param.put("system",CommonEnum.SystemType.SALE.getSystemType());
            List<Map<String, Object>> list = cartPointExMapper.exportExcel(param);
            rsp = this.exportData(list, cartNumber);
            if (rsp != null){
                Object data = rsp.getData();
                if (data != null){
                    redisUtil.set(key.toString(),JSON.toJSONString(rsp),CommonConstant.EXP_TIME);
                }
            }
        } else {
            rsp = JSON.parseObject(rspMessage, MessageRsp.class);
        }

        return rsp;
    }

    /**
     * 播控系统导出excel
     * 如果流入到播控系统中 只导出销控系统中存在的点位
     * @param cartNumber 购物车编号
     * @return 响应
     */
    @Override
    public MessageRsp playExportCartPoint(String cartNumber,CartScreenInfoReq req) throws Exception {
        // 数据参数导出相应的数据
        Map<String,Object> param = new HashMap<>();
        String errorParam = this.getPlayParam(param, req);
        if (StringUtils.isNotBlank(errorParam)) return MessageUtil.error(errorParam);
        MessageRsp rsp;
        // 存入缓存
        StringBuffer key = new StringBuffer();
        key.append(CommonConstant.CART_NUMBER).append("-").append(cartNumber)
                .append("-EXPORT-PLAY").append("-").append(req.getNumber())
                .append("-").append(req.getTimeLength())
                .append("-").append(req.getStartTime())
                .append("-").append(req.getEndTime());
        String rspMessage = (String) redisUtil.get(key.toString());
        if (StringUtils.isBlank(rspMessage)){
            List<Map<String, Object>> list = cartPointExMapper.exportExcel(param);
            rsp = this.exportData(list, cartNumber);
            if (rsp != null){
                Object data = rsp.getData();
                if (data != null){
                    redisUtil.set(key.toString(),JSON.toJSONString(rsp),CommonConstant.EXP_TIME);
                }
            }
        } else {
            rsp = JSON.parseObject(rspMessage, MessageRsp.class);
        }
        return rsp;
    }

    /**
     * 播控系统监播报告导出
     * @param cartNumber 购物车
     * @param req 请求
     * @return 响应
     */
    @Override
    public MessageRsp playSupervisionExportCartPoint(String cartNumber, CartScreenInfoReq req) {
        Map<String, String> result = new HashMap<>();
        Map<String,Object> param = new HashMap<>();
        String errorParam = this.getPlayParam(param, req);
        if (StringUtils.isNotBlank(errorParam)) return MessageUtil.error(errorParam);
        List<Map<String, Object>> list = cartPointExMapper.exportSupervisionExcel(param);
        String path = System.getProperty("user.dir");
        logger.info("文件地址：{}", path);
        String filePath = path + File.separator + cartNumber + ".xls";
        FileOutputStream fos = null;
        File file = null;
        try {
            file = new File(filePath);
            fos = new FileOutputStream(file);
            // 创建工作簿
            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFSheet sheet;
            HSSFRow row;
            // 构造数据
            Map<String, List<Map<String, Object>>> groupMap = this.crateExcelData(list);
            // 导出
            Set<Map.Entry<String, List<Map<String, Object>>>> province = groupMap.entrySet();
            String[] values = new String[]{"资产编号", "mac地址", "商家", "商家地址", "商圈", "人均消费", "设备分辨率", "设备类型", "行业", "省", "市", "区","设备状态","客户选择"};
            String[] cellValues;
            for (Map.Entry<String, List<Map<String, Object>>> map : province) {
                String sheetName = map.getKey();
                List<Map<String, Object>> screenList = map.getValue();
                // 创建sheet
                sheet = wb.createSheet(sheetName);
                row = sheet.createRow(0);
                setCellValue(createStyle(wb), row, values);

                for (int i = 0, length = screenList.size(); i < length; i++) {
                    Map<String, Object> screenMap = screenList.get(i);
                    String propertyNumber = screenMap.get("property_number") + "";
                    String mac = screenMap.get("mac") + "";
                    String sellerName = screenMap.get("seller_name") + "";
                    String address = screenMap.get("address") + "";
                    String businessArea = screenMap.get("business_area") + "";
                    String avgPrice = screenMap.get("avg_price") == null ? "" :screenMap.get("avg_price") + "";// 人均消费查询出来null对象
                    String resolution = screenMap.get("resolution") + "";
                    String devName = screenMap.get("dev_name") + "";
                    String trade = screenMap.get("trade") + "";
                    String provinceName = screenMap.get("province") + "";
                    String city = screenMap.get("city") + "";
                    String area = screenMap.get("area") + "";
                    String state = screenMap.get("state") == null ? "" : screenMap.get("state") + "";
                    String custom = screenMap.get("custom") + "";

                    cellValues = new String[]{propertyNumber, mac, sellerName, address, businessArea, avgPrice, resolution, devName, trade, provinceName, city, area, state, custom};
                    setCellValue(null, sheet.createRow(i + 1), cellValues);
                }
            }
            wb.write(fos);
            logger.info("导出成功");
            String ossFilePath = Clock.systemUTC().millis()+ ".xls";
            OssUtil.putFile(ossFilePath, file.getPath());
            logger.info("上传单个文件成功！");
            if (StringUtils.isNotBlank(ossFilePath)){
                result.put("filePath", "/" + ossFilePath);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new RuntimeException();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
                if (file != null) file.delete();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return MessageUtil.success(result);
    }

    /**
     * 导出数据
     *
     * @param list       数据
     * @param cartNumber 购物车编号
     * @return MessageRsp 响应数据
     */
    private MessageRsp exportData(List<Map<String, Object>> list, String cartNumber) {
        Map<String, String> result = new HashMap<>();
        String ossFilePath = null;
        List<Map<String, Object>> chooseList = list.stream()
                .filter(map -> getPointType(map.get("pointtype")) == CommonEnum.PointType.CHOOSE.getPointType())
                .collect(Collectors.toList());// 选择

        List<Map<String, Object>> giveWayList = list.stream()
                .filter(map -> getPointType(map.get("pointtype")) == CommonEnum.PointType.GIVE_AWAY.getPointType())
                .collect(Collectors.toList());// 赠送
        boolean hasZip = false;
        String chooseCartNumber = cartNumber;
        String giveWayCartNumber = cartNumber;
        if (chooseList != null && chooseList.size() > 0 && giveWayList != null && giveWayList.size() > 0) {
            hasZip = true;
            chooseCartNumber = cartNumber + "_xuanze";
            giveWayCartNumber = cartNumber + "_zengsong";
        }
//        String path = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        String path = System.getProperty("user.dir");
        String chooseLocalFile = this.createLocalFile(chooseList, chooseCartNumber);
        File downloadFile = null;
        // 如果存在多个文件 则将其放入到zip文件中
        if (hasZip) {
            try {
                this.createLocalFile(giveWayList, giveWayCartNumber);
                String[] childPath = new String[]{chooseCartNumber, giveWayCartNumber};
                downloadFile = new File(path + File.separator + "download");
                File zipFile = new File(downloadFile, cartNumber + ".zip");
                // 创建zip压缩输出流
                ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));
                for (String child : childPath) {
                    // 获取要压缩的文件
                    FileInputStream fis = new FileInputStream(new File(downloadFile, child + ".xls"));
                    // 添加到压缩文件中
                    out.putNextEntry(new ZipEntry(child + ".xls"));
                    // 设置编码格式
                    out.setEncoding("gbk");
                    byte[] buffer = new byte[1024];
                    int reade;
                    while ((reade = fis.read(buffer)) != -1) {
                        out.write(buffer, 0, reade);
                    }
                    fis.close();
                }
                out.close();
                out.flush();

                ossFilePath = zipFile.getName();
                OssUtil.putFile(ossFilePath, zipFile.getPath());
                logger.info("上传zip文件成功！");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                this.deleteFile(path);
            }
        } else {
            try {
                File file = new File(chooseLocalFile);
                ossFilePath = file.getName();
                OssUtil.putFile(ossFilePath, file.getPath());
                logger.info("上传单个文件成功！");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                this.deleteFile(path);
            }
        }
        if (StringUtils.isNotBlank(ossFilePath)) {
            result.put("filePath", "/" + ossFilePath);
        }
        return MessageUtil.success(result);
    }

    /**
     * 删除文件
     * @param path 文件路径
     */
    private void deleteFile(String path) {
        File downloadFile = new File(path + File.separator + "download");
        if (downloadFile.exists()){
            File[] files = downloadFile.listFiles();
            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
                downloadFile.delete();
            }
        }
    }

    /**
     * 创建本地文件
     *
     * @param list       原始数据
     * @param cartNumber 购物车编号
     * @return 响应
     */
    private String createLocalFile(List<Map<String, Object>> list, String cartNumber) {
//        String path = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        String path = System.getProperty("user.dir");
        logger.info("文件地址：{}", path);
        File downloadFile = new File(path + File.separator + "download");
        if (!downloadFile.exists()) {
            try {
                downloadFile.mkdirs();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String filePath = downloadFile.getPath() + File.separator + cartNumber + ".xls";
        FileOutputStream fos = null;
        File file;
        try {
            file = new File(filePath);
            fos = new FileOutputStream(file);
            // 创建工作簿
            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFSheet sheet;
            HSSFRow row;
            // 构造数据
            Map<String, List<Map<String, Object>>> groupMap = this.crateExcelData(list);
            // 导出
            Set<Map.Entry<String, List<Map<String, Object>>>> province = groupMap.entrySet();
            String[] values = new String[]{"屏编号", "mac地址", "商家", "商家地址", "商圈", "人均消费", "设备分辨率", "设备类型", "行业", "省", "市", "区","设备状态"};
            String[] cellValues;
            for (Map.Entry<String, List<Map<String, Object>>> map : province) {
                String sheetName = map.getKey();
                List<Map<String, Object>> screenList = map.getValue();
                // 创建sheet
                sheet = wb.createSheet(sheetName);
                row = sheet.createRow(0);
                setCellValue(createStyle(wb), row, values);

                for (int i = 0, length = screenList.size(); i < length; i++) {
                    Map<String, Object> screenMap = screenList.get(i);
                    String screenNumber = screenMap.get("screen_number") + "";
                    String mac = screenMap.get("mac") + "";
                    String sellerName = screenMap.get("seller_name") + "";
                    String address = screenMap.get("address") + "";
                    String businessArea = screenMap.get("business_area") + "";
                    String avgPrice = screenMap.get("avg_price") == null ? "" :screenMap.get("avg_price") + "";// 人均消费查询出来null对象
                    String resolution = screenMap.get("resolution") + "";
                    String devName = screenMap.get("dev_name") + "";
                    String trade = screenMap.get("trade") + "";
                    String provinceName = screenMap.get("province") + "";
                    String city = screenMap.get("city") + "";
                    String area = screenMap.get("area") + "";
                    String state = screenMap.get("state") == null ? "" : screenMap.get("state") + "";

                    cellValues = new String[]{screenNumber, mac, sellerName, address, businessArea, avgPrice, resolution, devName, trade, provinceName, city, area, state};
                    setCellValue(null, sheet.createRow(i + 1), cellValues);
                }
            }
            wb.write(fos);
            logger.info("导出成功");
        } catch (Exception e) {
            throw new RuntimeException();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return filePath;
    }

    /**
     * 返回所有订单中购物车编号
     *
     * @return 响应
     */
    @Override
    public MessageRsp getOrderCartNumber() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, String>> orderCartNumberList = cartPointExMapper.getOrderCartNumber();
        if (orderCartNumberList != null && orderCartNumberList.size() > 0) {
            result.put("cartNumberList", orderCartNumberList);
            result.put("total", orderCartNumberList.size());
        }
        return MessageUtil.success(result);
    }

    /**
     * 获取购物车编号下参数(废弃)
     *
     * @param cartNumber 购物车编号
     * @return 响应
     */
    @Override
    public MessageRsp getCartPointParam(String cartNumber) {
        CartPoint cartPoint = null;
        String param = (String) redisUtil.get(new StringBuilder(CommonConstant.SEARCH_SCREEN_PARAM).append(cartNumber).toString());
        if (StringUtils.isNotBlank(param)) {
            cartPoint = JSON.parseObject(param, CartPoint.class);
        } else {
            this.createParamCache(cartNumber);
        }
        return MessageUtil.success(cartPoint);
    }

    /**
     * 导入点位
     */
    @Override
    @Transactional
    public MessageRsp importCartPoint(MultipartFile multipartFile, SearchScreenMediaInfoReq req) throws Exception {
        logger.info("开始时间：{}", Clock.systemUTC().millis());
        Map<String, Object> result = new HashMap<>();
        String fileName = multipartFile.getOriginalFilename();
        int uploadFileSize = multipartFile.getInputStream().available();// 文件大小 字节kb
        if (uploadFileSize > fileSize * 1024 * 1024) return MessageUtil.error("上传文件过大，请分批次上次！");

        String fileType = fileName.substring(fileName.lastIndexOf("."));
        Integer pointType = req.getPointType();
        if (pointType == null) return MessageUtil.error("点位类型不能为空！");

        if (!".xls".equals(fileType) && !".xlsx".equals(fileType)) return MessageUtil.error("请上次excel表格！");
        // 创建工作簿
        Workbook workbook = null;
        try {
            workbook = new XSSFWorkbook(multipartFile.getInputStream());
        } catch (Exception ex) {
            try {
                workbook = new HSSFWorkbook(multipartFile.getInputStream());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // 验证参数正确
        String errorMsg = checkSearchParam(req, "add");
        if (StringUtils.isNotBlank(errorMsg)) {
            return MessageUtil.error(errorMsg);
        }
        String cartNumber = req.getCartNumber();
        // 当前购物车是否选择了该点位
        List<String> existScreenNumberList = null;
        if (StringUtils.isNotBlank(cartNumber)) {
            existScreenNumberList = this.getCartScreenNumberList(cartNumber, null, pointType, null, req.getNumber(), req.getTimeLength(), req.getStartTime(), req.getEndTime());
        } else {
            cartNumber = UUID.randomUUID().toString().replaceAll("-", "");
        }
        // 验证是否存在重复的excel数据针对错误值
        Map<String, Integer> repeatErrorData = new HashMap<>();
        // 错误数据
        List<Map<String, Object>> errorListMap = new ArrayList<>();
        // 屏信息数据
        List<ScreenInfoReq> screenInfoList = new ArrayList<>();
        List<ScreenInfoReq> checkScreenInfoList;
        ScreenInfoReq infoReq;
        // 获取sheet个数
        int sheetNumber = workbook.getNumberOfSheets();
        // 能否选择conf
        Integer playMode = req.getPlayMode();// 播放模式

        List<String> refuseScreenList = null;
        if (playMode == null || playMode != CommonEnum.PlayMode.INSERT.getPlayType()){
            refuseScreenList = this.refuseScreenNumberList(null, null, null, req.getStartTime(), req.getEndTime(), req.getNumber(), req.getTimeLength());
        }
        // 赠送验证城市
        List<String> refuseGiveAwayCityList = new ArrayList<>();
        if (req.getPointType() == CommonEnum.PointType.GIVE_AWAY.getPointType() && req.getSystem() == CommonEnum.SystemType.SALE.getSystemType()){
            refuseGiveAwayCityList = cartPointExMapper.checkGiveWayCity(cartNumber, null);
        }
        // 获取当前状态不可用的屏数据
        List<Map<String, String>> disabledScreenList = cartPointExMapper.searchScreenState();
        Map<String, String> disableScreenMap = this.createDisableScreenMap(disabledScreenList);

        logger.info("验证用时：{}", Clock.systemUTC().millis());
        int excelTotal = 0;// excel表格中的数据 不能使用length字段  因为会去验证excel表格中的数据是否合法
        for (int i = 0; i < sheetNumber; i++) {
            Sheet sheet = workbook.getSheetAt(i);
            String cityName = sheet.getSheetName();
            if (StringUtils.isBlank(cityName)) MessageUtil.error("sheet名称不能为空！");

            // 当前购物车编号对应的点位id集合
            // List<Integer> cartPointIds;
            // cartPointIds = this.getCartPointIds(cartNumber, null, pointType, cityName, req.getNumber(), req.getTimeLength(), req.getStartTime(), req.getEndTime());
            // 能否选择conf
            // refuseScreenList = this.refuseScreenNumberList(null, null, null, req.getStartTime(), req.getEndTime(), req.getNumber(), req.getTimeLength());
            // 查询当前城市所有屏信息
            Map<String, String> screenByCityMap = this.getScreenByCity(cityName);
            Row row;
            int rowNumber = sheet.getLastRowNum();
            if (rowNumber > 0) {
                for (int j = 1; j <= rowNumber; j++) {
                    row = sheet.getRow(j);
                    if (row != null) {
                        excelTotal++;
                        String screen_number = getValue(row.getCell(0));
                        String mac = getValue(row.getCell(1));
                        String seller_name = getValue(row.getCell(2));
                        String address = getValue(row.getCell(3));
                        String business_area = getValue(row.getCell(4));
                        String avg_price = getValue(row.getCell(5));
                        String resolution = getValue(row.getCell(6));
                        String dev_name = getValue(row.getCell(7));
                        String trade = getValue(row.getCell(8));
                        String province = getValue(row.getCell(9));
                        String city = getValue(row.getCell(10));
                        String area = getValue(row.getCell(11));
                        String state = disableScreenMap.get(screen_number);
                        // 如果是空数据直接忽略
                        if (StringUtils.isBlank(screen_number) && StringUtils.isBlank(mac)
                                && StringUtils.isBlank(province) && StringUtils.isBlank(city)
                                && StringUtils.isBlank(area)){
                            continue;
                        }

                        // 资产编号不能为空判断
                        if (StringUtils.isBlank(screen_number) && StringUtils.isNotBlank(mac)){
                            errorListMap.add(this.createErrorData(screen_number, mac, seller_name, address, business_area, avg_price, resolution, dev_name, trade, province, city, area, state));
                            continue;
                        }

                        // 记录
                        if (repeatErrorData.get(screen_number) == null) {
                            repeatErrorData.put(screen_number, 1);
                        } else {
                            errorListMap.add(this.createErrorData(screen_number, mac, seller_name, address, business_area, avg_price, resolution, dev_name, trade, province, city, area, state));
                            continue;
                        }
                        // key值
                        String key = screen_number + "_" + cityName;
                        String cityCode = screenByCityMap.get(key);
                        // 不存在表示存在错误数据
                        if (screenByCityMap.get(key) == null) {
                            errorListMap.add(this.createErrorData(screen_number, mac, seller_name, address, business_area, avg_price, resolution, dev_name, trade, province, city, area, state));
                        } else {
                            // 存在屏设备不可用的数据
                            if (StringUtils.isNotBlank(state)){
                                errorListMap.add(this.createErrorData(screen_number, mac, seller_name, address, business_area, avg_price, resolution, dev_name, trade, province, city, area, state));
                                continue;
                            }

                            // 存在需要验证1、是否重复 2、能否选择 排除已经添加到错误集合中的数据
                            if (existScreenNumberList != null && existScreenNumberList.size() > 0) {
                                if (existScreenNumberList.contains(screen_number)) {
                                    errorListMap.add(this.createErrorData(screen_number, mac, seller_name, address, business_area, avg_price, resolution, dev_name, trade, province, city, area, state));
                                    continue;
                                }
                            }
                            // 2、能否选择 满足时间
                            if (refuseScreenList != null && refuseScreenList.contains(screen_number)) {
                                errorListMap.add(this.createErrorData(screen_number, mac, seller_name, address, business_area, avg_price, resolution, dev_name, trade, province, city, area, state));
                                continue;
                            }
                            // 3、赠送城市 1、销控并且是赠送条件，需要验证是城市是否一致 在选中的屏中排除选中点位城市下的屏信息
                            if (req.getPointType() == CommonEnum.PointType.GIVE_AWAY.getPointType() && req.getSystem() == CommonEnum.SystemType.SALE.getSystemType()) {
                                if (refuseGiveAwayCityList.contains(screen_number)) {
                                    errorListMap.add(this.createErrorData(screen_number, mac, seller_name, address, business_area, avg_price, resolution, dev_name, trade, province, city, area, state));
                                    continue;
                                }
                            }
                            // 验证成功的数据
                            infoReq = new ScreenInfoReq();
                            infoReq.setScreenNumber(screen_number);
                            infoReq.setCityName(cityName);
                            infoReq.setCityCode(cityCode);
                            screenInfoList.add(infoReq);
                        }
                    }
                }
            }
        }
        logger.info("组装数据时间：{}", Clock.systemUTC().millis());
        Integer orderType = req.getOrderType();
        // 试投订单验证
        if (excelTotal > 0 && orderType == CommonEnum.OrderType.TRY.getOrderType()){
            req.setScreenArr(screenInfoList);
            String errorMessage = checkTryOrderNumber(excelTotal, cartNumber, req);
            if (StringUtils.isNotBlank(errorMessage)) return MessageUtil.error(errorMessage);
        }
        boolean flag = true;
        // 正确数据进行添加
        int length = screenInfoList.size();
        result.put("cartNumber", StringUtils.isNotBlank(cartNumber) ? cartNumber : UUID.randomUUID().toString().replaceAll("-", ""));// 无论成功与否都返回cartNumber
        if (length > 0) {
            List<CartPoint> cartPointList = new ArrayList<>();
            String screenNumber;
            CartPoint cartPoint;
            long createTime = System.currentTimeMillis() / 1000;
            for (int i = 0; i < length; i++) {
                cartPoint = new CartPoint();
                ScreenInfoReq screenInfoReq = screenInfoList.get(i);
                screenNumber = screenInfoReq.getScreenNumber();
                // 属性拷贝
                BeanCopyUtil.copyBean(cartPoint, req, "timeId", "timeLength", "number", "purpose", "startTime", "endTime", "timeBucket", "selectMode", "playMode", "system", "pointType", "userId", "orderType");
                cartPoint.setCityCode(screenInfoReq.getCityCode());
                cartPoint.setCityName(screenInfoReq.getCityName());
                cartPoint.setScreenNumber(screenNumber);
                cartPoint.setCartNumber(cartNumber);
                cartPoint.setState(0);
                cartPoint.setCreateTime(createTime);
                cartPoint.setTimeTotal(req.getTimeLength() * req.getNumber());
                cartPointList.add(cartPoint);
            }
            // 批量添加数据
            this.batchAddPointCart(cartNumber, cartPointList);
            result.put("addNumber", length);
        }
        logger.info("添加数据时间：{}", Clock.systemUTC().millis());
        // 错误数据进行导出
        if (errorListMap.size() > 0) {
            result.put("errorNumber", errorListMap.size());
            MessageRsp rsp = this.exportData(errorListMap, System.currentTimeMillis() + "");
            if (rsp.getErrorcode() == 0) {
                Map<String, String> data = (Map<String, String>) rsp.getData();
                result.put("filePath", data.get("filePath"));
            } else {
                flag = false;
            }
        }
        return flag ? MessageUtil.success(result) : MessageUtil.error(result,"导出错误数据发送错误！");
    }

    /**
     * 导入赠送点位
     * @param multipartFile 文件
     * @param req 参数
     * @return 响应
     */
    @Override
    public MessageRsp importGiveAwayCartPoint(MultipartFile multipartFile, SearchScreenMediaInfoReq req) throws Exception {
        // 1、销控 2、播控购物车编号是从销控过来,并且是赠送条件，需要验证是城市是否一致 在选中的屏中排除选中点位城市下的屏信息 满足赠送条件一定是存在购物车编号
        String cartNumber = req.getCartNumber();
        if (req.getSystem() == CommonEnum.SystemType.SALE.getSystemType()) {
            // 赠送时间验证
            String message = this.checkGiveAwayDate(cartNumber, req.getStartTime(), req.getEndTime());
            if (StringUtils.isNotBlank(message)) return MessageUtil.error(message);
        }
        return this.importCartPoint(multipartFile, req);
    }

    /**
     * 根据购物车编号查询统计信息
     * @param cartNumber 购物车编号
     * @return 响应
     */
    @Override
    public MessageRsp getStatisticsByCartNumber(String cartNumber) {
        if (StringUtils.isBlank(cartNumber)) return MessageUtil.error("购物车编号不能为空！");
        List<Map<String, Object>> statisticsByCartNumber = cartPointExMapper.getStatisticsByCartNumber(cartNumber);
        return MessageUtil.success(statisticsByCartNumber);
    }

    /**
     * 修改购物车基本信息
     *
     * @param req 请求参数
     * @return 响应
     */
    @Override
    @Transactional
    public MessageRsp modifyCartPoint(ModifyCartPointReq req) {
        Integer pointType = req.getPointType();
        String cartNumber = req.getCartNumber();// 购物车编号
        if (StringUtils.isBlank(cartNumber)) return MessageUtil.error("购物车编号不能为空！");
        String cityName = req.getCityName();// 城市名称
        if (StringUtils.isBlank(cityName)) return MessageUtil.error("城市名称不能为空！");
        Integer system = req.getSystem();// 所属系统
        if (system == null) return MessageUtil.error("当前系统不能为空！");

        // 目标参数
        CartScreenInfoReq targetData = req.getTargetData();
        if (targetData == null){
            return MessageUtil.error("修改目标不能为空！");
        } else {
            targetData.setCityName(cityName);
            if (targetData.getNumber() == null) MessageUtil.error("目标刊位个数不能为空！");
            if (targetData.getTimeLength() == null) MessageUtil.error("目标刊位时长不能为空！");
            if (targetData.getStartTime() == null) MessageUtil.error("目标开始时间不能为空！");
            if (targetData.getEndTime() == null) MessageUtil.error("目标结束时间不能为空！");
        }
        // 修改参数
        CartScreenInfoReq putData = req.getPutData();
        if (putData == null) {
            return MessageUtil.error("修改参数不能为空！");
        } else {
            putData.setCityName(cityName);
            if (putData.getNumber() == null) MessageUtil.error("修改刊位个数不能为空！");
            if (putData.getTimeLength() == null) MessageUtil.error("修改刊位时长不能为空！");
            if (putData.getStartTime() == null) MessageUtil.error("修改开始时间不能为空！");
            if (putData.getEndTime() == null) MessageUtil.error("修改结束时间不能为空！");
        }

        // 获取购物车订单类型
        List<CartPoint> cartPointList = this.searchCartPointList(cartNumber, null, null, null, null, null, null, null);
        if (cartPointList == null || cartPointList.size() == 0) return MessageUtil.error("当前购物车中无点位数据！");
        CartPoint cartPoint = cartPointList.get(0);
        Integer orderType = cartPoint.getOrderType(); // 订单类型

        Long startTime = putData.getStartTime();// 修改起始时间
        Long endTime = putData.getEndTime();// 修改结束时间
        // 试投订单验证周期
        if (orderType == CommonEnum.OrderType.TRY.getOrderType() && startTime != null && putData.getEndTime() != null){
            // 如果是试投订单有效期只有7天
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(startTime * 1000L);
            calendar.add(Calendar.DAY_OF_MONTH,7);
            Long tryEndTime = calendar.getTimeInMillis() / 1000L;
            if (putData.getEndTime() - tryEndTime > 0) return MessageUtil.error("试投订单周期最多不超过7天！");
        }
        // 先验证能否提交(重复)
        int count = cartPointExMapper.checkNumber(targetData, putData, cartNumber, pointType, system);
        if (count > 0) return MessageUtil.error("相同条件下已存在数据，修改失败！");

        // 验证时长是否满足 获取源数据下的屏集合  根据最新修改的参数验证能否进行修改
        List<String> screenNumberList = this.getCartScreenNumberList(cartNumber, system, pointType, cityName, targetData.getNumber(), targetData.getTimeLength(), targetData.getStartTime(), targetData.getEndTime());
        List<Integer> cartPointIdList = null; // 销控系统中如果订单已提交不能进行修改操作 this.getCartPointIds(cartNumber, system, pointType, cityName, targetData.getNumber(), targetData.getTimeLength(), targetData.getStartTime(), targetData.getEndTime());

        List<String> refuseScreenList = this.refuseScreenNumberList(screenNumberList, null, null, startTime, endTime, putData.getNumber(), putData.getTimeLength());
        if (refuseScreenList != null && refuseScreenList.size() > 0) {
            return MessageUtil.error("存在不能满足时长的点位！");
        }

        // 修改数据
        int modifyNumber = cartPointExMapper.modifyCartPoint(targetData, putData, cartNumber, pointType, system);
        logger.info("成功修改购物车编号【{}】下：{}条数据", cartNumber, modifyNumber);
        // 删除缓存
        redisUtil.batchDelete(new StringBuilder(CommonConstant.CART_NUMBER).append("-").append(cartNumber).toString());
        return MessageUtil.success();
    }

    /**
     * 修改赠送点位信息
     * @param req 参数
     * @return 响应
     */
    @Override
    public MessageRsp modifyGiveWayCartPoint(ModifyCartPointReq req) {
        String cartNumber = req.getCartNumber();
        if (StringUtils.isBlank(cartNumber)) return MessageUtil.error("购物车编号不能为空！");
        Integer pointType = CommonEnum.PointType.GIVE_AWAY.getPointType();
        req.setPointType(pointType);
        Integer system = req.getSystem();
        if (system == null) return MessageUtil.error("请选择系统！");
        // 销控会验证时长
        if (req.getSystem() == CommonEnum.SystemType.SALE.getSystemType()){
            // 验证赠送时间是否合法
            String message = this.checkGiveAwayDate(cartNumber, req.getPutData().getStartTime(), req.getPutData().getEndTime());
            if (StringUtils.isNotBlank(message)) return MessageUtil.error(message);
        }
        return this.modifyCartPoint(req);
    }

    /**
     * 赠送条件
     *
     * @param cartNumber 购物车编号
     * @param giveAwayCondition      赠送条件
     * @return 响应
     */
    @Override
    public MessageRsp giveAwayPointCondition(String cartNumber, Map<String,String> giveAwayCondition) {
        if (StringUtils.isBlank(cartNumber)) return MessageUtil.error("购物车编号不能为空！");
        String scale = giveAwayCondition.get("scale");
        String way = giveAwayCondition.get("way");
        if (scale == null) return MessageUtil.error("请输入赠送比例！");
        // if (way == null) return MessageUtil.error("请选择赠送方式！");

        StringBuilder sb = new StringBuilder();
        sb.append(CommonConstant.GIVE_AWAY).append("-").append(cartNumber);
        JSONObject condition = new JSONObject();
        condition.put("scale", scale);
        condition.put("way", way);
        redisUtil.set(sb.toString(), condition.toJSONString(), null);
        return MessageUtil.success();
    }

    /**
     * 确认购物车
     *
     * @return 响应
     */
    @Override
    public MessageRsp confirmPoint(String cartNumber, JSONObject req) {
        List<CartPoint> cartPointList = this.searchCartPointList(cartNumber, null, null, null, null, null, null, null);
        if (cartPointList == null || cartPointList.size() == 0) return MessageUtil.error("当前购物车中没有任何点位！");

        // 试投订单不进行验证
        if (cartPointList.get(0).getOrderType() != CommonEnum.OrderType.TRY.getOrderType()){
            Integer system = req.getInteger("system");
            // 设置赠送缓存
            Float scale = req.getFloat("scale");
            if (scale != null){
                Map<String, String> giveAwayMap = new HashMap<>();
                giveAwayMap.put("scale",req.getString("scale"));
                giveAwayMap.put("way",req.getString("way"));
                this.giveAwayPointCondition(cartNumber,giveAwayMap);
            }
            // 验证能否提交 赠送条件是否存在等
            Map<String, Integer> systemReq = new HashMap<>();
            systemReq.put("system",system);
            // 判断赠送条件
            String message = this.checkGiveAwayParam(cartNumber, systemReq);
            String check = message;// 是否需要验证赠送
            if (StringUtils.isNotBlank(message) && !"check".equals(check)) return MessageUtil.error(message);

            // 验证选择点位是否满足条件
            if (system == CommonEnum.SystemType.SALE.getSystemType()){
                message = this.checkChoose(cartNumber);
                if (StringUtils.isNotBlank(message)) return MessageUtil.error(message + "未达标！");

            }

            // 存在赠送点位需要验证是否满足赠送比例
            if ("check".equals(check)){
                //验证赠送选择时间段是否满足条件(销控)
                if (system == CommonEnum.SystemType.SALE.getSystemType()){
                    message = this.checkDate(cartNumber);
                    if (StringUtils.isNotBlank(message)) return MessageUtil.error(message);
                }

                // 验证城市级别下是否满足赠送比例
                Map<String, Boolean> cityLevelMap = this.checkGiveAway(cartNumber);
                String refuseCityLevel = this.getRefuseCityLevel(cityLevelMap);
                if (StringUtils.isNotBlank(refuseCityLevel)) return MessageUtil.error(refuseCityLevel + "不满足赠送标准！");
            }
        }
        return MessageUtil.success();
    }

    /**ch
     * 播控系统确认购物车(未使用接口)
     * @param cartNumber 购物车编号
     * @param req 请求参数ch
     * @return 响应
     */
    @Override
    public MessageRsp playConfirmPoint(String cartNumber, CartScreenInfoReq req) {
        if (StringUtils.isBlank(cartNumber)) return MessageUtil.error("购物车编号不能为空！");
        List<CartPoint> cartPointList = this.searchCartPointList(cartNumber, null, null, null, null, null, null, null);
        if (cartPointList == null || cartPointList.size() == 0) return MessageUtil.error("当前购物车中没有任何点位！");
        // 验证选择点位是否满足条件
        String errorMsg = this.playCheckChoose(cartNumber);
        if (StringUtils.isNotBlank(errorMsg)) return MessageUtil.error(errorMsg + "未达标！");

        return MessageUtil.success();
    }

    /**
     * 验证购物车 赠送 时间是否满足条件
     * @param cartNumber 购物车编号
     * @return 响应
     */
    private String checkDate(String cartNumber) {
        List<PointStatistics> pointStatistics = this.getPointStatistics(cartNumber, null);
        pointStatistics = pointStatistics.stream().filter(statistics -> statistics.getSystem() == CommonEnum.SystemType.SALE.getSystemType()).collect(Collectors.toList());// 只查看销控系统

        long chooseMin = pointStatistics.stream().filter(statistics -> statistics.getPointType() == 0).collect(Collectors.summarizingLong(PointStatistics::getStartTime)).getMin();
        long chooseMax = pointStatistics.stream().filter(statistics -> statistics.getPointType() == 0).collect(Collectors.summarizingLong(PointStatistics::getEndTime)).getMax();
        long giveAwayMin = pointStatistics.stream().filter(statistics -> statistics.getPointType() == 1).collect(Collectors.summarizingLong(PointStatistics::getStartTime)).getMin();
        long giveAwayMax = pointStatistics.stream().filter(statistics -> statistics.getPointType() == 1).collect(Collectors.summarizingLong(PointStatistics::getEndTime)).getMax();
        if (giveAwayMin < chooseMin || giveAwayMax > chooseMax) return "赠送点位时间段选择有误！";
        return null;
    }

    /**
     * 验证赠送时间范围
     * @param cartNumber 购物车编号
     */
    private String checkGiveAwayDate(String cartNumber,Long startTime,Long endTime) {
        Map<String, Long> districtByCartNumber = cartPointExMapper.getDistrictByCartNumber(cartNumber);
        if (districtByCartNumber == null) return "当前购物车无选择点位！";
        Long min = districtByCartNumber.get("min");
        Long max = districtByCartNumber.get("max");
        if (min > startTime || max < endTime) return "赠送点位时间段选择有误！";

        return null;
    }

    /**
     * 销控 满足赠送比例是否满足 对应城市级别
     * @param cartNumber 购物车编号
     * @return 响应
     */
    private Map<String,Boolean> checkGiveAway(String cartNumber) {
        Map<String,Boolean> errorMap = new HashMap<>();
        List<PointStatistics> pointStatistics = cartPointExMapper.searchPointStatistics(cartNumber,null);

        // 获取城市级别下选中点位统计
        Map<String, List<PointStatistics>> chooseCityLevel = pointStatistics.stream()
                .filter(statistics -> statistics.getPointType() == CommonEnum.PointType.CHOOSE.getPointType()
                        && statistics.getSystem() == CommonEnum.SystemType.SALE.getSystemType())
                .collect(Collectors.groupingBy(point -> point.getCityLevel() == null ? "" : point.getCityLevel()));

        // 赠送统计
        Map<String, List<PointStatistics>> giveAwayCityLevel = pointStatistics.stream()
                .filter(statistics -> statistics.getPointType() == CommonEnum.PointType.GIVE_AWAY.getPointType())
                .collect(Collectors.groupingBy(point -> point.getCityLevel() == null ? "" : point.getCityLevel()));

        // 计算比例
        if (giveAwayCityLevel != null && giveAwayCityLevel.size() > 0){
            // 存在赠送的点位才进入下一步流程
            String giveWayStr = (String) redisUtil.get(new StringBuilder().append(CommonConstant.GIVE_AWAY).append("-").append(cartNumber).toString());
            JSONObject giveWayCon = JSON.parseObject(giveWayStr);
            Float scale = null;
            // Integer way = null;
            if (giveWayCon != null){
                scale = giveWayCon.getFloat("scale");// 赠送比例
                // way = giveWayCon.getInteger("way");// 0:同机送时 1:同时送机
            }
            Set<Map.Entry<String, List<PointStatistics>>> giveAwayCityLevelKey = giveAwayCityLevel.entrySet();
            for (Map.Entry<String, List<PointStatistics>> key : giveAwayCityLevelKey){
                boolean flag = true;
                String cityLevel = key.getKey();
                List<PointStatistics> giveAwayCityLevelList = key.getValue();
                double giveAwayPointTime = giveAwayCityLevelList.stream().mapToDouble(pointStatic -> pointStatic.getPointTime() == null ? 0 : pointStatic.getPointTime()).sum();
                // 赠送的存在但是选择的点位不存在默认为不达标
                List<PointStatistics> chooseCityLevelList = chooseCityLevel.get(cityLevel);
                if (scale == null || chooseCityLevelList == null){
                    flag = false;
                } else {
                    double choosePointTime = chooseCityLevelList.stream().mapToDouble(pointStatic -> pointStatic.getPointTime() == null ? 0 : pointStatic.getPointTime()).sum();
                    double maxGiveWayPointTime = choosePointTime * scale;
                    if (giveAwayPointTime > maxGiveWayPointTime) flag = false;
                }

                errorMap.put(cityLevel,flag);
            }
        }

        return errorMap;
    }

    /**
     * 验证选择点位是否满足条件
     * @param cartNumber 购物车编号
     * @return 响应
     */
    private String checkChoose(String cartNumber){
        List<PointStatistics> pointStatistics = cartPointExMapper.searchPointStatistics(cartNumber,null);
        // 判断城市下是否有不满足选择点位或者赠送点位
       String chooseNoSatisfy = pointStatistics.stream()
                .filter(statistics -> statistics.getPointType() == CommonEnum.PointType.CHOOSE.getPointType())
                .filter(statistics -> statistics.getSystem() == CommonEnum.SystemType.SALE.getSystemType())
                .filter(pointCart -> (pointCart.getPointTime() == null || !Boolean.valueOf(pointCart.getSatisfy())))
                .map(PointStatistics::getCityName).distinct()
                .collect(Collectors.joining(","));
       return chooseNoSatisfy;
    }

    /**
     * 不影响之前的代码重新(废弃)
     * @param cartNumber 购物车编号
     * @return 响应
     */
    private String playCheckChoose(String cartNumber){
        List<PointStatistics> pointStatistics = cartPointExMapper.searchPointStatistics(cartNumber,null);
        // 判断城市下是否有不满足选择点位或者赠送点位
        String chooseNoSatisfy = pointStatistics.stream()
                .filter(statistics -> statistics.getPointType() == CommonEnum.PointType.CHOOSE.getPointType())
                .filter(statistics -> statistics.getSystem() == CommonEnum.SystemType.PLAY.getSystemType())
                .filter(pointCart -> (pointCart.getPointTime() == null || !Boolean.valueOf(pointCart.getSatisfy())))
                .map(PointStatistics::getCityName).distinct()
                .collect(Collectors.joining(","));
        return chooseNoSatisfy;
    }

    /**
     * 验证条件是否满足 计算总数
     * @param cartNumber 购物车编号
     * @param systemReq 请求参数
     * @return 响应
     */
    private String checkGiveAwayParam(String cartNumber, Map<String, Integer> systemReq) {
        String message = "";
        List<PointStatistics> pointStatistics = cartPointExMapper.searchPointStatistics(cartNumber,null);
        List<String> chooseCityNameList = pointStatistics.stream()
                .filter(statistics -> statistics.getPointType() == CommonEnum.PointType.CHOOSE.getPointType()
                    && statistics.getSystem() == CommonEnum.SystemType.SALE.getSystemType())
                .map(PointStatistics::getCityName).distinct().collect(Collectors.toList());

        List<String> giveWayCityNameList = pointStatistics.stream()
                .filter(statistics -> statistics.getPointType() == 1)
                .map(PointStatistics::getCityName).distinct().collect(Collectors.toList());
        // 存在赠送的点位才进入下一步流程
        if (giveWayCityNameList != null && giveWayCityNameList.size() > 0) {
            Integer system = systemReq.get("system");
            if (system == null) return "当前系统不能为空！";
            if (system == CommonEnum.SystemType.SALE.getSystemType()){
                List<String> errorCityNameList = giveWayCityNameList.stream().filter(cityName -> !chooseCityNameList.contains(cityName)).distinct().collect(Collectors.toList());
                if (errorCityNameList != null && errorCityNameList.size() > 0)
                    return "以下城市不能作为赠送！【" + errorCityNameList.stream().collect(Collectors.joining(",")) + "】";
            }

            String giveWayStr = (String) redisUtil.get(new StringBuilder().append(CommonConstant.GIVE_AWAY).append("-").append(cartNumber).toString());
            if (StringUtils.isBlank(giveWayStr)) return "赠送条件未添加！";

            JSONObject giveWayCon = JSON.parseObject(giveWayStr);
            Float scale = giveWayCon.getFloat("scale");// 赠送比例
            if (scale - 1.0 > 0) return "赠送比例不能大于1！";

            // 用于标志存在赠送点位，并且基本提交已经满足
            message = "check";
        }
        return message;
    }

    /**
     * 播控系统存在赠送 验证条件是否满足 计算总数 (接口未使用)
     * @param cartNumber 购物车编号
     * @return 响应
     */
    private String checkGiveAwayParamByPlay(String cartNumber) {
        String message = "";
        List<PointStatistics> pointStatistics = cartPointExMapper.searchPointStatistics(cartNumber,null);
        List<String> chooseCityNameList = pointStatistics.stream()
                .filter(statistics -> statistics.getPointType() == CommonEnum.PointType.CHOOSE.getPointType())
                .map(PointStatistics::getCityName).distinct().collect(Collectors.toList());

        List<String> giveWayCityNameList = pointStatistics.stream()
                .filter(statistics -> (statistics.getPointType() == CommonEnum.PointType.GIVE_AWAY.getPointType()
                        && statistics.getSystem() == CommonEnum.SystemType.PLAY.getSystemType()))
                .map(PointStatistics::getCityName).distinct().collect(Collectors.toList());

        // 如果是从销控系统流过来的购物车
        if (giveWayCityNameList != null && giveWayCityNameList.size() > 0) {
            List<String> errorCityNameList = giveWayCityNameList.stream().filter(cityName -> !chooseCityNameList.contains(cityName)).distinct().collect(Collectors.toList());
            if (errorCityNameList != null && errorCityNameList.size() > 0)
                return "以下城市不能作为赠送！【" + errorCityNameList.stream().collect(Collectors.joining(",")) + "】";
            // 用于标志存在赠送点位，并且基本提交已经满足
            message = "check";
        }
        return message;
    }

    /**
     * 选择赠送点位
     *
     * @param req 请求参数
     * @return 响应
     */
    @Override
    @Transactional
    public MessageRsp addGiveAwayPoint(SearchScreenMediaInfoReq req) {
        String cartNumber = req.getCartNumber();
        if (StringUtils.isBlank(cartNumber)) return MessageUtil.error("购物车编号不能为空！");
        // 1、销控会验证时长
        if (req.getSystem() == CommonEnum.SystemType.SALE.getSystemType()){
            Map<String, Long> districtByCartNumber = cartPointExMapper.getDistrictByCartNumber(cartNumber);
            if (districtByCartNumber == null) return MessageUtil.error("购物车无选择点位！");
            Long min = districtByCartNumber.get("min");
            Long max = districtByCartNumber.get("max");
            Long startTime = req.getStartTime();
            Long endTime = req.getEndTime();
            if (min > startTime || max < endTime) return MessageUtil.error("赠送点位时间段选择有误！");
        }
        return addPointCart(req);
    }

    /**
     * 判断是否来源销控系统
     * @param cartNumber 购物车编号
     * @return 响应
     */
    private boolean checkFromSaleSystem(String cartNumber) {
        if (StringUtils.isBlank(cartNumber)) return false;
        // 验证当前购物车是否存在销控中选中的点位 如果存在表示是从销控系统产生
        List<CartPoint> cartPointList = this.searchCartPointList(cartNumber, CommonEnum.SystemType.SALE.getSystemType(), CommonEnum.PointType.CHOOSE.getPointType()
                , null, null, null, null, null);
        return cartPointList != null && cartPointList.size() > 0;
    }

    /**
     * 根据城市获取城市key[名称+屏编号]对应的value[城市代码]
     *
     * @param cityName 城市名称
     * @return 响应
     */
    private Map<String, String> getScreenByCity(String cityName) {
        Map<String, String> cityScreenMap = new HashMap<>();
        List<Map<String, String>> screenByCityList = cartPointExMapper.getScreenByCity(cityName);
        if (screenByCityList != null && screenByCityList.size() > 0) {
            for (int i = 0, length = screenByCityList.size(); i < length; i++) {
                Map<String, String> map = screenByCityList.get(i);
                cityScreenMap.put(map.get("cityinfo"), map.get("citycode"));
            }
        }
        return cityScreenMap;
    }

    /**
     * 获取不可用屏数据
     * @param disabledScreenList 不可以集合
     * @return 响应
     */
    private Map<String,String> createDisableScreenMap(List<Map<String, String>> disabledScreenList) {
        Map<String,String> disable = new HashMap<>();
        if (disabledScreenList != null && disabledScreenList.size() > 0){
            disabledScreenList.forEach(map -> {
                String screen_number = map.get("screen_number");
                String state = map.get("media_resource_status");
                disable.put(screen_number,state);
            });
        }
        return disable;
    }

    /**
     * 错误数据
     *
     * @return 响应con
     */
    private Map<String, Object> createErrorData(String screen_number, String mac, String seller_name, String address, String business_area,
                                                String avg_price, String resolution, String dev_name,
                                                String trade, String province, String city, String area, String state) {
        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("screen_number", screen_number);
        errorMap.put("mac", mac);
        errorMap.put("seller_name", seller_name);
        errorMap.put("address", address);
        errorMap.put("business_area", business_area);
        errorMap.put("avg_price", avg_price);
        errorMap.put("resolution", resolution);
        errorMap.put("dev_name", dev_name);
        errorMap.put("trade", trade);
        errorMap.put("province", province);
        errorMap.put("city", city);
        errorMap.put("area", area);
        errorMap.put("state",state);
        return errorMap;
    }

    /**
     * 构造excel表格数据
     *
     * @param list 导出点位数据集合
     * @return 响应
     */
    private Map<String, List<Map<String, Object>>> crateExcelData(List<Map<String, Object>> list) {
        Map<String, List<Map<String, Object>>> groupMap = new HashMap<>();
        list.forEach(listMap -> {
            Map<String, Object> map = listMap;
            String province = map.get("city") + "";
            List<Map<String, Object>> screenList = groupMap.get(province);
            if (screenList == null) {
                screenList = new ArrayList<>();
            }
            screenList.add(map);
            groupMap.put(province, screenList);
        });
        return groupMap;
    }

    /**
     * 单元格设置值
     *
     * @param row    行
     * @param values 值
     */
    private void setCellValue(CellStyle cellStyle, HSSFRow row, String... values) {
        if (values != null && values.length > 0) {
            for (int i = 0, length = values.length; i < length; i++) {
                String value = values[i];
                HSSFCell cell = row.createCell(i);
                cell.setCellValue(value);
                if (cellStyle != null) {
                    cell.setCellStyle(cellStyle);
                }
            }
        }
    }

    /**
     * 创建单元格样式
     *
     * @param workbook 工作簿
     * @return 响应
     */
    private CellStyle createStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setColor(HSSFFont.COLOR_RED);
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);

        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setFont(font);
        return cellStyle;
    }

    /**
     * 获取cell内容
     */
    private String getValue(Cell cell) {
        if (cell != null) {
            if (cell.getCellType() == 4) {
                return String.valueOf(cell.getBooleanCellValue());
            } else if (cell.getCellType() == 0) {
                DecimalFormat df = new DecimalFormat("0");
                return df.format(cell.getNumericCellValue());
            }
            return String.valueOf(cell.getStringCellValue());
        }
        return "";
    }

    /**
     * 设置精度
     * @param number 数值
     * @return 响应
     */
    private float setNumberPrecision(Float number){
        float precisionMoney = 0.0F;
        if (number != null){
            DecimalFormat format = new DecimalFormat("#0.#");
            format.setRoundingMode(RoundingMode.FLOOR);
            precisionMoney = Float.valueOf(format.format(number + 0.001F));
        }

        return precisionMoney;
    }
}
