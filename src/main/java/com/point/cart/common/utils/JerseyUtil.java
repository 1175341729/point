package com.point.cart.common.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import org.codehaus.jettison.json.JSONArray;

import javax.ws.rs.core.MediaType;

public class JerseyUtil {

    /**
     * 发送post-json数据
     * @param url
     * @param params
     * @return
     */
    public static JSONObject sendPostByJson(String url, JSONArray params){
        JSONObject result = null;
        ClientConfig cc = new DefaultClientConfig();
        cc.getProperties()
                .put(ClientConfig.PROPERTY_CONNECT_TIMEOUT, 10 * 1000);
        Client client = Client.create(cc);

        WebResource resource = client.resource(url);
        ClientResponse response = resource
                .header("Content-Type", "application/json")
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .post(ClientResponse.class, params);

        int status = response.getStatus();
        if (status == 200){
            String resultEntity = response.getEntity(String.class);
            result = JSON.parseObject(resultEntity);
        }

        return result;
    }
}
