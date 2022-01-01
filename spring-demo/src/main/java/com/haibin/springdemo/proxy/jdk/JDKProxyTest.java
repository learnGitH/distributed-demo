package com.haibin.springdemo.proxy.jdk;

import java.lang.reflect.Proxy;

public class JDKProxyTest {

    public static void main(String[] args){
        System.setProperty("sun.misc.ProxyGenerator.saveGeneratedFiles","true");
        //希望被代理的目标业务类
        Saying target = new SayingImpl();
        //将目标类贺横切类编织在一起
        MyInvocationHandler handler = new MyInvocationHandler(target);
        //创建代理实例
        Saying proxy = (Saying) Proxy.newProxyInstance(
                target.getClass().getClassLoader(), //目标类的类加载器
                target.getClass().getInterfaces(),  //目标类的接口
                handler);                           //横切类
        proxy.sayHello("小明");
        proxy.talking("小丽");
    }

}


