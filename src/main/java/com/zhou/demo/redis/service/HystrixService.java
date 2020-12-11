package com.zhou.demo.redis.service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.zhou.demo.redis.utils.OkhttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @ClassName HystrixService
 * @Author JackZhou
 * @Date 2020/3/18  11:26
 **/
@Service
@Slf4j
public class HystrixService {

    @Autowired
    private RedisTemplate redisTemplate;

    @HystrixCommand(fallbackMethod = "simpleFallback",
            commandProperties = {
                    @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "1000" )
            })
    // 测试结论 ： hystrix的中断和使用interrupt方法  并不会中断正在执行的方法
    // 不使用其他组件的情况下  需要 @EnableCircuitBreaker注解
    public String hystrixDemo(String name){
        log.info("接受到参数 {}", name);
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            log.info("finally 代码执行");
        }
        log.info("外部代执行");
        return "接收到参数：" + name ;
    }

    @HystrixCommand(fallbackMethod = "simpleFallback",
            commandProperties = {
                    @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "2000" )
            })
    //测试结论  redisTemplate的底层应该判断了interrupt状态，后面的语句 log.info("外部代执行结束） 不执行
    // 所以redisTemPlate应该是结束的当前线程
    public String redisConnectDemo(String name){
        log.info("接受到参数 {}", name);
        for(int i =0;i <20; i++){
            Object taskId = redisTemplate.opsForList().leftPop("test1234List", 1, TimeUnit.SECONDS);
            log.info("执行完成{}次,结果{}", i, taskId);
        }

        log.info("外部代执行结束：{}", name);
        return "接收到参数：" + name ;
    }

    @HystrixCommand(fallbackMethod = "simpleFallback",
            commandProperties = {
                    @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "1000" )
            })
    //测试结论  Okhttp的底层应该判断了interrupt状态，java.io.InterruptedIOException: interrupted
            //但是OKhttp底层使用线程池，所以它结束的是它底层的线程，而不是上层的线程,上层的循环继续执行
    public String httpDemo(String name){
        log.info("接受到参数 {}", name);
        for(int i =0;i <100; i++){
            OkhttpUtils.execRequest("https://www.baidu.com/", null, null);
            log.info("执行完成{}次", i);
        }
        log.info("外部代执行结束 {}", name);
        return "接收到参数：" + name ;
    }


    public String simpleFallback(String name){
        log.info("执行降级流程");
        return "对参数："+ name + "降级处理";
    }


    public static void main(String[] args) {

        long begin = System.currentTimeMillis();
        for(int i =0;i <100; i++){
            OkhttpUtils.execRequest("https://www.baidu.com/", null, null);
            log.info("执行完成{}次", i);
        }
        log.info("耗时{}", System.currentTimeMillis() - begin);
    }
}
