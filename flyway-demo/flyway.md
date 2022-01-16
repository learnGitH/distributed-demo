数据库版本管理主要有Liquibase和Flyway，类似于git,以下介绍Flyway的使用

一、介绍

Flyway是一个比较简单，并且基于版本管理的数据库管理工具。

二、使用（基于springboot）

1、引入pom.xml文件

```
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
    <version>5.2.4</version>
</dependency>
```

二、配置yml文件

```
# Production Environment properties
# WARNING: NEVER EVER store credentials in the code base. This is a demo only!
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT&useSSL=false
    username: sa
    password:
    driver-class-name: com.mysql.cj.jdbc.Driver
#  flyway:
#    locations: classpath:/db/migration/schema, classpath:/db/migration/data

server:
  port: 8080
```

三、创建sql文件

在resources目录下创建db/migration目录，并在该目录下创建对于的sql文件，sql文件的命名格式为：V1.1_descript.sql

四、主启动类

```java
@SpringBootApplication
public class FlywayApplication {

    public static void main(String[] args){
        SpringApplication.run(FlywayApplication.class,args);
    }

}
```

五、flyway产生文件

启动后会在数据库产生一张表flyway_schema_history，并且里面的数据是根据sql文件来进行管理的

[![7pqUdU.md.png](https://s4.ax1x.com/2022/01/07/7pqUdU.md.png)](https://imgtu.com/i/7pqUdU)

六、注意点

1、当sql文件里面的sql执行失败后，也会在flyway_schema_history记录一条该文件版本的记录，当修改再次执行就会报错，此时可以修改文件版本，或者去数据库把对应的记录删除掉。