package com.zhou.demo.redis.controller;

import com.zhou.demo.redis.dao.bean.Person;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * @Description
 * @Author JackZhou
 **/
@RestController
@RequestMapping("/demo/redis/")
@Slf4j
@Api(tags = "简单redis测试")
public class DemoController {

    @Autowired
    RedisTemplate redisTemplate;

    @ApiOperation(value = "根据key(id)查询")
    @RequestMapping(value = "/{key}", method = RequestMethod.GET)
    public Object getById(@PathVariable("key") String key) throws InterruptedException {
        log.info("根据key {} 进行查询", key);
        Person person = (Person)redisTemplate.opsForValue().get(key);
        return person;
    }

    @ApiOperation("用id做key，保存")
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public void save(@RequestBody Person person){
        log.info("调用保存接口");
        //不存在就保存 存在就更新
        redisTemplate.opsForValue().set(person.getId(), person);
    }

    @ApiOperation("删除")
    @ApiImplicitParam(name = "key", value = "用户id")
    @RequestMapping(value = "/{key}", method = RequestMethod.DELETE)
    public Boolean delete(@PathVariable("key") String key){
        log.info("调用删除接口");
        // 不存在就返回false
        return  redisTemplate.delete(key);
    }

    @ApiOperation("更新")
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    public Object update(@ModelAttribute Person person){
        //得到旧值   不存在就保存 存在就更新
        Object andSet = redisTemplate.opsForValue().getAndSet(person.getId(), person);
        return andSet;
    }

    @ApiOperation("测试1")
    @RequestMapping(value = "/test3", method = RequestMethod.GET)
    public boolean test3(@ModelAttribute Person person){
        // 设置过期时间 10秒/ 不存在返回false
        return redisTemplate.expire(person.getName(), 120, TimeUnit.SECONDS);
    }

    @ApiOperation("测试2，不存在key就新建并设置过期时间")
    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public Boolean test(@ModelAttribute Person person){
        //test:张三  文件夹
        return redisTemplate.opsForValue().setIfAbsent(person.getName(), person, 10, TimeUnit.SECONDS);
    }

    @ApiOperation("测试3,存在就更新并设置过期时间,存在返回true，不存在返回false")
    @RequestMapping(value = "/test2", method = RequestMethod.GET)
    public Boolean test2(@ModelAttribute Person person){
        return redisTemplate.opsForValue().setIfPresent(person.getName(), person, 10, TimeUnit.SECONDS);
    }

}
