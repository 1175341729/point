package com.point.cart.message.rsp;

public class ContractCityTotalRsp {
    private String cityName;
    private Integer total;

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }
}
