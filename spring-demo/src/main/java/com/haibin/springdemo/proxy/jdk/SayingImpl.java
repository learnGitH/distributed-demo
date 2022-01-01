package com.haibin.springdemo.proxy.jdk;

public class SayingImpl implements Saying{

    @Override
    public void sayHello(String name) {
        System.out.println(name + "：大家好啊！");
    }

    @Override
    public void talking(String name) {
        System.out.println(name + "：大家好啊！");
    }

}
