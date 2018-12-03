package com.shgx.redis.services;

import com.shgx.redis.pojo.User;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService{

    @Override
    public User getUserById(Long id) {
        //模拟从DB中获取数据
        //User user = DBHelper.getUser(id);
        return new User("shgx",18);
    }
}
