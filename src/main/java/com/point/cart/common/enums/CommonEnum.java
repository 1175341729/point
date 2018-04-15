package com.point.cart.common.enums;

public class CommonEnum {
    public enum Message {
        SUCCESS(0, "成功"), ERROR(1, "请求失败"), RUNTIMEEXCEPTION(2, "未知异常"), AUTHEXCEPTION(3, "认证失败");

        private Integer code;
        private String message;

        Message(Integer code, String message) {
            this.code = code;
            this.message = message;
        }

        public Integer getCode() {
            return code;
        }

        public void setCode(Integer code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    /**
     * 订单类型
     */
    public enum OrderType {
        BUSINESS(0, "商业"), TRY(1, "试投"), URGENCY(2, "紧急");
        private Integer orderType;
        private String typeName;

        OrderType(int orderType, String typeName) {
            this.orderType = orderType;
            this.typeName = typeName;
        }

        public Integer getOrderType() {
            return orderType;
        }

        public void setOrderType(Integer orderType) {
            this.orderType = orderType;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
    }

    /**
     * 点位类型
     */
    public enum PointType {
        CHOOSE(0, "选择"), GIVE_AWAY(1, "赠送");
        private Integer pointType;
        private String typeName;

        PointType(int pointType, String typeName) {
            this.pointType = pointType;
            this.typeName = typeName;
        }

        public Integer getPointType() {
            return pointType;
        }

        public void setPointType(Integer pointType) {
            this.pointType = pointType;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
    }

    /**
     * 系统类型
     */
    public enum SystemType {
        PLAY(0, "播控"), SALE(1, "销控");
        private Integer systemType;
        private String typeName;

        SystemType(int systemType, String typeName) {
            this.systemType = systemType;
            this.typeName = typeName;
        }

        public Integer getSystemType() {
            return systemType;
        }

        public void setSystemType(Integer systemType) {
            this.systemType = systemType;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
    }

    /**
     * 系统类型
     */
    public enum StateType {
        DRAFT(0, "草稿"), FORMAL(1, "正式");
        private Integer stateType;
        private String typeName;

        StateType(int stateType, String typeName) {
            this.stateType = stateType;
            this.typeName = typeName;
        }

        public Integer getStateType() {
            return stateType;
        }

        public void setStateType(Integer stateType) {
            this.stateType = stateType;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
    }

    /**
     * 选点模式
     */
    public enum SelectMode {
        COMMON(0, "普通筛选"), SUPER(1, "超级筛选");
        private Integer selectType;
        private String typeName;

        SelectMode(Integer selectType, String typeName) {
            this.selectType = selectType;
            this.typeName = typeName;
        }

        public Integer getSelectType() {
            return selectType;
        }

        public void setSelectType(Integer selectType) {
            this.selectType = selectType;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
    }

    /**
     * 播放方式
     */
    public enum PlayMode {
        INSERT(0, "插入"), STOP_BUSINESS(1, "停播商业"), STOP_ALL(2, "停播所有");
        private Integer playType;
        private String typeName;

        PlayMode(Integer playType, String typeName) {
            this.playType = playType;
            this.typeName = typeName;
        }

        public Integer getPlayType() {
            return playType;
        }

        public void setPlayType(Integer playType) {
            this.playType = playType;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }
    }
}
