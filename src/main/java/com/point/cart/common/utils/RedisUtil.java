package com.point.cart.common.utils;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {
    @Resource
    private RedisTemplate<Object, Object> redisTemplate;

    /**
     * 设置缓存 value型
     * @param key key
     * @param value 值
     * @param time 有效时间
     */
    public void set(Object key,Object value,Long time){
        ValueOperations<Object, Object> valueOperations = redisTemplate.opsForValue();
        if (time == null){
            valueOperations.set(key,value);
        } else {
            valueOperations.set(key,value,time, TimeUnit.SECONDS);
        }
    }

    /**
     * 获取值  value型
     * @param key key
     * @return 响应
     */
    public Object get(Object key){
        ValueOperations<Object, Object> valueOperations = redisTemplate.opsForValue();
        Object value = valueOperations.get(key);
        return value;
    }

    /**
     * 删除缓存
     * @param key key值
     */
    public void delete(Object key){
        redisTemplate.delete(key);
    }

    /**
     * 批量删除
     * @param keyPre key前缀
     */
    public void batchDelete(String keyPre){
        Set<Object> keys = redisTemplate.keys(keyPre + "*");
        for (Object key : keys){
            redisTemplate.delete(key);
        }
    }
}
