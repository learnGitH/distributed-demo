package com.haibin.springdemo.proxy.cglib;

import com.haibin.springdemo.proxy.jdk.Saying;
import com.haibin.springdemo.proxy.jdk.SayingImpl;
import org.springframework.cglib.core.DebuggingClassWriter;

public class CglibProxyTest {

    public static void main(String[] args){
        //将代理类存到本地磁盘
        System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY, "./");
        CglibProxy proxy = new CglibProxy();
        //通过动态生成子类的方式创建代理类
        Saying target = (Saying)proxy.getProxy(SayingImpl.class);
        target.sayHello("小明");
        target.talking("小丽");
    }

}
