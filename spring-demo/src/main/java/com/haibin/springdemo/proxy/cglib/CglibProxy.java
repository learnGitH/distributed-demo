package com.haibin.springdemo.proxy.cglib;

import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

public class CglibProxy implements MethodInterceptor {

    Enhancer enhancer = new Enhancer();
    public Object getProxy(Class clazz){
        //设置需要创建的子类
        enhancer.setSuperclass(clazz);
        enhancer.setCallback(this);
        //通过字节码技术动态创建子类实例
        return enhancer.create();
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        System.out.println("——————————————————————————");
        System.out.println("下一位请登台发言！");
        //目标方法调用
        Object result = methodProxy.invokeSuper(o,objects);
        //目标方法后执行
        System.out.println("大家掌声鼓励！");
        return result;
    }

}
