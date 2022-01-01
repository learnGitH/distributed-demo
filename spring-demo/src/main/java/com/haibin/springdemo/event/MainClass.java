package com.haibin.springdemo.event;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class MainClass {

    public static void main(String[] args){
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(MainConfig.class);

        //下单
        Order order = new Order();
        order.setId(1);
        System.out.println("下单");
        ctx.publishEvent(new OrderEvent(order,"减库存"));
        System.out.println("日志。。。。。。。。。。");
    }

}
