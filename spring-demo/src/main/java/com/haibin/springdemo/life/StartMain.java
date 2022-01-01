package com.haibin.springdemo.life;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class StartMain {

    public static void main(String[] args){
        AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext(AppConfig.class);
        annotationConfigApplicationContext.destroy();
    }

}
