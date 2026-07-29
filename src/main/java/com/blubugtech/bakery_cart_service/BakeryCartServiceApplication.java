package com.blubugtech.bakery_cart_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.blubakery.common.security.security.MethodSecurityConfig;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@Import({MethodSecurityConfig.class, org.blubakery.common.feign.security.FeignClientInterceptor.class, org.blubakery.common.feign.feign.FeignConfig.class})
@EnableFeignClients
@EnableCaching
@EnableAsync
@EnableScheduling
public class BakeryCartServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BakeryCartServiceApplication.class, args);
    }

}

