package com.haibin.boot.controller;

import com.haibin.boot.entity.User;
import com.haibin.boot.exception.SimpleException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @RequestMapping("/hello")
    public String hello() throws SimpleException {
        //throw new NullPointerException();
        throw new SimpleException();
        //return "Hello World!";
        //return 1/0 + "";
        /*try{
            int a = 1/0;
        }catch (Exception e){
            throw new NullPointerException();
        }*/
        //return "hello";
    }

    @GetMapping("/user")
    private User user() {
        User user = new User();
        user.setId(1L);
        user.setName("shenhaibin");
        user.setEmail("hbshencon@163.com");
        user.setAge(18);
        return user;
    }


}
