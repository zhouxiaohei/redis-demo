package com.zhou.demo.redis.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName TestService
 * @Author JackZhou
 * @Date 2020/7/30  14:12
 **/

@Slf4j
@Service
public class TestService {

    @Autowired
    private RedisTemplate redisTemplate;

    public void multiErrorTest(){
        try{
            String key = "mutltiTest";
            redisTemplate.delete(key);

            Runnable runnable = (() -> {
                int count = 0;
                String value = (String) redisTemplate.opsForValue().get(key);
                if (!StringUtils.isEmpty(value)) {
                    count = Integer.parseInt(value);
                }
                count = count + 1;
                redisTemplate.opsForValue().set(key, String.valueOf(count));
            });

            List<Thread> threads = new ArrayList<>();

            for (int i = 0; i < 10; i++) {
                Thread thread = new Thread(runnable, "thread-" + (i + 1));
                thread.start();
                threads.add(thread);
            }

            for (Thread thread : threads) {
                thread.join();
            }

            log.info("最终：value:" + redisTemplate.opsForValue().get(key));
        }catch(Exception e){
            log.info("执行报错:", e);
        }
    }

    public void test(){
//        Object executeResult = redisTemplate.execute(new SessionCallback<List<Object>>() {
//            public List<Object> execute(RedisOperations operations) throws DataAccessException {
//                operations.multi();
//                operations.opsForSet().add("key", "value1");
//                return operations.exec();
//            }
//        });
        String key = "mutltiTest";
        redisTemplate.delete(key);

        Runnable runnable = (() -> {
            redisTemplate.execute(new SessionCallback<Object>() {

                @Override
                //@SuppressWarnings({ "unchecked", "rawtypes" })
                public Object execute(RedisOperations redisOperations) throws DataAccessException {
                    List<Object> result = null;
                    do {
                        int count = 0;
                        redisOperations.watch(key);  // watch某个key,当该key被其它客户端改变时,则会中断当前的操作
                        String value = (String) redisOperations.opsForValue().get(key);
                        if (!StringUtils.isEmpty(value)) {
                            count = Integer.parseInt(value);
                        }
                        count = count + 1;
                        redisOperations.multi(); //开始事务
                        redisOperations.opsForValue().set("11", String.valueOf(count));
                        try {
                            result = redisOperations.exec(); //提交事务
                        } catch (Exception e) { //如果key被改变,提交事务时这里会报异常

                        }
                    } while (result == null); //如果失败则重试
                    return null;
                }
            });
        });

    }
}
