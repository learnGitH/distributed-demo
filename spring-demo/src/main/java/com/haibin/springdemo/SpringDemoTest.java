package com.haibin.springdemo;

import com.haibin.springdemo.config.AppConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class SpringDemoTest {

    public static void main(String[] args){
       // ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("beans.xml");
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class);
        //System.out.println(ctx.getBean("car"));
        //System.out.println(ctx.getBean("person"));
        //System.out.println(ctx.getBean("book"));
        ctx.destroy();
    }

}
