package com.zhou.demo.redis.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName ClnTestController
 * @Author JackZhou
 * @Date 2020/7/31  10:17
 **/
@RestController
@RequestMapping("/clnDemo/redis/")
@Slf4j
@Api(tags = "简单redis list和 set测试")
public class ClnTestController {

    @Autowired
    RedisTemplate redisTemplate;

    @ApiOperation(value = "多个value,分割，保存到set中")
    @RequestMapping(value = "/saveToSet/{key}", method = RequestMethod.POST)
    public Long save(@PathVariable("key") String key, @RequestParam String values){
        log.info("保存values：{}", values);
        // set保存，返回保存长度；重复项-1
       return redisTemplate.opsForSet().add(key, values.split(","));
    }

    @ApiOperation(value = "将set中的值，移动到list中")
    @RequestMapping(value = "/moveToList", method = RequestMethod.POST)
    public long move(@RequestParam String setKey, @RequestParam String listKey){
        log.info("将set{}中的值，移动到list{}中：", setKey, listKey);
        Set members = redisTemplate.opsForSet().members(setKey);
        log.info("将要copy的set长度为{}，values{}", redisTemplate.opsForSet().size(setKey), members.toArray());
        // 返回list长度
        return redisTemplate.opsForList().leftPushAll(listKey, members);
    }

    @ApiOperation(value = "从list弹出一个值")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "direction", value = "方向", dataType = "String", paramType = "query",
                    allowableValues = "left,right")
    })
    @RequestMapping(value = "/listPop", method = RequestMethod.GET)
    public Object lkstPop(@RequestParam String listKey, @RequestParam String direction){
        if(direction.equalsIgnoreCase("right")){
            return redisTemplate.opsForList().rightPop(listKey);
        }
        return redisTemplate.opsForList().leftPop(listKey);
    }

    @ApiOperation(value = "从list弹出一个值,阻塞")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "direction", value = "方向", dataType = "String", paramType = "query",
                    allowableValues = "left,right")
    })
    @RequestMapping(value = "/listPopBlock", method = RequestMethod.GET)
    public Object lkstPopBlock(@RequestParam String listKey, @RequestParam String direction){
        log.info("接到请求");
        if(direction.equalsIgnoreCase("right")){
            // 0   TimeUnit.SECONDS 代表，一直阻塞  redisTemplate.opsForList().rightPop(listKey, 0, TimeUnit.SECONDS);
            // 阻塞指定时间后没有返回空 , 超时报错io.lettuce.core.RedisCommandTimeoutException: Command timed out after 100 second(s)
            // 默认1分钟，通过spring.redis.timeOut 设置
            Object o = redisTemplate.opsForList().rightPop(listKey,  2, TimeUnit.SECONDS);
            return o;
        }
        //Object o = redisTemplate.opsForList().leftPop(listKey, 2, TimeUnit.MINUTES);
        Object o = redisTemplate.opsForList().leftPop(listKey, 0, TimeUnit.SECONDS);
        log.info("完成请求");
        return o;
    }

}
