package com.point.cart;

import feign.Request;
import feign.Retryer;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.point.cart.mapper")
public class PointApplication {

	public static void main(String[] args) {
		SpringApplication.run(PointApplication.class, args);
	}

	@Bean
	Request.Options feignOptions() {
		return new Request.Options(/**connectTimeoutMillis**/300 * 1000, /** readTimeoutMillis **/200 * 1000);
	}

	@Bean
	Retryer feignRetryer() {
		return Retryer.NEVER_RETRY;
	}
}
