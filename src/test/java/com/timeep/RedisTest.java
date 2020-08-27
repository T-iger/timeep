package com.timeep;

import com.timeep.service.MainService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * @Description:
 * @Author: lh
 * @Date 2020/8/25
 * @Version: 1.0
 **/
@SpringBootTest
public class RedisTest {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private MainService mainService;

    @Test
    void test(){
        System.out.println(mainService.reasoning());
//        System.out.println(mainService.findAllKnowledgePointSystem("Thing"));
        /*redisTemplate.opsForValue().set("k","1");
        System.out.println(redisTemplate.expire("k", 10, TimeUnit.SECONDS));
        System.out.println(redisTemplate.getExpire("k", TimeUnit.SECONDS));*/

    }



}
