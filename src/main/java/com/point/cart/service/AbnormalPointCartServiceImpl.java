package com.point.cart.service;

import com.alibaba.fastjson.JSON;
import com.point.cart.common.rsp.MessageRsp;
import com.point.cart.common.rsp.MessageUtil;
import com.point.cart.common.utils.AreaUtil;
import com.point.cart.common.utils.ExcelUtil;
import com.point.cart.mapper.AbnormalCartPointExMapper;
import com.point.cart.mapper.AbnormalCartPointMapper;
import com.point.cart.model.AbnormalCartPoint;
import com.point.cart.model.AbnormalCartPointExample;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  异常点位
 */
@Service
public class AbnormalPointCartServiceImpl implements AbnormalPointCartService {
    @Resource
    private AbnormalCartPointExMapper abnormalCartPointExMapper;

    @Resource
    private AbnormalCartPointMapper abnormalCartPointMapper;
    @Override
    public MessageRsp searchArea(String areaType, String areaName, Integer level) {
        if ("province".equals(areaType) && level != null && level > 3) {
            return MessageUtil.error("查询省时层级不能大于3");
        } else if ("city".equals(areaType) && level != null && level > 2) {
            return MessageUtil.error("查询市时层级不能大于2");
        } else if ("area".equals(areaType) && level != null && level > 1) {
            return MessageUtil.error("查询区县时层级不能大于1");
        }

        if ("city".equals(areaType) && level != null) {
            level += 1;
        }
        if ("area".equals(areaType) && level != null) {
            level += 2;
        }
        if (level == null) {
            level = 3;
        }
        Map<String, Object> param = new HashMap<>();
        if (StringUtils.isNotBlank(areaType)) param.put("areaType", areaType);
        if (StringUtils.isNotBlank(areaName)) param.put("areaName", areaName);
        param.put("level", level);
        Map<String, Map<String, List<String>>> provinceMap = new HashMap<>();
        List<Map<String, Object>> areaMapList = abnormalCartPointExMapper.areaList(param);
        areaMapList.forEach(map -> {
            String province = String.valueOf(map.get("province"));
            String city = map.get("city") == null ? null : String.valueOf(map.get("city"));
            String area = map.get("area") == null ? null : String.valueOf(map.get("area"));
            Map<String, List<String>> cityMap = provinceMap.get(province);
            if (cityMap == null) {
                cityMap = new HashMap<>();
            }

            List<String> areaList = cityMap.get(city);
            if (areaList == null) {
                areaList = new ArrayList<>();
            }
            if (StringUtils.isNotBlank(area)) {
                areaList.add(area);
            }
            if (StringUtils.isNotBlank(city)) {
                cityMap.put(city, areaList);
            }
            provinceMap.put(province, cityMap);
        });
        List<Map<String, Object>> resultData = AreaUtil.createResultData(provinceMap);
        return MessageUtil.success(resultData);
    }

    /**
     * 查询列表
     * @param req 请求
     * @return 响应
     */
    @Override
    public MessageRsp searchList(AbnormalCartPoint req,Integer limit,Integer offset) {
        MessageRsp rsp = new MessageRsp();
        String province = req.getProvince(); // 省
        String city = req.getCity();// 市
        String area = req.getArea();// 区
        boolean flag = AreaUtil.checkArea(province, city, area);
        if (!flag) {
            return MessageUtil.error("省市区选择有误");
        }
        String mac = req.getMac(); // mac
        String propertyNumber = req.getPropertyNumber();
        AbnormalCartPointExample example = new AbnormalCartPointExample();
        AbnormalCartPointExample.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotBlank(province)) criteria.andProvinceEqualTo(province);
        if (StringUtils.isNotBlank(city)) criteria.andCityEqualTo(city);
        if (StringUtils.isNotBlank(area)) criteria.andAreaEqualTo(area);
        if (StringUtils.isNotBlank(mac))criteria.andMacLike("%" + mac + "%");
        if (StringUtils.isNotBlank(propertyNumber))criteria.andPropertyNumberLike("%" + propertyNumber + "%");
        example.setOrderByClause("province");

        Integer totalPages = 0;
        Integer limitRsp = limit;
        Integer offsetRsp = offset;
        int count = abnormalCartPointMapper.countByExample(example);// 查询总数
        if (count > 0){
            totalPages = 1;
            if (offset != null && offset > 0){
                totalPages = count % limit == 0 ? (count / limit) :(count / limit) + 1;
                example.setOffset((offset - 1) * limit);
                example.setLimit(limit);
            } else {
                limitRsp = count;
                offsetRsp = 1;
            }
        }
        List<AbnormalCartPoint> abnormalCartPoints = abnormalCartPointMapper.selectByExample(example);// 查询数据
        Map<String,Object> result = new HashMap<>();
        result.put("offset",offsetRsp);
        result.put("limit",limitRsp);
        result.put("totalPages",totalPages);
        result.put("total",count);
        result.put("orderList",abnormalCartPoints);
        rsp.setData(result);
        return rsp;
    }

    /**
     * 导出excel
     * @return 响应
     */
    @Override
    public MessageRsp export(AbnormalCartPoint req) {
        MessageRsp rsp = new MessageRsp();
        Map<String,String> result = new HashMap<>();
        // 根据参数导出
        MessageRsp paramRsp = searchList(req, null, null);
        Map<String,Object> resultMap = (Map<String, Object>) paramRsp.getData();
        if (resultMap != null && resultMap.size() > 0){
            List<AbnormalCartPoint> abnormalCartPoints = (List<AbnormalCartPoint>) resultMap.get("orderList");
            if (abnormalCartPoints != null && abnormalCartPoints.size() > 0){
                String sheetName = "异常点位";
                String[] titles = new String[]{"资产编号","MAC","商家名称","省","市","区","地址","异常原因"};
                String[] fields = new String[]{"propertyNumber","mac","sellerName","province","city","area","address","fault"};
                String ossKey = ExcelUtil.exportExcel(abnormalCartPoints, sheetName, titles, fields);
                result.put("filePath",ossKey);
                rsp.setData(result);
            }else {
                return MessageUtil.error("无数据！");
            }
        } else {
            return MessageUtil.error("无数据！");
        }
        return rsp;
    }
}
