Spring源码循环依赖终极讲解

1.手写Spring循环依赖的整个过程

2.源码解析：

DefaultSingleBeanRegistry.java

```java
/**
 * 在网上很多很多写源码的大佬或者是<spring源码深度解析>一书上,也没有说清楚为啥要使用三级缓存(二级缓存可不可以能够
 * 解决) 答案是：可以, 但是没有很好的扩展性为啥这么说.......
 * 原因: 获取三级缓存-----getEarlyBeanReference()经过一系列的后置处理来给我们早期对象进行特殊化处理
 * //从三级缓存中获取包装对象的时候 ，他会经过一次后置处理器的处理对我们早期对象的bean进行
 * 特殊化处理，但是spring的原生后置处理器没有经过处理，而是留给了我们程序员进行扩展
 * singletonObject = singletonFactory.getObject();
 * 把三级缓存移植到二级缓存中
 * this.earlySingletonObjects.put(beanName, singletonObject);
 * //删除三级缓存中的之

 * this.singletonFactories.remove(beanName);
 * @param beanName bean的名称
 * @param allowEarlyReference 是否允许暴露早期对象  通过该参数可以控制是否能够解决循环依赖的.
 * @return
 *         这里可能返回一个null（IOC容器加载单实例bean的时候,第一次进来是返回null）
 *         也有可能返回一个单例对象(IOC容器加载了单实例了,第二次来获取当前的Bean)
 *         也可能返回一个早期对象(用于解决循环依赖问题)
 */
@Nullable
protected Object getSingleton(String beanName, boolean allowEarlyReference) {
   /**
    * 第一步:我们尝试去一级缓存(单例缓存池中去获取对象,一般情况从该map中获取的对象是直接可以使用的)
    * IOC容器初始化加载单实例bean的时候第一次进来的时候 该map中一般返回空
    */
   Object singletonObject = this.singletonObjects.get(beanName);
   /**
    * 若在第一级缓存中没有获取到对象,并且singletonsCurrentlyInCreation这个list包含该beanName
    * IOC容器初始化加载单实例bean的时候第一次进来的时候 该list中一般返回空,但是循环依赖的时候可以满足该条件
    */
   if (singletonObject == null && isSingletonCurrentlyInCreation(beanName)) {
      synchronized (this.singletonObjects) {
         /**
          * 尝试去二级缓存中获取对象(二级缓存中的对象是一个早期对象)
          * 何为早期对象:就是bean刚刚调用了构造方法，还来不及给bean的属性进行赋值的对象
          * 就是早期对象
          */
         singletonObject = this.earlySingletonObjects.get(beanName);
         /**
          * 二级缓存中也没有获取到对象,allowEarlyReference为true(参数是有上一个方法传递进来的true)
          */
         if (singletonObject == null && allowEarlyReference) {
            /**
             * 直接从三级缓存中获取 ObjectFactory对象 这个对接就是用来解决循环依赖的关键所在
             * 在ioc后期的过程中,当bean调用了构造方法的时候,把早期对象包裹成一个ObjectFactory
             * 暴露到三级缓存中
             */
            ObjectFactory<?> singletonFactory = this.singletonFactories.get(beanName);
            //从三级缓存中获取到对象不为空
            if (singletonFactory != null) {
               /**
                * 在这里通过暴露的ObjectFactory 包装对象中,通过调用他的getObject()来获取我们的早期对象
                * 在这个环节中会调用到 getEarlyBeanReference()来进行后置处理
                */
               singletonObject = singletonFactory.getObject();
               //把早期对象放置在二级缓存,
               this.earlySingletonObjects.put(beanName, singletonObject);
               //ObjectFactory 包装对象从三级缓存中删除掉
               this.singletonFactories.remove(beanName);
            }
         }
      }
   }
   return singletonObject;
}
```

2.Spring怎么解决循环依赖

3.为什么要二级缓存和三级缓存

4.Spring有没有解决构造函数的循环依赖

5.Spring有没有解决多例下的循环依赖





6.Spring读取不完整Bean的最终解决原理

7.监听器原理学习-Listener

8.内置Bean的后置处理器

9.BeanDefinition的一些玩法