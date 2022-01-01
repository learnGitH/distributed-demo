package com.haibin.springdemo.aop.earlyAopDemo;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class LogInterceptor implements MethodInterceptor {
    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        System.out.println(getClass()+"调用方法前");
        Object ret = methodInvocation.proceed();
        System.out.println(getClass()+"调用方法后");
        return ret;
    }
}
