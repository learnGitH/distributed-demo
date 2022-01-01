package com.haibin.springdemo.event;

import org.springframework.context.ApplicationEvent;

/**
 * 订单事件
 * @author shb
 * @date 2021-11-01
 */
public class OrderEvent extends ApplicationEvent {

    private String name;

    public OrderEvent(Object source,String name) {
        super(source);
        this.name = name;
    }

    public String getName(){
        return name;
    }
}
