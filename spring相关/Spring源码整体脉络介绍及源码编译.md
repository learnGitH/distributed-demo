Spring源码整体脉络介绍及源码编译

1、Spring源码学习介绍

**Spring体系架构（基于4.x）**

![https://note.youdao.com/yws/public/resource/0fe06ed560dc8cb89e6f7baf541cea97/xmlnote/B40AC82873EA40C0882810B06385C60B/5436](https://note.youdao.com/yws/public/resource/0fe06ed560dc8cb89e6f7baf541cea97/xmlnote/B40AC82873EA40C0882810B06385C60B/5436)

**1）、Core Container（核心容器）**

该模块主要包含Core、Beans、Context和SpEL模块。其中Core和Beans是整个框架最基础的部分，提供IOC和依赖注入特性。这里最重要的概念就是BeanFactory，提供了以Factory模式的实现来消除对程序性

单例模式。

- Core：模块主要包含Spring框架最基本的核心工具类，Core是其他组件的基础核心。
- Beans：模块主要包含访问配置文件、创建/管理Bean以及IOC/DI相关的类。
- Context：继承了Beans的特性，主要为Spring提供大量的扩展，如国际化、事件机制、资源加载等待。ApplicationContext接口是Context模块的关键。
- SpEL：模块提供了一个强大的语言表达式。

**2）、AOP and Instrumentation**

提供符合AOP Alliance标准的面向切面编程的实现，可以让你定义如方法拦截器和切点，从而降低程序之间的耦合性。

- AspectJ模块：提供了与AspectJ的集成 。
- Instrumentation模块：提供用于某些应用程序服务器的类工具支持和类加载器实现 。

**3）、Messaging**

该模块具有来自Spring Integration项目的关键抽象，如Message，MessageChannel，MessageHandler等。它们构成基于消息的应用程序的基础。该模块还包括一组注释，用于将消息映射到方法，类似于基于Spring MVC注释的编程模型。

**4）、Data Access/Integration**

数据访问/集成层由JDBC，ORM，OXM，JMS和事务模块组成。

- JDBC模块：提供了JDBC抽象层，从而无需进行繁琐的JDBC编码和数据库特定错误代码（不同数据代码可能不同）的解析。
- 事务模块：支持对实现特殊接口的类以及所有POJO（普通Java对象）进行编程和声明式事务管理。
- ORM模块：该模块为当前流行的ORM（包括JPA，JDO和Hibernate）提供了集成层。使用ORM模块，可以将所有这些O/R映射框架与Spring提供的所有功能结合使用，如前面提到的事务管理功能。
- OXM模块：提供了一个抽象层，该抽象层支持Object/ XML映射实现，例如JAXB，Castor，XMLBeans，JiBX和XStream。
- JMS模块（Java Messaging Service）：包含用于生成和使用消息的功能。从Spring Framework 4.1开始，提供了与Spring-Messaging模块集成。

**5）、Web**

Web上下文模块建立在应用程序上下文模块之上，为基于Web的应用程序提供上下文支持。该模块包含Web、WebMVC、Web Socket和Web-Porlet模块。

- Web模块：提供了基本的面向Web的集成功能，如文件上传功能以及使用Servlet监听器和面向Web的应用程序上下文对IoC容器的初始化。
- WebMVC模块（也称为Web-Servlet模块）：包含基于Spring的Model-View-Controller（MVC）支持和针对Web应用程序的Rest Web服务实现。
- Web-Portlet 模块（也称为Web-Portlet模块）：提供Portlet环境中的MVC实现。

**6）、Test**

该模块支持使用JUnit或TestNG对Spring组件进行单元测试和集成测试。

2、Spring源码的整体脉络梳理

[![5AXAeK.md.png](https://z3.ax1x.com/2021/10/10/5AXAeK.md.png)](https://imgtu.com/i/5AXAeK)

3、什么是BeanFactory

BeanFactory是Bean的工厂，spring顶层核心接口，没有BeanFactory就没有Bean的存在，负责生产BeanDefinition原材料的bean。

BeanFactory和FactoryBean的区别：虽然名字很像容易混淆，但是完全2个东西。BeanFactory是Bean的工厂，spring的顶层核心接口，没有BeanFactory就没有Bean的存在，重要程度可想而知。

FactoryBean也是一个接口，被他修饰的Bean将成为一个特殊的Bean,原本的Bean将被隐藏，而是由FactoryBean的getObject返回最终Bean。你就把FactoryBean当作改装车行，它可以改装你原本的Bean。

4、BeanFactory和ApplicationContext的区别

一句话，BeanFactory是增对你给的原材料进行生产，ApplicationContext提供人性化的服务后，再去调用工厂进行生产。

BeanFactory是Bean的工厂，spring的顶层核心接口，没有BeanFactory就没有Bean的存在，工厂只负责按照要求生产Bean,Bean的定义信息，要生产成什么样由下家(ApplicationContext)说了算。

ApplicationContext面向的是用户，所以需要更好的服务用户，不仅要提供Bean和调用工厂去生产Bean还要提供一系列人性化的服务（如国际化、加载Bean定义、监听器等等。。),怎么生成Bean的事就交给工厂去做。

但是ApplicationContext也依赖工厂，没有工厂也就没有办法提供Bean,没有办法更好的服务用户，所以它需要继承工厂

5、图述SpringIoC的加载过程

6、图述Bean的生命周期

[![5AXAeK.md.png](https://z3.ax1x.com/2021/10/10/5AXAeK.md.png)](https://imgtu.com/i/5AXAeK)

7、图述Spring中的扩展接口



8、Spring源码编译过程
