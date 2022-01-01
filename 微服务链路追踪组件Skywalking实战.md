微服务链路追踪组件Skywalking实战

# 一、概述

## 1.skywalking是什么

对于一个大型的几十个、几百个微服务构成的微服务架构系统，通常会遇到下面一些问题，比如：

1. 如何串联整个调用链路，快速定位问题？
2. 如何理清各个微服务之间的依赖关系？
3. 如何进行各个微服务接口的性能分折？
4. 如何跟踪整个业务流程的调用处理顺序？

skywalking是一个国产开源框架，2015年由吴晟开源 ， 2017年加入Apache孵化器。skywalking是分布式系统的应用程序性能监视工具，专为微服务、云原生架构和基于容器（Docker、K8s、Mesos）架构而设计。SkyWalking 是观察性分析平台和应用性能管理系统，提供分布式追踪、服务网格遥测分析、度量聚合和可视化一体化解决方案。

## 2.文档

官网：http://skywalking.apache.org/

下载：http://skywalking.apache.org/downloads/

Github：https://github.com/apache/skywalking

文档： [https://skywalking.apache.org/docs/main/v8.4.0/readme/](https://skywalking.apache.org/docs/main/v8.3.0/readme/)

中文文档： https://skyapm.github.io/document-cn-translation-of-skywalking/

## 3.调用链选型

1. Zipkin是Twitter开源的调用链分析工具，目前基于springcloud sleuth得到了广泛的使用，特点是轻量，使用部署简单。
2. Pinpoint是韩国人开源的基于字节码注入的调用链分析，以及应用监控分析工具。特点是支持多种插件，UI功能强大，接入端无代码侵入。
3. SkyWalking是本土开源的基于字节码注入的调用链分析，以及应用监控分析工具。特点是支持多种插件，UI功能较强，接入端无代码侵入。目前已加入Apache孵化器。
4. CAT是大众点评开源的基于编码和配置的调用链分析，应用监控分析，日志采集，监控报警等一系列的监控平台工具。

<img src="https://note.youdao.com/yws/public/resource/07ea8709108735fd281b4ab34e7710ef/xmlnote/2BE1C8E0CAA141DCB4281B4EA0E9C706/14414" alt="img" style="zoom:80%;" />

## 4.探针性能对比

模拟了三种并发用户：500，750，1000。使用jmeter测试，每个线程发送30个请求，设置思考时间为10ms。使用的采样率为1，即100%，这边与生产可能有差别。pinpoint默认的采样率为20，即50%，通过设置agent的配置文件改为100%。zipkin默认也是1。组合起来，一共有12种。下面看下汇总表：

![https://note.youdao.com/yws/public/resource/07ea8709108735fd281b4ab34e7710ef/xmlnote/2836C9780D6F43AB9FFE7CB6F4C6E8F9/14419](https://note.youdao.com/yws/public/resource/07ea8709108735fd281b4ab34e7710ef/xmlnote/2836C9780D6F43AB9FFE7CB6F4C6E8F9/14419)

从上表可以看出，在三种链路监控组件中，**skywalking的探针对吞吐量的影响最小，zipkin的吞吐量居中。pinpoint的探针对吞吐量的影响较为明显**，在500并发用户时，测试服务的吞吐量从1385降低到774，影响很大。然后再看下CPU和memory的影响，在内部服务器进行的压测，对CPU和memory的影响都差不多在10%之内。

## 5.skywalking主要功能特性

1)、多种监控手段，可以通过语言探针和service mesh获得监控的数据；

2)、支持多种语言自动探针，包括 Java，.NET Core 和 Node.JS；

3)、轻量高效，无需大数据平台和大量的服务器资源；

4)、模块化，UI、存储、集群管理都有多种机制可选；

5)、支持告警；

6)、优秀的可视化解决方案；

# 二、Skywalking整体架构剖析

![https://note.youdao.com/yws/public/resource/07ea8709108735fd281b4ab34e7710ef/xmlnote/75D58423F9CC4278A25FE64130608843/14409](https://note.youdao.com/yws/public/resource/07ea8709108735fd281b4ab34e7710ef/xmlnote/75D58423F9CC4278A25FE64130608843/14409)

## 1.整个架构分成四部分

1)、上部分Agent ：负责从应用中，收集链路信息，发送给 SkyWalking OAP 服务器；

