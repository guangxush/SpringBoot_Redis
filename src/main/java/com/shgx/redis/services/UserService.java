package com.shgx.redis.services;

import com.shgx.redis.pojo.User;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    public User getUserById(Long id);
}
