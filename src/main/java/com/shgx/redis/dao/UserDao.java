package com.shgx.redis.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.shgx.redis.model.User;

@Mapper
public interface UserDao {

    void delete(String uuid);

    int update(@Param("ruser") User user);

    User findByUuid(String uuid);

    int save(@Param("ruser") User user) throws Exception;
}
