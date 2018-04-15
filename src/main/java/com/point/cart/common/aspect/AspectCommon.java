package com.point.cart.common.aspect;

import com.alibaba.fastjson.JSON;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AspectCommon {
    private Logger logger = LoggerFactory.getLogger(AspectCommon.class);

    @Pointcut(value = "execution(* com.point.cart.controller.*.*(..))")
    public void param(){

    }

    @Around(value = "param()")
    public Object arround(ProceedingJoinPoint point) throws Throwable {
        try {
            Object[] args = point.getArgs();
            try {
                logger.info("请求方法：{}",point.getSignature().getDeclaringTypeName() + "." + point.getSignature().getName());
                logger.info("请求参数：{}", JSON.toJSONString(args));
            } catch (Exception e) {
                logger.info("请求方式可能是form-data:");
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        return point.proceed();
    }
}
