package com.point.cart.message.req;

import java.util.List;

public class CartScreenInfoReq {
    private String cartNumber;// 购物车编号
    private String cityCode;// 城市代码
    private String cityName;// 城市名称
    private List<String> screenList;// 屏编号
    private Integer timeLength;// 时长
    private Long startTime;// 开始时间
    private Long endTime;// 结束时间
    private Integer number;// 刊位个数
    private Integer purpose;// 刊位用途
    private Integer system;// 所属系统
    private Integer pointType;// 点位类型

    public String getCartNumber() {
        return cartNumber;
    }

    public void setCartNumber(String cartNumber) {
        this.cartNumber = cartNumber;
    }

    public List<String> getScreenList() {
        return screenList;
    }

    public void setScreenList(List<String> screenList) {
        this.screenList = screenList;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public Integer getTimeLength() {
        return timeLength;
    }

    public void setTimeLength(Integer timeLength) {
        this.timeLength = timeLength;
    }

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

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
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

    public Integer getPurpose() {
        return purpose;
    }

    public void setPurpose(Integer purpose) {
        this.purpose = purpose;
    }
}
