SpringBoot启动原理核心源码剖析

SpringBoot核心两大部分：自动装配原理和启动原理

1、为什么SpringBoot的jar可以直接运行？

插件：

```pom.xml
<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
        </plugin>
    </plugins>
</build>
```

当我们使用以上springboot的插件打包成jar文件的时候，它就会把我们依赖的jar文件并且帮我们生成一个MANIFEST.MF文件，然后当我们执行java -jar xxx 的时候就会执行MANIFEST.MF文件里的JarLauncher，JarLauncher里面通过加载BOOT-INF/classes目录及BOOT-INF/lib目录下jar文件，实现fat jar的启动。SpringBoot通过扩展JarFile、JarURLConnection及URLStreamHandler,实现了jar in jar中资源的加载。SpringBoot通过扩展URLClassLoader-LauncherURLClassLoader,实现了jar in jar中class文件的加载。

2、SpringBoot是如何启动Spring容器源码详解

3、SpringBoot是如何启动内置Tomcat源码详解

4、外置Tomcat是如何启动SpringBoot源码详解

5、到底什么是SPI机制