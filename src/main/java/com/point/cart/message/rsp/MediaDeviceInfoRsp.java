package com.point.cart.message.rsp;

public class MediaDeviceInfoRsp {
    private String businessAdd;
    private String businessName;
    private Integer deviceStatus;
    private Integer deviceType;
    private String mac;
    private String propertynumber;

    public String getBusinessAdd() {
        return businessAdd;
    }

    public void setBusinessAdd(String businessAdd) {
        this.businessAdd = businessAdd;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public Integer getDeviceStatus() {
        return deviceStatus;
    }

    public void setDeviceStatus(Integer deviceStatus) {
        this.deviceStatus = deviceStatus;
    }

    public Integer getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(Integer deviceType) {
        this.deviceType = deviceType;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getPropertynumber() {
        return propertynumber;
    }

    public void setPropertynumber(String propertynumber) {
        this.propertynumber = propertynumber;
    }
}