2)、下部分 SkyWalking OAP ：负责接收Agent发送的Tracing数据信息，然后进行分析(Analysis Core)，存储到外部存储器(Storage)，最终提供查询(Query)功能；

3)、右部分Storage：Tracing数据存储，目前支持ES、MySQL、Sharding Sphere、TiDB、H2多种存储器，目前采用较多的是ES，主要考虑是SkyWalking开发团队自己的生产环境采用ES为主；

4)、左部分SkyWalking UI：负责提供控制台，查看链路等等；

## 2.skywalking支持三种探针

1)、Agent – 基于ByteBuddy字节码增强技术实现，通过jvm的agent参数加载，并在程序启动时拦截指定的方法来收集数据。

2)、SDK – 程序中显式调用SkyWalking提供的SDK来收集数据，对应用有侵入。

3)、Service Mesh – 通过Service mesh的网络代理来收集数据。

## 3.后端（Backend）

接受探针发送过来的数据，进行度量分析，调用链分析和存储。后端主要分为两部分：

●	OAP（Observability Analysis Platform）- 进行度量分析和调用链分析的后端平台，并支持将数据存储到各种数据库中，如：ElasticSearch，MySQL，InfluxDB等。

●	OAL（Observability Analysis Language）- 用来进行度量分析的DSL，类似于SQL，用于查询度量分析结果和警报。

## 4.界面（UI）

●	RocketBot UI – SkyWalking 7.0.0 的默认web UI

●	CLI – 命令行界面

这三个模块的交互流程：

