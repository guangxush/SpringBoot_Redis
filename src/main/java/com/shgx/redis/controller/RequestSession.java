package com.shgx.redis.controller;

import com.alibaba.fastjson.JSON;
import com.shgx.redis.pojo.User;
import com.shgx.redis.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/session")
public class RequestSession {

    @Autowired
    UserService userService;

    @RequestMapping("testSessionTimeOut")
    public String testSessionTimeOut(Long id, HttpSession session, Model model){
        User user = userService.getUserById(id);
        System.out.println("sessionId-------->"+session.getId());
        model.addAttribute("user", JSON.toJSONString(user));
        session.setAttribute("user",JSON.toJSONString(user));
        return "hello world";
    }
}
