package com.point.cart.message.req;

import java.util.List;
import java.util.Map;

/**
 *  选择屏请求参数
 */
public class SearchScreenMediaInfoReq {
    private Integer selectMode;// 选点模式(1代表超级模式，0代表普通模式)
    private Integer playMode;// 播放方式
    private String cartNumber;// 编号 相当于唯一编号
    private Long startTime;// 开始时间
    private Long endTime;// 结束时间
    private Integer timeId;// 关联选择时间
    private Integer timeLength;// 时长
    private Float startConsume;// 人均消费(开始)
    private Float endConsume;// 人均消费(结束)
    private Integer number;// 刊位个数
    private Integer purpose;// 刊位用途(1增加频次,2延长时长)
    private String devType;// 设备类型
    private String screenLocation;// 屏幕位置
    private Integer direction;// 屏方向
    // private Map<String,Object> areaInfo;// 投放区域(废弃有问题)
    private String province;
    private String city;
    private String area;
    private List<String> businessArr;// 商圈范围
    private String mac;// 设备MAC
    private String sellerName;// 商家名称
    private List<String> sellerType;// 商家类型
    private List<String> tradeArr;// 行业类型
    private Integer randomNumber;// 随机选中个数
    private Integer totalTime;// 从配置文件中获取设置的可售时长
    private Integer userId;// 用户id
    private List<ScreenInfoReq> screenArr;// 选择屏集合
    private String timeBucket;// 时间段
    private Integer system;// 所属系统 0:播控、1:销控
    private Integer pointType;// 点位类型 0:选择、1:赠送
    private List<String> quality;// 刊位质量
    private Float startCustomerFlow;// 开始日客流量
    private Float endCustomerFlow;// 结束日客流量
    private Integer orderType;// 订单类型 (0商业 1试投 2紧急)
    private String deviceState;// 设备状态 0离线1在线

    private String screenNumber;// 资产编号
    private List<String> screenNumberList;// 资产编号集合
    private String cityName;// 城市名称
    private boolean detail;// 是否查看详情
    private boolean fromSale;// 来源销控

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public Integer getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(Integer totalTime) {
        this.totalTime = totalTime;
    }

    public Integer getTimeId() {
        return timeId;
    }

    public void setTimeId(Integer timeId) {
        this.timeId = timeId;
    }

    public Integer getTimeLength() {
        return timeLength;
    }

    public void setTimeLength(Integer timeLength) {
        this.timeLength = timeLength;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Integer getPurpose() {
        return purpose;
    }

    public void setPurpose(Integer purpose) {
        this.purpose = purpose;
    }

    public List<String> getBusinessArr() {
        return businessArr;
    }

    public void setBusinessArr(List<String> businessArr) {
        this.businessArr = businessArr;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public List<String> getTradeArr() {
        return tradeArr;
    }

    public void setTradeArr(List<String> tradeArr) {
        this.tradeArr = tradeArr;
    }

    public String getCartNumber() {
        return cartNumber;
    }

    public void setCartNumber(String cartNumber) {
        this.cartNumber = cartNumber;
    }

    public Integer getSelectMode() {
        return selectMode;
    }

    public void setSelectMode(Integer selectMode) {
        this.selectMode = selectMode;
    }

    public Integer getPlayMode() {
        return playMode;
    }

    public void setPlayMode(Integer playMode) {
        this.playMode = playMode;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public Float getStartConsume() {
        return startConsume;
    }

    public void setStartConsume(Float startConsume) {
        this.startConsume = startConsume;
    }

    public Float getEndConsume() {
        return endConsume;
    }

    public void setEndConsume(Float endConsume) {
        this.endConsume = endConsume;
    }

    public Integer getRandomNumber() {
        return randomNumber;
    }

    public void setRandomNumber(Integer randomNumber) {
        this.randomNumber = randomNumber;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public List<ScreenInfoReq> getScreenArr() {
        return screenArr;
    }

    public void setScreenArr(List<ScreenInfoReq> screenArr) {
        this.screenArr = screenArr;
    }

    public String getTimeBucket() {
        return timeBucket;
    }

    public void setTimeBucket(String timeBucket) {
        this.timeBucket = timeBucket;
    }

    public Integer getSystem() {
        return system;
    }

    public void setSystem(Integer system) {
        this.system = system;
    }

    public Integer getPointType() {
        return pointType;
    }

    public void setPointType(Integer pointType) {
        this.pointType = pointType;
    }

    public String getScreenNumber() {
        return screenNumber;
    }

    public void setScreenNumber(String screenNumber) {
        this.screenNumber = screenNumber;
    }

    public List<String> getScreenNumberList() {
        return screenNumberList;
    }

    public void setScreenNumberList(List<String> screenNumberList) {
        this.screenNumberList = screenNumberList;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getDevType() {
        return devType;
    }

    public void setDevType(String devType) {
        this.devType = devType;
    }

    public String getScreenLocation() {
        return screenLocation;
    }

    public void setScreenLocation(String screenLocation) {
        this.screenLocation = screenLocation;
    }

    public Float getStartCustomerFlow() {
        return startCustomerFlow;
    }

    public void setStartCustomerFlow(Float startCustomerFlow) {
        this.startCustomerFlow = startCustomerFlow;
    }

    public Float getEndCustomerFlow() {
        return endCustomerFlow;
    }

    public void setEndCustomerFlow(Float endCustomerFlow) {
        this.endCustomerFlow = endCustomerFlow;
    }

    public Integer getDirection() {
        return direction;
    }

    public void setDirection(Integer direction) {
        this.direction = direction;
    }

    public Integer getOrderType() {
        return orderType;
    }

    public void setOrderType(Integer orderType) {
        this.orderType = orderType;
    }

    public String getDeviceState() {
        return deviceState;
    }

    public void setDeviceState(String deviceState) {
        this.deviceState = deviceState;
    }

    public List<String> getSellerType() {
        return sellerType;
    }

    public void setSellerType(List<String> sellerType) {
        this.sellerType = sellerType;
    }

    public List<String> getQuality() {
        return quality;
    }

    public void setQuality(List<String> quality) {
        this.quality = quality;
    }

    public boolean isDetail() {
        return detail;
    }

    public void setDetail(boolean detail) {
        this.detail = detail;
    }

    public boolean isFromSale() {
        return fromSale;
    }

    public void setFromSale(boolean fromSale) {
        this.fromSale = fromSale;
    }
}
