package com.haibin.mall.user.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    /**
     * 根据用户id查询用户信息
     * @param userId
     * @return
     */
    @RequestMapping("/findUserByUserId/{userId}")
    public String findOrderByUserId(@PathVariable("userId") Integer userId) {
        //模拟异常
        if(userId==5){
            throw new IllegalArgumentException("非法参数异常");
        }
        log.info("根据userId:"+userId+"查询订单信息");
        return "success";
    }

}
