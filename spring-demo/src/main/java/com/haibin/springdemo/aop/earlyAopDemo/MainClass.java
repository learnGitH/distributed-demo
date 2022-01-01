package com.haibin.springdemo.aop.earlyAopDemo;

import com.haibin.springdemo.aop.Calculate;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class MainClass {

    public static void main(String[] args){
       /* AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(EalyAopMainConfig.class);
        Calculate calculate = ctx.getBean("calculateImpl",Calculate.class);
        calculate.div(1,1);*/

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(EalyAopMainConfig.class);
        Calculate calculateProxy = ctx.getBean("calculateProxy",Calculate.class);
       // System.out.println(calculateProxy);
        calculateProxy.div(1,1);
    }

}
