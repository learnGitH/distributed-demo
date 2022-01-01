SpringBoot自动装配核心源码剖析

SpringBoot核心两大部分：自动装配原理和启动原理

1.从Spring的IOC到SpringBoot的自动配置原理

beanDefinition的注册顺序为：

（1）先@ComponentScan的：@Component、@Service、@Controller

（2）@Import的：@Component、@Service、@Controller

（3）@Configuration-->和该配置类里面的@Bean-->该配置类里面的@Import进来的实现了ImportBeanDefinitionRegistrar接口的

（4）@Import进来的@Configuration-->和该配置类里面的@Bean-->该配置类里面的@Import进来的实现了ImportBeanDefinitionRegistrar接口

（5）@Import进来的实现了DeffredImportSelector接口的@Configuration-->和该配置类里面的@Bean-->该配置类里面的@Import进来的实现了ImportBeanDefinitionRegistrar接口

2.DefferredImportSelector对Bean加载顺序的影响

3.SpringBoot自动配置源码深入分析

4.如何在自动配置类上进行定制扩展

5.实现自定义Starter完成自动配置



