package com.point.cart.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.point.cart.common.enums.CommonEnum;
import com.point.cart.common.rsp.MessageRsp;
import com.point.cart.common.rsp.MessageUtil;
import com.point.cart.common.rsp.PageMessage;
import com.point.cart.common.utils.AreaUtil;
import com.point.cart.mapper.MediaDeviceInfoExMapper;
import com.point.cart.message.rsp.MediaDeviceInfoRsp;
import com.point.cart.model.MediaDeviceInfo;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MediaDeviceInfoServiceImpl implements MediaDeviceInfoService{
    @Resource
    private MediaDeviceInfoExMapper mediaDeviceInfoExMapper;
    @Override
    public MessageRsp searchList(MediaDeviceInfo req, Integer limit, Integer offset) {
        String province = req.getProvince(); // 省
        String city = req.getCity();// 市
        String area = req.getArea();// 区
        boolean flag = AreaUtil.checkArea(province, city, area);
        if (!flag) {
            return MessageUtil.error("省市区选择有误");
        }
        String mac = req.getMac(); // mac
        String propertyNumber = req.getPropertyNumber();// 设备编号
        String sellerName = req.getSellerName();
        Map<String,Object> param = new HashMap<>();
        if (StringUtils.isNotBlank(province)) param.put("province",province);
        if (StringUtils.isNotBlank(city)) param.put("city",city);
        if (StringUtils.isNotBlank(area)) param.put("area",area);
        if (StringUtils.isNotBlank(mac)) param.put("mac","%" + mac + "%");
        if (StringUtils.isNotBlank(propertyNumber)) param.put("propertyNumber","%" + propertyNumber + "%");
        if (StringUtils.isNotBlank(sellerName)) param.put("sellerName","%" + sellerName + "%");

        Integer totalPages = 0;
        Integer limitRsp = limit;
        Integer offsetRsp = offset;
        int count = mediaDeviceInfoExMapper.count(param);// 查询总数
        if (count > 0){
            totalPages = 1;
            if (offset != null && offset > 0){
                totalPages = count % limit == 0 ? (count / limit) :(count / limit) + 1;
                param.put("offset",(offset - 1) * limit);
                param.put("limit",limit);
            } else {
                limitRsp = count;
                offsetRsp = 1;
            }
        }
        List<MediaDeviceInfoRsp> mediaDeviceList = mediaDeviceInfoExMapper.selectByExampleCustom(param);// 查询数据
        PageMessage<MediaDeviceInfoRsp> result = new PageMessage<>();
        result.setLimit(limitRsp);
        result.setOffset(offsetRsp);
        result.setTotalPage(totalPages);
        result.setTotal(count);
        result.setList(mediaDeviceList);
        return MessageUtil.success(result);
    }

    /**
     * 按照之前python 提供的接口
     * @param province 省
     * @param city 市
     * @param area 区
     * @return 响应
     */
    @Override
    public JSONObject mediaInfo(String province, String city, String area) {
        JSONObject bean = new JSONObject();
        boolean flag = AreaUtil.checkArea(province, city, area);
        if (!flag) {
            bean.put("code",CommonEnum.Message.ERROR.getCode());
            bean.put("message","省市区选择有误");
            return bean;
        }
        Map<String,Object> param = new HashMap<>();
        if (StringUtils.isNotBlank(province)) param.put("province",province);
        if (StringUtils.isNotBlank(city)) param.put("city",city);
        if (StringUtils.isNotBlank(area)) param.put("area",area);
        // param.put("state",1); 不去验证设备状态
        List<MediaDeviceInfoRsp> mediaDeviceList = mediaDeviceInfoExMapper.selectByExampleCustom(param);// 查询数据

        if (mediaDeviceList != null && mediaDeviceList.size() > 0){
            bean.put("code", CommonEnum.Message.SUCCESS.getCode());
            bean.put("message","成功");

            JSONObject data = new JSONObject();
            data.put("list",mediaDeviceList);
            bean.put("data",data);
        }

        return bean;
    }

    /**
     * 按照之前python 提供的接口
     * @param mac mac地址
     * @return 响应
     */
    @Override
    public JSONObject mediaInfoByMac(String mac) {
        Map<String,Object> param = new HashMap<>();
        JSONObject rsp = new JSONObject();
        rsp.put("status",CommonEnum.Message.SUCCESS.getCode());
        rsp.put("msg","成功");
        if (StringUtils.isNotBlank(mac)){
            param.put("realMac",mac);
            // param.put("state",1);
        } else {
            rsp.put("status",CommonEnum.Message.ERROR.getCode());
            rsp.put("msg","MAC不能为空");
            return rsp;
        }

        List<MediaDeviceInfoRsp> mediaDeviceList = mediaDeviceInfoExMapper.selectByExampleCustom(param);// 查询数据
        if (mediaDeviceList != null && mediaDeviceList.size() > 0){
            JSONArray Data = new JSONArray();
            mediaDeviceList.forEach(media -> {
                JSONObject obj = new JSONObject();
                obj.put("propertynumber",media.getPropertynumber());
                obj.put("mac",mac);
                Data.add(obj);
            });

            rsp.put("Data",Data);
        }
        return rsp;
    }
}
