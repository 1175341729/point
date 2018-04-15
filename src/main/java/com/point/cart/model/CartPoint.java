package com.point.cart.model;

public class CartPoint {
    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column t_cart_point.cart_point_id
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    private Integer cartPointId;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column t_cart_point.cart_number
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    private String cartNumber;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column t_cart_point.user_id
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    private Integer userId;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column t_cart_point.time_id
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    private Integer timeId;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column t_cart_point.time_length
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    private Integer timeLength;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column t_cart_point.city_code
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    private String cityCode;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column t_cart_point.city_name
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    private String cityName;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column t_cart_point.screen_number
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    private String screenNumber;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column t_cart_point.number
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    private Integer number;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column t_cart_point.purpose
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    private Integer purpose;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column t_cart_point.state
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    private Integer state;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column t_cart_point.start_time
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    private Long startTime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column t_cart_point.end_time
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    private Long endTime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column t_cart_point.create_time
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    private Long createTime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column t_cart_point.time_bucket
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    private String timeBucket;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column t_cart_point.select_mode
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    private Integer selectMode;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column t_cart_point.play_mode
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    private Integer playMode;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column t_cart_point.system
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    private Integer system;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column t_cart_point.point_type
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    private Integer pointType;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column t_cart_point.time_total
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    private Integer timeTotal;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column t_cart_point.order_type
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    private Integer orderType;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column t_cart_point.cart_point_id
     *
     * @return the value of t_cart_point.cart_point_id
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    public Integer getCartPointId() {
        return cartPointId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column t_cart_point.cart_point_id
     *
     * @param cartPointId the value for t_cart_point.cart_point_id
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    public void setCartPointId(Integer cartPointId) {
        this.cartPointId = cartPointId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column t_cart_point.cart_number
     *
     * @return the value of t_cart_point.cart_number
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    public String getCartNumber() {
        return cartNumber;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column t_cart_point.cart_number
     *
     * @param cartNumber the value for t_cart_point.cart_number
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    public void setCartNumber(String cartNumber) {
        this.cartNumber = cartNumber == null ? null : cartNumber.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column t_cart_point.user_id
     *
     * @return the value of t_cart_point.user_id
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    public Integer getUserId() {
        return userId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column t_cart_point.user_id
     *
     * @param userId the value for t_cart_point.user_id
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column t_cart_point.time_id
     *
     * @return the value of t_cart_point.time_id
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    public Integer getTimeId() {
        return timeId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column t_cart_point.time_id
     *
     * @param timeId the value for t_cart_point.time_id
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    public void setTimeId(Integer timeId) {
        this.timeId = timeId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column t_cart_point.time_length
     *
     * @return the value of t_cart_point.time_length
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    public Integer getTimeLength() {
        return timeLength;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column t_cart_point.time_length
     *
     * @param timeLength the value for t_cart_point.time_length
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    public void setTimeLength(Integer timeLength) {
        this.timeLength = timeLength;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column t_cart_point.city_code
     *
     * @return the value of t_cart_point.city_code
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    public String getCityCode() {
        return cityCode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column t_cart_point.city_code
     *
     * @param cityCode the value for t_cart_point.city_code
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    public void setCityCode(String cityCode) {
        this.cityCode = cityCode == null ? null : cityCode.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column t_cart_point.city_name
     *
     * @return the value of t_cart_point.city_name
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    public String getCityName() {
        return cityName;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column t_cart_point.city_name
     *
     * @param cityName the value for t_cart_point.city_name
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    public void setCityName(String cityName) {
        this.cityName = cityName == null ? null : cityName.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column t_cart_point.screen_number
     *
     * @return the value of t_cart_point.screen_number
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    public String getScreenNumber() {
        return screenNumber;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column t_cart_point.screen_number
     *
     * @param screenNumber the value for t_cart_point.screen_number
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    public void setScreenNumber(String screenNumber) {
        this.screenNumber = screenNumber == null ? null : screenNumber.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column t_cart_point.number
     *
     * @return the value of t_cart_point.number
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    public Integer getNumber() {
        return number;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column t_cart_point.number
     *
     * @param number the value for t_cart_point.number
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    public void setNumber(Integer number) {
        this.number = number;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column t_cart_point.purpose
     *
     * @return the value of t_cart_point.purpose
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    public Integer getPurpose() {
        return purpose;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column t_cart_point.purpose
     *
     * @param purpose the value for t_cart_point.purpose
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    public void setPurpose(Integer purpose) {
        this.purpose = purpose;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column t_cart_point.state
     *
     * @return the value of t_cart_point.state
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    public Integer getState() {
        return state;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column t_cart_point.state
     *
     * @param state the value for t_cart_point.state
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    public void setState(Integer state) {
        this.state = state;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column t_cart_point.start_time
     *
     * @return the value of t_cart_point.start_time
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    public Long getStartTime() {
        return startTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column t_cart_point.start_time
     *
     * @param startTime the value for t_cart_point.start_time
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column t_cart_point.end_time
     *
     * @return the value of t_cart_point.end_time
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    public Long getEndTime() {
        return endTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column t_cart_point.end_time
     *
     * @param endTime the value for t_cart_point.end_time
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column t_cart_point.create_time
     *
     * @return the value of t_cart_point.create_time
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    public Long getCreateTime() {
        return createTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column t_cart_point.create_time
     *
     * @param createTime the value for t_cart_point.create_time
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column t_cart_point.time_bucket
     *
     * @return the value of t_cart_point.time_bucket
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    public String getTimeBucket() {
        return timeBucket;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column t_cart_point.time_bucket
     *
     * @param timeBucket the value for t_cart_point.time_bucket
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    public void setTimeBucket(String timeBucket) {
        this.timeBucket = timeBucket == null ? null : timeBucket.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column t_cart_point.select_mode
     *
     * @return the value of t_cart_point.select_mode
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    public Integer getSelectMode() {
        return selectMode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column t_cart_point.select_mode
     *
     * @param selectMode the value for t_cart_point.select_mode
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    public void setSelectMode(Integer selectMode) {
        this.selectMode = selectMode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column t_cart_point.play_mode
     *
     * @return the value of t_cart_point.play_mode
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    public Integer getPlayMode() {
        return playMode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column t_cart_point.play_mode
     *
     * @param playMode the value for t_cart_point.play_mode
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    public void setPlayMode(Integer playMode) {
        this.playMode = playMode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column t_cart_point.system
     *
     * @return the value of t_cart_point.system
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    public Integer getSystem() {
        return system;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column t_cart_point.system
     *
     * @param system the value for t_cart_point.system
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    public void setSystem(Integer system) {
        this.system = system;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column t_cart_point.point_type
     *
     * @return the value of t_cart_point.point_type
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    public Integer getPointType() {
        return pointType;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column t_cart_point.point_type
     *
     * @param pointType the value for t_cart_point.point_type
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    public void setPointType(Integer pointType) {
        this.pointType = pointType;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column t_cart_point.time_total
     *
     * @return the value of t_cart_point.time_total
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    public Integer getTimeTotal() {
        return timeTotal;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column t_cart_point.time_total
     *
     * @param timeTotal the value for t_cart_point.time_total
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    public void setTimeTotal(Integer timeTotal) {
        this.timeTotal = timeTotal;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column t_cart_point.order_type
     *
     * @return the value of t_cart_point.order_type
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    public Integer getOrderType() {
        return orderType;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column t_cart_point.order_type
     *
     * @param orderType the value for t_cart_point.order_type
     *
     * @mbggenerated Mon Jan 08 10:29:06 CST 2018
     */
    public void setOrderType(Integer orderType) {
        this.orderType = orderType;
    }
}