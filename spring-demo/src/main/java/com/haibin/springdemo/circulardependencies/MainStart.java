package com.haibin.springdemo.circulardependencies;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MainStart {

    private static Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(256);

    /**
     * 读取bean定义，当然在spring中肯定是根据配置 动态扫描注册
     */
    public static void loadBeanDefinitions(){
        RootBeanDefinition aBeanDefinition = new RootBeanDefinition(InstanceA.class);
        RootBeanDefinition bBeanDefinition = new RootBeanDefinition(InstanceB.class);
        beanDefinitionMap.put("instanceA",aBeanDefinition);
        beanDefinitionMap.put("instanceB",bBeanDefinition);
    }

    public static void main(String[] args) throws Exception{
        //加载了BeanDefinition
        loadBeanDefinitions();
        //注册Bean的后置处理器

        //循环创建Bean
        for (String key : beanDefinitionMap.keySet()){
            //先创建A
            getBean(key);
        }
        InstanceA instanceA = (InstanceA) getBean("instanceA");
        instanceA.say();
    }

    // 一级缓存
    public static Map<String,Object> singletonObjects = new ConcurrentHashMap<>();

    // 二级缓存：为了将成熟Bean和纯净Bean分离，避免读取到不完整的Bean
    public static Map<String,Object> earlySingletonObjects = new ConcurrentHashMap<>();

    // 三级缓存
    public static Map<String,ObjectFactory> singletonFactories = new ConcurrentHashMap<>();

    //循环依赖标识
    public static Set<String> singletonsCurrennlyInCreation = new HashSet<>();

    //获取Bean
    public static  Object getBean(String beanName) throws Exception{
        Object singleton = getSingleton(beanName);
        if (singleton != null){
            return singleton;
        }

        //正在创建
        if (!singletonsCurrennlyInCreation.contains(beanName)){
            singletonsCurrennlyInCreation.add(beanName);
        }

        //实例化
        RootBeanDefinition beanDefinition = (RootBeanDefinition) beanDefinitionMap.get(beanName);
        Class<?> beanClass = beanDefinition.getBeanClass();
        Object instanceBean = beanClass.newInstance();
        final Object earlyInstanceBean = instanceBean;

        singletonFactories.put(beanName,() -> new JdkProxyBeanPostProcessor().getEarlyBeanReference(earlyInstanceBean,beanName));

        //添加到二级缓存
        //earlySingletonObjects.put(beanName,instanceBean);

        //属性赋值
        Field[] declaredFields = beanClass.getDeclaredFields();
        for (Field declaredField : declaredFields){
            Autowired annotation = declaredField.getAnnotation(Autowired.class);
            //说明属性上面有Autowired
            if (annotation != null){
                declaredField.setAccessible(true);
                String name = declaredField.getName();
                Object fileObject = getBean(name);  //拿到B的bean
                declaredField.set(instanceBean,fileObject);
            }
        }

        // 由于递归调用完A还是原实例，所以要从二级缓存中拿到proxy
        if (earlySingletonObjects.containsKey(beanName)){
            instanceBean = earlySingletonObjects.get(beanName);
        }

        //添加到一级缓存
        singletonObjects.put(beanName,instanceBean);

        // remove 二级缓存和三级缓存
        return instanceBean;
    }

    public static Object getSingleton(String beanName){
        //先从一级缓存拿
        Object bean = singletonObjects.get(beanName);
        //说明是循环依赖
        if (bean == null && singletonsCurrennlyInCreation.contains(beanName)) {
            bean = earlySingletonObjects.get(beanName);
            if (bean == null){
                //从三级缓存中拿
                ObjectFactory factory = singletonFactories.get(beanName);
                if (factory != null){
                    bean = factory.getObject();     //拿到动态代理
                    earlySingletonObjects.put(beanName,bean);
                }
            }
        }
        return bean;
    }

}
