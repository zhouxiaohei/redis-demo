package com.zhou.demo.redis.controller;

import com.zhou.demo.redis.service.HystrixService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName HystrixDemoController
 * @Author JackZhou
 * @Date 2020/8/20  17:23
 **/

@RestController
@RequestMapping("/clnDemo/hystrix")
@Slf4j
@Api(tags = "hystrix超时测试")
public class HystrixDemoController {

    @Autowired
    HystrixService hystrixService;

    @ApiOperation(value = "测试hystrix超时1")
    @RequestMapping(value = "/test1", method = RequestMethod.GET)
    public String test1(){
        return hystrixService.hystrixDemo("张三");
    }

    @ApiOperation(value = "测试hystrix超时2,redis连接")
    @RequestMapping(value = "/test2", method = RequestMethod.GET)
    public String test2(){
        return hystrixService.redisConnectDemo("张三");
    }


    @ApiOperation(value = "测试hystrix超时3,http连接")
    @RequestMapping(value = "/test3", method = RequestMethod.GET)
    public String test3(){
        return hystrixService.httpDemo("张三");
    }
}
