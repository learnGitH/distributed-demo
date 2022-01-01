package com.haibin.springdemo.proxy.staticProxy;

public class ShakeHands implements Greeting{
    @Override
    public void doGreet() {
        System.out.println("Greeting by shake other`s hands .");
    }
}
