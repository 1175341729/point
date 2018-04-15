package com.point.cart.message.req;

public class ModifyCartPointReq {
    private String cartNumber;
    private String cityName;
    private Integer system;
    private Integer pointType;
    private Integer orderType;// 订单类型
    private CartScreenInfoReq targetData;
    private CartScreenInfoReq putData;

    public CartScreenInfoReq getTargetData() {
        return targetData;
    }

    public void setTargetData(CartScreenInfoReq targetData) {
        this.targetData = targetData;
    }

    public CartScreenInfoReq getPutData() {
        return putData;
    }

    public void setPutData(CartScreenInfoReq putData) {
        this.putData = putData;
    }

    public String getCartNumber() {
        return cartNumber;
    }

    public void setCartNumber(String cartNumber) {
        this.cartNumber = cartNumber;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
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

    public Integer getOrderType() {
        return orderType;
    }

    public void setOrderType(Integer orderType) {
        this.orderType = orderType;
    }
}
