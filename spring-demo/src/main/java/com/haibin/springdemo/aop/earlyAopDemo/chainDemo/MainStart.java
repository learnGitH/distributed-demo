package com.haibin.springdemo.aop.earlyAopDemo.chainDemo;

import com.haibin.springdemo.aop.CalculateImpl;
import com.haibin.springdemo.aop.earlyAopDemo.LogAdvice;
import com.haibin.springdemo.aop.earlyAopDemo.LogInterceptor;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MainStart {

    public static void main(String[] args) throws Throwable {
        //把一条链上的都初始化
        List<MethodInterceptor> list = new ArrayList<>();
        list.add(new MethodBeforeAdviceInterceptor(new LogAdvice()));
        list.add(new LogInterceptor());

        //递归依次调用
        MyMethodInvocation invocation = new MyMethodInvocation(list);
        invocation.proceed();
    }

    public static class MyMethodInvocation implements MethodInvocation {

        protected List<MethodInterceptor> list;
        protected final CalculateImpl target;

        public MyMethodInvocation(List<MethodInterceptor> list){
            this.list = list;
            this.target = new CalculateImpl();
        }
        int i = 0;

        @Override
        public Method getMethod() {
            try{
                return target.getClass().getMethod("add",int.class,int.class);
            }catch (NoSuchMethodException e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public Object[] getArguments() {
            return new Object[0];
        }

        @Override
        public Object proceed() throws Throwable {
            if (i == list.size()){
                return target.add(2,2);
            }
            MethodInterceptor mi = list.get(i);
            i++;
            return mi.invoke(this);
        }

        @Override
        public Object getThis() {
            return target;
        }

        @Override
        public AccessibleObject getStaticPart() {
            return null;
        }
    }

}
