package com.point.cart.message.req;

public class ScreenInfoReq {
    private String cityCode;
    private String cityName;
    private String screenNumber;

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

    public String getScreenNumber() {
        return screenNumber;
    }

    public void setScreenNumber(String screenNumber) {
        this.screenNumber = screenNumber;
    }

    public ScreenInfoReq(String cityCode, String cityName, String screenNumber) {
        this.cityCode = cityCode;
        this.cityName = cityName;
        this.screenNumber = screenNumber;
    }

    public ScreenInfoReq() {
    }
}