![https://note.youdao.com/yws/public/resource/07ea8709108735fd281b4ab34e7710ef/xmlnote/DD9F7B33011148B3889E13483D537A5D/14912](https://note.youdao.com/yws/public/resource/07ea8709108735fd281b4ab34e7710ef/xmlnote/DD9F7B33011148B3889E13483D537A5D/14912)

# 三、Skywalking OAP&UI服务搭建

## 1.下载

按照前面的下载地址进行下载，然后进行解压，这里下载8.4版本

[![4vSo2F.md.png](https://z3.ax1x.com/2021/10/05/4vSo2F.md.png)](https://imgtu.com/i/4vSo2F)

## 2.目录结构介绍

![https://note.youdao.com/yws/public/resource/07ea8709108735fd281b4ab34e7710ef/xmlnote/E90D6F88080E43FA87D1866101EE6C52/14411](https://note.youdao.com/yws/public/resource/07ea8709108735fd281b4ab34e7710ef/xmlnote/E90D6F88080E43FA87D1866101EE6C52/14411)

## 3.启动

这里先采用默认的启动的方式，不修改配置文件。在config/application.yml可以进行修改，这里不修改默认采用H2数据库存储

![https://note.youdao.com/yws/public/resource/07ea8709108735fd281b4ab34e7710ef/xmlnote/E8C7AC83A12B436EBDBABFD50AD7DC25/18781](https://note.youdao.com/yws/public/resource/07ea8709108735fd281b4ab34e7710ef/xmlnote/E8C7AC83A12B436EBDBABFD50AD7DC25/18781)

启动脚本bin/startup.sh

[![4vpBZR.md.png](https://z3.ax1x.com/2021/10/05/4vpBZR.md.png)](https://imgtu.com/i/4vpBZR)

日志信息存储在logs目录

![https://note.youdao.com/yws/public/resource/07ea8709108735fd281b4ab34e7710ef/xmlnote/C222FA1A6D604517A8894C67D6EB8EA9/14423](https://note.youdao.com/yws/public/resource/07ea8709108735fd281b4ab34e7710ef/xmlnote/C222FA1A6D604517A8894C67D6EB8EA9/14423)

启动成功后会启动两个服务，一个是skywalking-oap-server，一个是skywalking-web-ui

skywalking-oap-server服务启动后会暴露11800 和 12800 两个端口，分别为收集监控数据的端口11800和接受前端请求的端口12800，修改端口可以修改config/applicaiton.yml

skywalking-web-ui服务会占用 8080 端口， 修改端口可以修改webapp/webapp.yml

![https://note.youdao.com/yws/public/resource/07ea8709108735fd281b4ab34e7710ef/xmlnote/91491BA332394731A7C29A3B099B1AEF/14671](https://note.youdao.com/yws/public/resource/07ea8709108735fd281b4ab34e7710ef/xmlnote/91491BA332394731A7C29A3B099B1AEF/14671)

server.port：SkyWalking UI服务端口，默认是8080；

collector.ribbon.listOfServers：SkyWalking OAP服务地址数组，SkyWalking UI界面的数据是通过请求SkyWalking OAP服务来获得；

4.访问

http://192.168.159.129:8080/

[![4vpxwn.md.png](https://z3.ax1x.com/2021/10/05/4vpxwn.md.png)](https://imgtu.com/i/4vpxwn)

**服务(Service) ：**表示对请求提供相同行为的一系列或一组工作负载，在使用Agent时，可以定义服务的名字；

**服务实例(Service Instance) ：**上述的一组工作负载中的每一个工作负载称为一个实例， 一个服务实例实际就是操作系统上的一个真实进程；

**端点(Endpoint) ：**对于特定服务所接收的请求路径, 如HTTP的URI路径和gRPC服务的类名 + 方法签名；

# 四、Skywalking Agent接入微服务实现链路跟踪告警通知配置

## 1、接入微服务

在需要监控的服务上在启动参数添加以下就可以实现监控：

```java
-javaagent:D:\software\installPackage\skywalking\apache-skywalking-apm-bin-es7\agent\skywalking-agent.jar -DSW_AGENT_NAME=springboot-skywalking-demo-micro -DSW_AGENT_COLLECTOR_BACKEND_SERVICES=192.168.159.129:11800 
```

-javaagent配置的是探针agent的路径

-DSW_AGENT_NAME配置名称，这个名称会显示在界面服务对应的名称

-DSW_AGENT_COLLECTOR_BACKEND_SERVICES配置的是SkyWalking OAP后端收集数据的服务地址

[![4v9vcD.md.png](https://z3.ax1x.com/2021/10/05/4v9vcD.md.png)](https://imgtu.com/i/4v9vcD)

这里需要注意的是，如果有网关的话，需要做以下操作，要不然不会出现gateway的调用

拷贝agent/optional-plugins目录下的gateway插件到agent/plugins目录

![https://note.youdao.com/yws/public/resource/07ea8709108735fd281b4ab34e7710ef/xmlnote/729AF6C13037446BBB94ABA94481B849/14574](https://note.youdao.com/yws/public/resource/07ea8709108735fd281b4ab34e7710ef/xmlnote/729AF6C13037446BBB94ABA94481B849/14574)

## 2.Skywalking告警通知

skywalking告警的核心由一组规则驱动，这些规则定义在config/alarm-settings.yml文件中，告警规则的定义分为三部分:

1)、告警规则：它们定义了应该如何触发度量警报，应该考虑什么条件；

2)、网络钩子(Webhook}：当警告触发时，哪些服务终端需要被通知；

3)、gRPC钩子：远程gRPC方法的主机和端口，告警触发后调用；

为了方便，skywalking发行版中提供了默认的alarm-setting.yml文件，包括一些规则，每个规则有英文注释，可以根据注释得知每个规则的作用：

- 在最近10分钟的3分钟内服务平均响应时间超过1000ms
- 最近10分钟内，服务成功率在2分钟内低于80%
- 服务实例的响应时间在过去10分钟的2分钟内超过1000ms
- 数据库访问{name}的响应时间在过去10分钟的2分钟内超过1000ms

只要我们的服务请求符合alarm-setting.yml文件中的某一条规则就会触发告警。

比如service_resp_time_rule规则：

![https://note.youdao.com/yws/public/resource/07ea8709108735fd281b4ab34e7710ef/xmlnote/BCB84696B9E644C2B5DBC36DF7AF0A2B/14421](https://note.youdao.com/yws/public/resource/07ea8709108735fd281b4ab34e7710ef/xmlnote/BCB84696B9E644C2B5DBC36DF7AF0A2B/14421)

测试：

```java
//模拟延时
@RequestMapping("/info/{id}")
public String info(@PathVariable("id") Integer id){
    log.info("根据userId:"+id+"查询订单信息");
    try {
        Thread.sleep(2000);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
    return "userSuccess";
}

//回调接口
@RequestMapping("/notify")
public String notify(@RequestBody Object obj){
    //TODO 告警信息，给技术负责人发短信，钉钉消息，邮件，微信通知等
    System.err.println(obj.toString());
    return "notify successfully";
}
```

在config/alarm-settings.yml中配置回调接口，并重启skywalking服务

[![4vk8o9.md.png](https://z3.ax1x.com/2021/10/05/4vk8o9.md.png)](https://imgtu.com/i/4vk8o9)

测试访问：http://127.0.0.1:8888/user/info/2，满足告警规则后，控制台输出告警信息

[![4vA674.md.png](https://z3.ax1x.com/2021/10/05/4vA674.md.png)](https://imgtu.com/i/4vA674)

SkyWalking UI显示告警信息:
[![4vAh1x.md.png](https://z3.ax1x.com/2021/10/05/4vAh1x.md.png)](https://imgtu.com/i/4vAh1x)

参考： https://github.com/apache/skywalking/blob/master/docs/en/setup/backend/backend-alarm.md

# 五、基于mysql/elasticsearch跟踪数据持久化

## 1.基于mysql持久化

1）修改config目录下的application.yml，使用mysql作为持久化存储的仓库

![https://note.youdao.com/yws/public/resource/07ea8709108735fd281b4ab34e7710ef/xmlnote/CCCECD2C12A14A23AA9B452030B9027A/14440](https://note.youdao.com/yws/public/resource/07ea8709108735fd281b4ab34e7710ef/xmlnote/CCCECD2C12A14A23AA9B452030B9027A/14440)

2）修改mysql连接配置

![https://note.youdao.com/yws/public/resource/07ea8709108735fd281b4ab34e7710ef/xmlnote/C102DE48A98C498D8240DA27B5AC6114/14448](https://note.youdao.com/yws/public/resource/07ea8709108735fd281b4ab34e7710ef/xmlnote/C102DE48A98C498D8240DA27B5AC6114/14448)

```
storage:
#选择使用mysql   默认使用h2，不会持久化，重启skyWalking之前的数据会丢失
selector: ${SW_STORAGE:mysql}
#使用mysql作为持久化存储的仓库
mysql:
properties:
#数据库连接地址
jdbcUrl: ${SW_JDBC_URL:"jdbc:mysql://1ocalhost:3306/swtest"}
#用户名
dataSource.user: ${SW_DATA_SOURCE_USER:root}
#密码
dataSource.password: ${SW_DATA_SOURCE_PASSWORD:root}
```

注意：需要添加mysql数据驱动包，因为在lib目录下是没有mysql数据驱动包的，所以修改完配置启动是会报错，启动失败的。

![https://note.youdao.com/yws/public/resource/07ea8709108735fd281b4ab34e7710ef/xmlnote/C70F2C37020C4418B43DA16A3A2D739E/14473](https://note.youdao.com/yws/public/resource/07ea8709108735fd281b4ab34e7710ef/xmlnote/C70F2C37020C4418B43DA16A3A2D739E/14473)

3）添加mysql数据驱动包到oap-libs目录下

![https://note.youdao.com/yws/public/resource/07ea8709108735fd281b4ab34e7710ef/xmlnote/BEA866BA3548403AB967EB4B4A5A1E9F/14482](https://note.youdao.com/yws/public/resource/07ea8709108735fd281b4ab34e7710ef/xmlnote/BEA866BA3548403AB967EB4B4A5A1E9F/14482)

4）启动Skywalking

## 2.基于elasticsearch持久化

1）准备好elasticsearch环境

启动elasticsearch服务

```
su - es
cd /usr/local/soft/elasticsearch-7.6.1/
bin/elasticsearch -d
```

2）修改config/application.yml配置文件：

![https://note.youdao.com/yws/public/resource/07ea8709108735fd281b4ab34e7710ef/xmlnote/B7FCB2FF6A274B56B760EC1C7C1559BB/14526](https://note.youdao.com/yws/public/resource/07ea8709108735fd281b4ab34e7710ef/xmlnote/B7FCB2FF6A274B56B760EC1C7C1559BB/14526)

修改elasticsearch7的连接配置

![https://note.youdao.com/yws/public/resource/07ea8709108735fd281b4ab34e7710ef/xmlnote/4619535B1BC94FA1AE6FBD45202A9EC9/14552](https://note.youdao.com/yws/public/resource/07ea8709108735fd281b4ab34e7710ef/xmlnote/4619535B1BC94FA1AE6FBD45202A9EC9/14552)

3）启动Skywalking服务

启动时会向elasticsearch中创建大量的index索引用于持久化数据，每天会产生一个新的索引文件。

启动应用程序，查看跟踪数据是否已经持久化到elasticsearch的索引中，然后重启skywalking，验证跟踪数据会不会丢失

# 六、@Trace自定义链路追踪

如果我们希望对项目中的业务方法，实现链路追踪，方便我们排查问题，可以使用如下的代码

引入依赖

```xml
<!-- SkyWalking 工具类 -->
<dependency>
  <groupId>org.apache.skywalking</groupId>
  <artifactId>apm-toolkit-trace</artifactId>
  <version>8.4.0</version>
</dependency>
```

在业务方法中可以TraceContext获取到traceId:

```java
@RequestMapping(value = "/findOrderByUserId/{id}")
public String  findOrderByUserId(@PathVariable("id") Integer id) {
    log.info("根据userId:"+id+"查询订单信息");
    //TraceContext可以绑定key-value
    TraceContext.putCorrelation("name", "haibin");
    Optional<String> op = TraceContext.getCorrelation("name");
    log.info("name = {} ", op.get());
    //获取跟踪的traceId
    String traceId = TraceContext.traceId();
    log.info("traceId = {} ", traceId);
    return "userSuccess";
}
```

测试：

http://127.0.0.1:8888/user/findOrderByUserId/2

[![4vEIGn.md.png](https://z3.ax1x.com/2021/10/05/4vEIGn.md.png)](https://imgtu.com/i/4vEIGn)

如果一个业务方法想在ui界面的跟踪链路上显示出来，只需要在业务方法上加上@Trace注解即可，我们还可以为追踪链路增加其他额外的信息，比如记录参数和返回信息。实现方式：在方法上增加@Tag或者@Tags。

```java
@Service
public class UserService {

    @Trace
    @Tags({@Tag(key = "param", value = "arg[0]"),
            @Tag(key = "user", value = "returnedObj")})
    public String  findOrderByUserId(Integer id){
        return "userSuccess";
    }
}
```

[![4vZk60.md.png](https://z3.ax1x.com/2021/10/05/4vZk60.md.png)](https://imgtu.com/i/4vZk60)

# 七、Skywalking集成日志框架

## 1.引入依赖

```xml
<!-- apm-toolkit-logback-1.x -->
<dependency>
  <groupId>org.apache.skywalking</groupId>
  <artifactId>apm-toolkit-logback-1.x</artifactId>
  <version>8.4.0</version>
</dependency>
```

2.logback-spring.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>

    <!-- 引入 Spring Boot 默认的 logback XML 配置文件  -->
    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>

    <!-- 控制台 Appender -->
    <property name="CONSOLE_LOG_PATTERN" value="%clr(%d{${LOG_DATEFORMAT_PATTERN:-yyyy-MM-dd HH:mm:ss.SSS}}){faint} %clr(${LOG_LEVEL_PATTERN:-%5p}) %clr(${PID:- }){magenta} %tid %clr(---){faint} %clr([%15.15t]){faint} %clr(%-40.40logger{39}){cyan} %clr(:){faint} %m%n${LOG_EXCEPTION_CONVERSION_WORD:-%wEx}"/>

    <appender name="console" class="ch.qos.logback.core.ConsoleAppender">
        <!-- 日志的格式化 -->
        <encoder class="ch.qos.logback.core.encoder.LayoutWrappingEncoder">
            <layout class="org.apache.skywalking.apm.toolkit.log.logback.v1.x.TraceIdPatternLogbackLayout">
                <Pattern>${CONSOLE_LOG_PATTERN}</Pattern>
            </layout>
        </encoder>

    </appender>

    <!-- 从 Spring Boot 配置文件中，读取 spring.application.name 应用名 -->
    <springProperty name="applicationName" scope="context" source="spring.application.name" />

    <property name="FILE_LOG_PATTERN" value="%d{${LOG_DATEFORMAT_PATTERN:-yyyy-MM-dd HH:mm:ss.SSS}} ${LOG_LEVEL_PATTERN:-%5p} ${PID:- } %tid --- [%t] %-40.40logger{39} : %m%n${LOG_EXCEPTION_CONVERSION_WORD:-%wEx}"/>

    <!-- 日志文件的路径 -->
    <property name="LOG_FILE" value="/logs/${applicationName}.log"/>

    <!-- 日志文件 Appender -->
    <appender name="file" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_FILE}</file>
        <!--滚动策略，基于时间 + 大小的分包策略 -->
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>${LOG_FILE}.%d{yyyy-MM-dd}.%i.gz</fileNamePattern>
            <maxHistory>7</maxHistory>
            <maxFileSize>10MB</maxFileSize>
        </rollingPolicy>
        <!-- 日志的格式化 -->
        <encoder class="ch.qos.logback.core.encoder.LayoutWrappingEncoder">
            <layout class="org.apache.skywalking.apm.toolkit.log.logback.v1.x.TraceIdPatternLogbackLayout">
                <Pattern>${FILE_LOG_PATTERN}</Pattern>
            </layout>
        </encoder>
    </appender>

    <!-- v8.4.0提供 -->
    <appender name="grpc-log" class="org.apache.skywalking.apm.toolkit.log.logback.v1.x.log.GRPCLogClientAppender"/>

    <!-- 设置 Appender -->
    <root level="INFO">
        <appender-ref ref="console"/>
        <appender-ref ref="file"/>
        <appender-ref ref="grpc-log" />
    </root>



</configuration>
```

打开agent/config/agent.config配置文件，添加如下配置信息：

```
plugin.toolkit.log.grpc.reporter.server_host=${SW_GRPC_LOG_SERVER_HOST:192.168.3.100}
plugin.toolkit.log.grpc.reporter.server_port=${SW_GRPC_LOG_SERVER_PORT:11800}
plugin.toolkit.log.grpc.reporter.max_message_size=${SW_GRPC_LOG_MAX_MESSAGE_SIZE:10485760}
plugin.toolkit.log.grpc.reporter.upstream_timeout=${SW_GRPC_LOG_GRPC_UPSTREAM_TIMEOUT:30}
```

以上配置是默认配置信息,agent与oap在本地的可以不配

| 配置名                                            | 解释                                           | 默认值    |
| ------------------------------------------------- | ---------------------------------------------- | --------- |
| plugin.toolkit.log.transmit_formatted             | 是否以格式化或未格式化的格式传输记录的数据     | true      |
| plugin.toolkit.log.grpc.reporter.server_host      | 指定要向其报告日志数据的grpc服务器的主机       | 127.0.0.1 |
| plugin.toolkit.log.grpc.reporter.server_port      | 指定要向其报告日志数据的grpc服务器的端口       | 11800     |
| plugin.toolkit.log.grpc.reporter.max_message_size | 指定grpc客户端要报告的日志数据的最大大小       | 10485760  |
| plugin.toolkit.log.grpc.reporter.upstream_timeout | 客户端向上游发送数据时将超时多长时间。单位是秒 | 30        |

[Agent配置信息大全]([Setup java agent | Apache SkyWalking](https://skywalking.apache.org/docs/main/v8.4.0/en/setup/service-agent/java-agent/readme/#table-of-agent-configuration-properties))

[![4vmfO0.md.png](https://z3.ax1x.com/2021/10/05/4vmfO0.md.png)](https://imgtu.com/i/4vmfO0)

# 八、进阶扩展：Java Ageng实战

补充中。。。。。。。