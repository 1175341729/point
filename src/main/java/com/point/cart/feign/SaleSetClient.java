package com.point.cart.feign;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient("sale-saleset")
public interface SaleSetClient {

    @PostMapping("/saleset/algorithm/algorithmArray")
    public JSONObject getTimeAndAmountAndStatus(@RequestBody JSONArray param);
}
