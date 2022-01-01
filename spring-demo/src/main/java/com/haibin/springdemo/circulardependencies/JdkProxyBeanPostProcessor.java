package com.haibin.springdemo.circulardependencies;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor;

public class JdkProxyBeanPostProcessor implements SmartInstantiationAwareBeanPostProcessor {

    @Override
    public Object getEarlyBeanReference(Object bean, String beanName) throws BeansException {
        if (bean instanceof InstanceA){
            JdkDynimcProxy jdkDynimcProxy = new JdkDynimcProxy(bean);
            return jdkDynimcProxy.getProxy();
        }
        return bean;
    }
}
