package com.haibin.springdemo.entity;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

//@Component
public class Book implements BeanPostProcessor {

    public Book(){
        System.out.println("book的构造方法");
    }

    @PostConstruct
    public void init(){
        System.out.println("book 的PostConstruct标志的方法");
    }

    @PreDestroy
    public void destroy(){
        System.out.println("book 的PreDestory标注的方法");
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        System.out.println("TulingBeanPostProcessor...postProcessBeforeInitialization:"+beanName);
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        System.out.println("TulingBeanPostProcessor...postProcessAfterInitialization:"+beanName);
        return bean;
    }
}
