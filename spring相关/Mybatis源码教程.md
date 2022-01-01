Mybatis源码教程

一、MyBatis源码体系介绍和配置文件解析源码

1、MyBatis和传统JDBC的优势

2、图说MyBatis的整体体系结构

3、MyBatis配置文件源码解析

二、MyBatis的数据库操作过程源码解析

1、MyBatis插件（Plugin）的执行原理

2、MyBatis缓存（Cache）的执行原理

三、MyBatis整合Spring的全过程源码解析

1、Spring如何利用扩展节点完美整合MyBatis



怎么让Spring去管理Mapper代理？





jdk8之前：

if(user != null){

​	order.setUser(user);

}



jdk8及之后

Optional.ofNullable(user).ifPresent(order::setUser);