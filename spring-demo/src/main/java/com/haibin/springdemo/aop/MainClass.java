package com.haibin.springdemo.aop;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class MainClass {

    public static void main(String[] args){
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(MainConfig.class);

        Calculate calculate = (Calculate)ctx.getBean("calculateImpl");
        int retVal = calculate.div(2,4);
    }

}
