package com.zhou.demo.redis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;

/**
 * @ClassName RedisApp
 * @Author JackZhou
 * @Date 2020/7/27  16:38
 **/

@SpringBootApplication
@EnableCircuitBreaker
public class RedisDemoApp {

    public static void main(String[] args) {
        SpringApplication.run(RedisDemoApp.class, args);
    }
    
}
