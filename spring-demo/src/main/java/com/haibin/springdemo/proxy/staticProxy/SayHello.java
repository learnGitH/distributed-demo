package com.haibin.springdemo.proxy.staticProxy;

public class SayHello implements Greeting{
    @Override
    public void doGreet() {
        System.out.println("Greeting by say hello .");
    }
}
