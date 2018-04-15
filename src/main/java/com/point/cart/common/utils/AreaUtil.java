package com.point.cart.common.utils;

import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 *  省市区工具
 */
public class AreaUtil {
    /**
     * 构建省市区
     */
    public static List<Map<String, Object>> createResultData(Map<String, Map<String, List<String>>> provinceMap) {
        List<Map<String, Object>> resultProList = new ArrayList<>();
        List<Map<String, Object>> resultCityList;
        List<Map<String, Object>> resultAreaList;
        Map<String, Object> resultProMap;
        Map<String, Object> resultCityMap;
        Map<String, Object> resultAreaMap;
        Set<Map.Entry<String, Map<String, List<String>>>> entries = provinceMap.entrySet();
        for (Map.Entry<String, Map<String, List<String>>> city : entries) {
            resultProMap = new HashMap<>();
            // 处理省
            String provinceName = city.getKey();
            if (StringUtils.isNotBlank(provinceName)) {
                resultProMap.put("name", provinceName);
                resultProMap.put("type", "province");
                // 处理市
                Map<String, List<String>> cityMap = city.getValue();
                if (cityMap != null && cityMap.size() > 0) {
                    resultCityList = new ArrayList<>();
                    Set<Map.Entry<String, List<String>>> cityEntries = cityMap.entrySet();
                    for (Map.Entry<String, List<String>> areaMap : cityEntries) {
                        resultCityMap = new HashMap<>();
                        String cityName = areaMap.getKey();
                        if (StringUtils.isNotBlank(cityName)) {
                            resultCityMap.put("name", cityName);
                            resultCityMap.put("type", "city");
                            // 处理区县
                            List<String> areaList = areaMap.getValue();
                            if (areaList != null && areaList.size() > 0) {
                                resultAreaList = new ArrayList<>();
                                for (String name : areaList) {
                                    resultAreaMap = new HashMap<>();
                                    resultAreaMap.put("name", name);
                                    resultAreaMap.put("type", "area");
                                    resultAreaList.add(resultAreaMap);
                                }
                                resultCityMap.put("list", resultAreaList);
                            }
                            resultCityList.add(resultCityMap);
                        }
                    }
                    resultProMap.put("list", resultCityList);
                }
                resultProList.add(resultProMap);
            }
        }
        return resultProList;
    }

    /**
     * 验证投放区域
     */
    public static boolean checkArea(String province, String city, String area) {
        Boolean flag = true;
        if (StringUtils.isNotBlank(area) && StringUtils.isBlank(city)) {
            flag = false;
        } else if (StringUtils.isNotBlank(city) && StringUtils.isBlank(province)) {
            flag = false;
        }
        return flag;
    }
}
