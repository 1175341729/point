package com.point.cart.message.rsp;

import java.util.List;

public class ContractCartNumberRsp {
    private Long startTime;
    private Long endTime;
    private Double time;
    private List<Integer> devType;
    private List<ContractTimeAndNumberRsp> timeAndNumberList;
    private List<ContractCityTotalRsp> cityTotalRsp;

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

    public Double getTime() {
        return time;
    }

    public void setTime(Double time) {
        this.time = time;
    }

    public List<Integer> getDevType() {
        return devType;
    }

    public void setDevType(List<Integer> devType) {
        this.devType = devType;
    }

    public List<ContractTimeAndNumberRsp> getTimeAndNumberList() {
        return timeAndNumberList;
    }

    public void setTimeAndNumberList(List<ContractTimeAndNumberRsp> timeAndNumberList) {
        this.timeAndNumberList = timeAndNumberList;
    }

    public List<ContractCityTotalRsp> getCityTotalRsp() {
        return cityTotalRsp;
    }

    public void setCityTotalRsp(List<ContractCityTotalRsp> cityTotalRsp) {
        this.cityTotalRsp = cityTotalRsp;
    }
}
