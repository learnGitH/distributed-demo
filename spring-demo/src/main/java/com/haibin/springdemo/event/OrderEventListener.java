package com.haibin.springdemo.event;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener implements ApplicationListener<OrderEvent> {

    @Override
    public void onApplicationEvent(OrderEvent orderEvent) {
        if(orderEvent.getName().equals("减库存")){
            System.out.println("减库存。。。。。。。。。。");
        }
    }
}
