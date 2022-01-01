package com.haibin.springdemo.proxy.jdk;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class MyInvocationHandler implements InvocationHandler {

    private Object target;

    public MyInvocationHandler(Object target){
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //目标方法执行前
        System.out.println("——————————————————————————");
        System.out.println("下一位请登台发言！");
        //目标方法调用
        Object obj = method.invoke(target,args);
        //目标方法执行后
        System.out.println("大家掌声鼓励！");
        return obj;
    }
}
