package com.haibin.springdemo.entity;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

public class Person implements InitializingBean, DisposableBean {

    private Integer id;
    private String name;

    public Person(){
        System.out.println("Person的构造方法");
    }

    @Override
    public void destroy() throws Exception {
        System.out.println("DisposableBean的destroy()方法 ");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("InitializingBean的 afterPropertiesSet方法");
    }
}
