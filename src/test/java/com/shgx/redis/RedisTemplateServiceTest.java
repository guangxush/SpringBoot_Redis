package com.shgx.redis;

import com.shgx.redis.pojo.User;
import com.shgx.redis.services.RedisTemplateService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class RedisTemplateServiceTest {

    @Autowired
    RedisTemplateService redisTemplateService;

    @Autowired
    User user;

    @Test
    public void redisTest(){

        user.setId(11);
        user.setName("test");
        user.setPassword("hello redis");
        redisTemplateService.set("key1",user);

        User us = redisTemplateService.get("key1",User.class);
        System.out.println(us.getName()+":  "+us.getPassword());
    }
}
