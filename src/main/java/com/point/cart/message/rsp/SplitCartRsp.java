package com.point.cart.message.rsp;

import java.util.List;
import java.util.Map;

public class SplitCartRsp {
    private Integer number;
    private Integer timeLength;
    private Long startTime;
    private Long endTime;
    private List<Map<String,Object>> typeScreen;

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
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

    public List<Map<String, Object>> getTypeScreen() {
        return typeScreen;
    }

    public void setTypeScreen(List<Map<String, Object>> typeScreen) {
        this.typeScreen = typeScreen;
    }
}
