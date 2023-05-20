package com.haibin.springsecurity.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginDemo {

    @RequestMapping("/mylogin")
    public String login() {
        return "login";
    }
}
