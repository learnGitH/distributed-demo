package com.haibin.springdemo.aop.earlyAopDemo;

import com.haibin.springdemo.aop.Calculate;
import com.haibin.springdemo.aop.CalculateImpl;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.support.NameMatchMethodPointcutAdvisor;
import org.springframework.context.annotation.Bean;

public class EalyAopMainConfig {

    // 被代理对象
    @Bean
    public Calculate calculateImpl() {
        return new CalculateImpl();
    }

    // Advice 方式
    @Bean
    public LogAdvice logAdvice(){
        return new LogAdvice();
    }

    // Interceptor方式 ， 可以理解为环绕通知
    @Bean
    public LogInterceptor logInterceptor() {
        return new LogInterceptor();
    }

    @Bean
    public NameMatchMethodPointcutAdvisor logAspect(){
        NameMatchMethodPointcutAdvisor advisor = new NameMatchMethodPointcutAdvisor();
        //通知（Advice）:是我们的通知类
        //通知者（Advisor）:是经过包装后的细粒度控制方式。
        advisor.setAdvice(logAdvice());
        advisor.setMappedNames("div");
        return advisor;
    }

    @Bean
    public ProxyFactoryBean calculateProxy(){
        ProxyFactoryBean userService=new ProxyFactoryBean();
        //userService.setInterceptorNames("logAdvice","logInterceptor");  // 根据指定的顺序执行
        userService.setInterceptorNames("logAspect");
        userService.setTarget(calculateImpl());
        return userService;
    }

}
