package com.haibin.springdemo.proxy.staticProxy;

public class Main {

    public static void main(String[] args){
        Greeting hello = new SayHello();
        Greeting shakeHands = new ShakeHands();

        //静态代理
        GreetStaticProxy staticHelloProxy = new GreetStaticProxy(hello);
        staticHelloProxy.doGreet();
        System.out.println();
        GreetStaticProxy shakeHandsProxy = new GreetStaticProxy(shakeHands);
        shakeHandsProxy.doGreet();
    }

}

