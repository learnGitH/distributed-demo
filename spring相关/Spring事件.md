Spring事件

一、使用Spring事件

spring事件体系主要包括：事件，事件监听器，事件广播器。

1、事件

spring内置事件：

在spring中默认提供了一些内置的事件，由系统内部进行发布，只需要注入监听器

![img](https://note.youdao.com/yws/public/resource/42d15ea5ab2072b4a354441ce9080eb9/xmlnote/39FCB85E7FCE4C3FB0E542D4A8751C12/7078)

| **Event**             | **说明**                                                     |
| --------------------- | ------------------------------------------------------------ |
| ContextRefreshedEvent | 当容器被实例化或refreshed时发布.如调用refresh()方法, 此处的实例化是指所有的bean都已被加载,后置处理器都被激活,所有单例bean都已被实例化, 所有的容器对象都已准备好可使用. 如果容器支持热重载,则refresh可以被触发多次(XmlWebApplicatonContext支持热刷新,而GenericApplicationContext则不支持) |
| ContextStartedEvent   | 当容器启动时发布,即调用start()方法, 已启用意味着所有的Lifecycle bean都已显式接收到了start信号 |
| ContextStoppedEvent   | 当容器停止时发布,即调用stop()方法, 即所有的Lifecycle bean都已显式接收到了stop信号 , 关闭的容器可以通过start()方法重启 |
| ContextClosedEvent    | 当容器关闭时发布,即调用close方法, 关闭意味着所有的单例bean都已被销毁.关闭的容器不能被重启或refresh |
| RequestHandledEvent   | 这只在使用spring的DispatcherServlet时有效,当一个请求被处理完成时发布 |

自定义事件：

事件类需要继承ApplicationEvent：

```java
/**
 * 订单事件
 * @author shb
 * @date 2021-11-01
 */
public class OrderEvent extends ApplicationEvent {

    private String name;

    public OrderEvent(Object source,String name) {
        super(source);
        this.name = name;
    }

    public String getName(){
        return name;
    }
}
```

事件监听器-基于接口：

```java
@Component
public class OrderEventListener implements ApplicationListener<OrderEvent> {

    @Override
    public void onApplicationEvent(OrderEvent orderEvent) {
        if(orderEvent.getName().equals("减库存")){
            System.out.println("减库存。。。。。。。。。。");
        }
    }
}
```

事件监听器-基于注解：

```java
@Component
public class OrderEventListener {
	// 基于注解的
	@EventListener(OrderEvent.class)
	public void onApplicationEvent(OrderEvent event) {
   	 	if(event.getName().equals("减库存")){
        	System.out.println("减库存.......");
    	}
	}
}
```

事件发布：

```java
public class MainClass {

    public static void main(String[] args){
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(MainConfig.class);

        //下单
        Order order = new Order();
        order.setId(1);
        System.out.println("下单");
        ctx.publishEvent(new OrderEvent(order,"减库存"));
        System.out.println("日志。。。。。。。。。。");
    }

}
```

二、Spring事件原理

原理：观察者模式

Spring的事件监听有三个部分组成：

事件（ApplicationEvent）负责对应相应监听器事件源发生某事件是特定事件监听器被触发的原因。

监听器（ApplicationListener）对应于观察者模式中的观察者。监听器监听特定事件，并在内部定义了事件发生后的相应逻辑。

事件发布器（ApplicationEventMulticaster）对应于观察者模式中的被观察者/主题，负责通知观察者对外提供发布事件和增删事件监听器的接口，维护事件和事件监听器之间的映射关系，并在事件发生时负责通知相关监听器。

<img src="https://note.youdao.com/yws/public/resource/42d15ea5ab2072b4a354441ce9080eb9/xmlnote/854BE13AA97E4F789FC4AFE5869E6610/7150" alt="https://note.youdao.com/yws/public/resource/42d15ea5ab2072b4a354441ce9080eb9/xmlnote/854BE13AA97E4F789FC4AFE5869E6610/7150" style="zoom:80%;" />

Spring事件机制是观察者模式的一种实现，但是除了发布者和监听者两个角色之外，还有一个EventMultiCaster的角色负责把事件转发给监听者，工作流程如下：

<img src="https://note.youdao.com/yws/public/resource/42d15ea5ab2072b4a354441ce9080eb9/xmlnote/D6A3C89D36414981BC1B5C9B70FBC026/5791" alt="https://note.youdao.com/yws/public/resource/42d15ea5ab2072b4a354441ce9080eb9/xmlnote/D6A3C89D36414981BC1B5C9B70FBC026/5791" style="zoom:80%;" />q'wa

也就是说上面代码中发布者调用applicationEventPublisher.publishEvent(msg); 是会将事件发送给了EventMultiCaster， 而后由EventMultiCaster注册着所有的Listener，然后根据事件类型决定转发给那个Listener。