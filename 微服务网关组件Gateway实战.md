微服务网关组件Gateway实战

一、微服务网关组件Gateway实战

1、官方文档

https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#gateway-request-predicates-factories

2、Gateway核心概念和工作原理分析

2.1 什么是Spring Cloud Gateway

网关作为流量的入口，常用的功能主要包括路由转发，权限校验，限流等。

Spring Cloud Gateway 是Spring Cloud官方推出的第二代网关框架，定位于取代 Netflix Zuul。相比 Zuul 来说，Spring Cloud Gateway 提供更优秀的性能，更强大的有功能。

Spring Cloud Gateway 是由 WebFlux + Netty + Reactor 实现的响应式的 API 网关。它不能在传统的 servlet 容器中工作，也不能构建成 war 包。

Spring Cloud Gateway 旨在为微服务架构提供一种简单且有效的 API 路由的管理方式，并基于 Filter 的方式提供网关的基本功能，例如说安全认证、监控、限流等等。

2.2 核心概念

​	路由（route) ：路由是网关中最基础的部分，路由信息包括一个ID、一个目的URI、一组断言工厂、一组Filter组成。如果断言为真，则说明请求的URL和配置的路由匹配。

- **Route**: The basic building block of the gateway. It is defined by an ID, a destination URI, a collection of predicates, and a collection of filters. A route is matched if the aggregate predicate is true.

  断言(predicates) ：Java8中的断言函数，SpringCloud Gateway中的断言函数类型是Spring5.0框架中的ServerWebExchange。断言函数允许开发者去定义匹配Http request中的任何信息，比如请求头和参数等。

- **Predicate**: This is a [Java 8 Function Predicate](https://docs.oracle.com/javase/8/docs/api/java/util/function/Predicate.html). The input type is a [Spring Framework `ServerWebExchange`](https://docs.spring.io/spring/docs/5.0.x/javadoc-api/org/springframework/web/server/ServerWebExchange.html). This lets you match on anything from the HTTP request, such as headers or parameters.

  过滤器（Filter) ：SpringCloud Gateway中的filter分为Gateway FilIer和Global Filter。Filter可以对请求和响应进行处理。

- **Filter**: These are instances of [`GatewayFilter`](https://github.com/spring-cloud/spring-cloud-gateway/tree/3.0.x/spring-cloud-gateway-server/src/main/java/org/springframework/cloud/gateway/filter/GatewayFilter.java) that have been constructed with a specific factory. Here, you can modify requests and responses before or after sending the downstream request.

2.3 工作原理

​	Spring Cloud Gateway 的工作原理跟 Zuul 的差不多，最大的区别就是 Gateway 的 Filter 只有 pre 和 post 两种。

<img src="https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/images/spring_cloud_gateway_diagram.png" alt="Spring Cloud Gateway Diagram" style="zoom:50%;" />

客户端向 Spring Cloud Gateway 发出请求，如果请求与网关程序定义的路由匹配，则该请求就会被发送到网关 Web 处理程序，此时处理程序运行特定的请求过滤器链。

过滤器之间用虚线分开的原因是过滤器可能会在发送代理请求的前后执行逻辑。所有 pre 过滤器逻辑先执行，然后执行代理请求；代理请求完成后，执行 post 过滤器逻辑。

3、Spring Cloud整合Gateway使用

3.1引入依赖

```java
<!-- gateway网关 -->
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>
```

注意：会和spring-webmvc的依赖冲突，需要排除spring-webmvc

3.2 编写yml配置文件

```yml
server:
  port: 8888
spring:
  application:
    name: mall-gateway

  cloud:
    gateway:
      discovery:
        locator:
          # 默认为false，设为true开启通过微服务创建路由的功能，即可以通过微服务名访问服务
          # http://localhost:8888/mall-order/order/findOrderByUserId/1
          enabled: true
      # 是否开启网关
      enabled: true

      #设置路由：路由id、路由到微服务的uri、断言
      routes:
        - id: order_route  #路由ID，全局唯一，建议配合服务名
                 #uri: http://localhost:8020  #目标微服务的请求地址和端口
          uri: http://localhost:8020  #lb 整合负载均衡器ribbon,loadbalancer
          predicates:
                   #Path路径匹配
            - Path=/order/**
        - id: user_route
          uri: http://localhost:8040  #lb 整合负载均衡器ribbon,loadbalancer
          predicates:
              - Path=/user/**
```



4、RoutePredicateFactories路由断言工厂配置

https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#the-path-route-predicate-factory

网关启动日志：

[![4rYo8I.md.png](https://z3.ax1x.com/2021/09/25/4rYo8I.md.png)](https://imgtu.com/i/4rYo8I)

4.1 时间匹配问题

可以用在限时抢购的一些场景中。

[![4rYzPs.md.png](https://z3.ax1x.com/2021/09/25/4rYzPs.md.png)](https://imgtu.com/i/4rYzPs)

```yml
spring:
  cloud:
    gateway:
      #设置路由：路由id、路由到微服务的uri、断言
      routes:
        - id: order_route  #路由ID，全局唯一
          uri: http://localhost:8020  #目标微服务的请求地址和端口
          predicates:
            # 测试：http://localhost:8888/order/findOrderByUserId/1
            # 匹配在指定的日期时间之后发生的请求  入参是ZonedDateTime类型
            - After=2021-09-25T9:50:07.783+08:00[Asia/Shanghai]
```

获取ZonedDateTime类型的指定日期时间

```java
ZonedDateTime zonedDateTime = ZonedDateTime.now();//默认时区
// 用指定时区获取当前时间
ZonedDateTime zonedDateTime2 = ZonedDateTime.now(ZoneId.of("Asia/Shanghai"));
```

说明：只有在上面时间后访问才能成功

4.2 Cookie匹配

```yml
spring:
  cloud:
    gateway:
      #设置路由：路由id、路由到微服务的uri、断言
      routes:
        - id: order_route  #路由ID，全局唯一
          uri: http://localhost:8020  #目标微服务的请求地址和端口
          predicates:
            # Cookie匹配
            - Cookie=username, haibin
```

[![4rdZm6.png](https://z3.ax1x.com/2021/09/25/4rdZm6.png)](https://imgtu.com/i/4rdZm6)

4.3 Header匹配

```yml
spring:
  cloud:
    gateway:
      #设置路由：路由id、路由到微服务的uri、断言
      routes:
        - id: order_route  #路由ID，全局唯一
          uri: http://localhost:8020  #目标微服务的请求地址和端口
          predicates:
          # Header匹配  请求中带有请求头名为 x-request-id，其值与 \d+ 正则表达式匹配
          - Header=X-Request-Id, \d+
```

4.4 路径匹配

```yml
spring:
  cloud:
    gateway:
      #设置路由：路由id、路由到微服务的uri、断言
      routes:
        - id: order_route  #路由ID，全局唯一
          uri: http://localhost:8020  #目标微服务的请求地址和端口
          predicates:
            # 测试：http://localhost:8888/order/findOrderByUserId/1
            - Path=/order/**   #Path路径匹配
```

4.5 自定义路由断言工厂

自定义路由断言工厂需要继承 AbstractRoutePredicateFactory 类，重写 apply 方法的逻辑。在 apply 方法中可以通过 exchange.getRequest() 拿到 ServerHttpRequest 对象，从而可以获取到请求的参数、请求方式、请求头等信息。

注意： 命名需要以 RoutePredicateFactory 结尾

```java
/**
 * 自定义RoutePredicateFactory
 */
@Component
@Slf4j
public class CheckAuthRoutePredicateFactory extends AbstractRoutePredicateFactory<CheckAuthRoutePredicateFactory.Config> {

    public CheckAuthRoutePredicateFactory() {
        super(Config.class);
    }

    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        return new GatewayPredicate() {
            @Override
            public boolean test(ServerWebExchange serverWebExchange) {
                log.info("调用CheckAuthRoutePredicateFactory" + config.getName());
                if (config.getName().equals("haibin")){
                    return true;
                }
                return false;
            }
        };
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Collections.singletonList("name");
    }

    /**
     * 需要定义一个内部类，该类用于封装application.yml中的配置
     */
    public static class Config {

        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
```

yml配置

```yml
spring:
  cloud:
    gateway:
      #设置路由：路由id、路由到微服务的uri、断言
      routes:
        - id: order_route  #路由ID，全局唯一
          uri: http://localhost:8020  #目标微服务的请求地址和端口
          predicates:
            # 测试：http://localhost:8888/order/findOrderByUserId/1
            - Path=/order/**   #Path路径匹配
            #自定义CheckAuth断言工厂
            #        - name: CheckAuth
            #          args:
            #            name: fox
            - CheckAuth=haibin 
```

5、GatewayFilterFactories过滤器工厂配置

SpringCloudGateway 内置了很多的过滤器工厂，我们通过一些过滤器工厂可以进行一些业务逻辑处理器，比如添加剔除响应头，添加去除参数等

https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#gatewayfilter-factories

5.1 添加请求头

```yml
spring:
  cloud:
    gateway:
      #设置路由：路由id、路由到微服务的uri、断言
      routes:
        - id: order_route  #路由ID，全局唯一
          uri: http://localhost:8020  #目标微服务的请求地址和端口
          predicates:
             #Path路径匹配
            - Path=/order/**
          #配置过滤器工厂
          filters:
            - AddRequestHeader=X-Request-color, red  #添加请求头
```

其它都是类似的配置，不再一一做介绍，具体可以看官网，下面再讲解下自定义过滤器工厂

5.2 自定义过滤器工厂

继承AbstractNameValueGatewayFilterFactory且我们的自定义名称必须要以GatewayFilterFactory结尾并交给spring管理。

```
@Component
@Slf4j
public class CheckAuthGatewayFilterFactory extends AbstractNameValueGatewayFilterFactory {
    @Override
    public GatewayFilter apply(NameValueConfig config) {
        return (exchange,chain) -> {
            log.info("调用CheckAuthGatewayFilterFactory===" + config.getName() + ":" + config.getValue());
            return chain.filter(exchange);
        };
    }
}
```

配置自定义的过滤器工厂

```
spring:
  cloud:
    gateway:
      #设置路由：路由id、路由到微服务的uri、断言
      routes:
        - id: order_route  #路由ID，全局唯一
          uri: http://localhost:8020  #目标微服务的请求地址和端口
          predicates:
            #Path路径匹配
            - Path=/order/**
          #配置过滤器工厂
          filters:
            - CheckAuth=haibin,男
```

6、GlobalFilters全局过滤器配置

https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#global-filters
[![4rX7rj.png](https://z3.ax1x.com/2021/09/25/4rX7rj.png)](https://imgtu.com/i/4rX7rj)

GlobalFilter 接口和 GatewayFilter 有一样的接口定义，只不过， GlobalFilter 会作用于所有路由。

官方声明：GlobalFilter的接口定义以及用法在未来的版本可能会发生变化。

6.1 LoadBalancerClientFilter

LoadBalancerClientFilter 会查看exchange的属性 ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR 的值（一个URI），如果该值的scheme是 lb，比如：lb://myservice ，它将会使用Spring Cloud的LoadBalancerClient 来将 myservice 解析成实际的host和port，并替换掉 ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR 的内容。
其实就是用来整合负载均衡器Ribbon的

```yml
spring:
  cloud:
    gateway:
      routes:
        - id: order_route
          uri: lb://mall-order
          predicates:
            - Path=/order/**
```

6.2 自定义全局过滤器

```java
@Component
@Order(-1)
@Slf4j
public class CheckAuthFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //校验请求头中的token
        List<String> token =exchange.getRequest().getHeaders().get("token");
        log.info("token:" + token);
        if (token.isEmpty()){
            return chain.filter(exchange);
        }
        // TODO token校验
        return chain.filter(exchange);
    }
}

@Component
@Slf4j
public class CheckIPFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        HttpHeaders headers = exchange.getRequest().getHeaders();
        //模拟对IP的访问限制，即不在IP白名单中就不能调用的需求
        if (getIp(headers).equals("127.0.0.1")){
            log.info("======非法访问======");
            ServerHttpResponse response =exchange.getResponse();
            byte[] bytes = new String("======非法访问======").getBytes();
            response.setStatusCode(HttpStatus.NOT_ACCEPTABLE);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            response.getHeaders().add("Content-Type",
                    "application/json;charset=UTF-8");
            return exchange.getResponse().writeWith(Mono.just(buffer));
        }
        return chain.filter(exchange);
    }

    private String getIp(HttpHeaders headers){
        return headers.getHost().getHostName();
    }

    /**
     * 值越小优先级越高
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
```

7、Gateway Cors跨域配置

![img](https://note.youdao.com/yws/public/resource/e6a0c6530154a553fd11593f56b78c9a/xmlnote/BFE271F9A4F24C6D802BDC70386EE653/15406)

通过yml配置的方式

https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#cors-configuration

```yml
spring:
	#统一配置跨域请求
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowed-origins: "*"
            allowed-headers: "*"
            allow-credentials: true
            allowed-methods:
              - GET
              - POST
              - DELETE
              - PUT
              - PATCH
              - OPTION
```

通过java配置方式

https://github.com/spring-cloud/spring-cloud-gateway/pull/255

```java
@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedMethod("*");
        config.addAllowedOrigin("*");
        config.addAllowedHeader("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(new PathPatternParser());
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }

}
```

8、Gateway整合Sentinel限流实战

https://github.com/alibaba/Sentinel/wiki/%E7%BD%91%E5%85%B3%E9%99%90%E6%B5%81

二、微服务网关组件Gateway源码剖析

思考 http://127.0.0.1:8888/order/findOrderByUserId/2 => http://127.0.0.1:8020/order/findOrderByUserId/2?

下面就一步步分析，如何通过访问网关的ip，然后转发到具体服务中去。

1、DispatcherHandler#handle

```java
public Mono<Void> handle(ServerWebExchange exchange) {
    return this.handlerMappings == null ? this.createNotFoundError() : Flux.fromIterable(this.handlerMappings).concatMap((mapping) -> {
        return mapping.getHandler(exchange);
    }).next().switchIfEmpty(this.createNotFoundError()).flatMap((handler) -> {
        return this.invokeHandler(exchange, handler);
    }).flatMap((result) -> {
        return this.handleResult(exchange, result);
    });
}
```

这里主要处理三个核心的逻辑：

（1）调用HandlerMapping#getHandler,根据请求的信息进行HandlerMapping的查找和路由匹配，并返回相应的handler，这里类似于springMVC的HandlerMapping,主要有：RouterFunctionMapping、RequestMappingHandlerMapping、RoutePredicateHandlerMapping、SimpleUrlHandlerMapping，RequestMappingHandlerMapping主要处理在方法加注解，类似于controller上的注解，SimpleUrlHandlerMapping主要处理通过简单的url暴露的，RoutePredicateHandlerMapping主要处理我们在配置文件上配置的断言方式，所以这次无疑是会找到RoutePredicateHandlerMapping这个HandlerMapping进行处理。

（2）调用HandlerAdapter#handle,根据找到的handler进行HandlerAdapter的查找，HandlerAdapter负责适配HandlerMapping返回的handler,调用handle方法生成HandlerResult。这里类似于SpringMVC的HandlerAdapter，主要有：RequestMappingHandlerAdapter、HandlerFunctionAdapter、SimpleHandlerAdapter，我们这里无疑是会找到SimpleHandlerAdapter进行适配，并调用对应的handle进行处理。

（3）调用HandlerResultHandler#handleResult处理响应结果。

下面就对各个步骤的核心逻辑进行讲解

2、HandlerMapping#getHandler

2.1 获取路由信息

```java
public Flux<Route> getRoutes() {
    Flux<Route> routes = this.routeDefinitionLocator.getRouteDefinitions().map(this::convertToRoute);
    if (!this.gatewayProperties.isFailOnRouteDefinitionError()) {
        routes = routes.onErrorContinue((error, obj) -> {
            if (this.logger.isWarnEnabled()) {
                this.logger.warn("RouteDefinition id " + ((RouteDefinition)obj).getId() + " will be ignored. Definition has invalid configs, " + error.getMessage());
            }

        });
    }

    return routes.map((route) -> {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("RouteDefinition matched: " + route.getId());
        }

        return route;
    });
}
```

以上是会在项目启动的时候RouteDefinitionRouteLocator.getRoutes()将路由信息读入内存，然后在访问的时候通过CachingRouteLocator.getRoutes()进行获取：

[![4gGRbV.md.png](https://z3.ax1x.com/2021/09/27/4gGRbV.md.png)](https://imgtu.com/i/4gGRbV)

2.2 根据请求url和断言条件匹配路由

```java
PathRoutePredicateFactory.java
return new GatewayPredicate() {
    public boolean test(ServerWebExchange exchange) {
        PathContainer path = PathContainer.parsePath(exchange.getRequest().getURI().getRawPath());
        Optional<PathPattern> optionalPathPattern = pathPatterns.stream().filter((pattern) -> {
            return pattern.matches(path);
        }).findFirst();
        if (optionalPathPattern.isPresent()) {
            PathPattern pathPattern = (PathPattern)optionalPathPattern.get();
            PathRoutePredicateFactory.traceMatch("Pattern", pathPattern.getPatternString(), path, true);
            PathMatchInfo pathMatchInfo = pathPattern.matchAndExtract(path);
            ServerWebExchangeUtils.putUriTemplateVariables(exchange, pathMatchInfo.getUriVariables());
            return true;
        } else {
            PathRoutePredicateFactory.traceMatch("Pattern", config.getPatterns(), path, false);
            return false;
        }
    }
    
    
PathPattern.java    
    public boolean matches(PathContainer pathContainer) {
        if (this.head != null) {
            if (!this.hasLength(pathContainer)) {
                if (!(this.head instanceof WildcardTheRestPathElement) && !(this.head instanceof CaptureTheRestPathElement)) {
                    return false;
                }

                pathContainer = EMPTY_PATH;
            }

            PathPattern.MatchingContext matchingContext = new PathPattern.MatchingContext(pathContainer, false);
            return this.head.matches(0, matchingContext);
        } else {
            return !this.hasLength(pathContainer) || this.matchOptionalTrailingSeparator && this.pathContainerIsJustSeparator(pathContainer);
        }
    }
```

这里主要对请求的url跟断言配置的进行匹配，如果匹配到则返回true，执行下一步操作，如果匹配不到则返回false，结束。

3、HandlerAdapter#handle

```java
private Mono<HandlerResult> invokeHandler(ServerWebExchange exchange, Object handler) {
    if (this.handlerAdapters != null) {
        Iterator var3 = this.handlerAdapters.iterator();

        while(var3.hasNext()) {
            HandlerAdapter handlerAdapter = (HandlerAdapter)var3.next();
            if (handlerAdapter.supports(handler)) {
                return handlerAdapter.handle(exchange, handler);
            }
        }
    }

    return Mono.error(new IllegalStateException("No HandlerAdapter: " + handler));
}
```

这是继上一步找到handler后进行查找对应HandlerAdapter的操作，找不到就报错，找到了就调用对应的handle进行处理。接着找到SimpleHandlerAdapter这个适配器进行处理：

```java
public class SimpleHandlerAdapter implements HandlerAdapter {
    public SimpleHandlerAdapter() {
    }

    public boolean supports(Object handler) {
        return WebHandler.class.isAssignableFrom(handler.getClass());
    }

    public Mono<HandlerResult> handle(ServerWebExchange exchange, Object handler) {
        WebHandler webHandler = (WebHandler)handler;
        Mono<Void> mono = webHandler.handle(exchange);
        return mono.then(Mono.empty());
    }
}
```

这里的WebHandler是接口，具体是执行FilteringWebHandler.class上handle:

```java
public Mono<Void> handle(ServerWebExchange exchange) {
    Route route = (Route)exchange.getRequiredAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
    List<GatewayFilter> gatewayFilters = route.getFilters();
    List<GatewayFilter> combined = new ArrayList(this.globalFilters);
    combined.addAll(gatewayFilters);
    AnnotationAwareOrderComparator.sort(combined);
    if (logger.isDebugEnabled()) {
        logger.debug("Sorted gatewayFilterFactories: " + combined);
    }

    return (new FilteringWebHandler.DefaultGatewayFilterChain(combined)).filter(exchange);
}
```

这里主要执行一系列的过滤器链，即责任链

首先，List<GatewayFilter> gatewayFilters = route.getFilters();执行这一句主要是获取路由中配置的GateWayFilter,从这一句也印证了我们之前介绍的，GatewayFilter是配置在routes上的，而且只有配置了才会加载并执行，没有配置是没有获取的：

```yml
routes:
  - id: order_route  #路由ID，全局唯一，建议配合服务名
           #uri: http://localhost:8020  #目标微服务的请求地址和端口
    uri: http://localhost:8020  #lb 整合负载均衡器ribbon,loadbalancer
    predicates:
             #Path路径匹配
      - Path=/order/**
      #- After=2021-09-25T10:04:00.511+08:00[Asia/Shanghai]
      # Cookie匹配
      # - Cookie=username, haibin
      # - CheckAuth=haibin
    filters:
     # - AddRequestHeader=X-Request-color, red  #添加请求头
      - CheckAuth=haibin,男
```

[![42FpQJ.md.png](https://z3.ax1x.com/2021/09/27/42FpQJ.md.png)](https://imgtu.com/i/42FpQJ)

第二，List<GatewayFilter> combined = new ArrayList(this.globalFilters);执行这一行主要将所有的globalFilters获取到，并转换成GatewayFilter，这里主要运用适配器的设计模式，通过GatewayFilterAdapter将GlobalFilter适配成GatewayFilter。

第三，将所有的过Filter获取，并排序后，执行

(new FilteringWebHandler.DefaultGatewayFilterChain(combined)).filter(exchange)将组合成链，这里运用了责任链的设计模式，并且一个个执行各个过滤器。

所有的过滤器：

[![42ElwR.md.png](https://z3.ax1x.com/2021/09/27/42ElwR.md.png)](https://imgtu.com/i/42ElwR)

核心执行逻辑：

```
FilterWebHandler.class

public Mono<Void> filter(ServerWebExchange exchange) {
    return Mono.defer(() -> {
        if (this.index < this.filters.size()) {
            GatewayFilter filter = (GatewayFilter)this.filters.get(this.index);
            FilteringWebHandler.DefaultGatewayFilterChain chain = new FilteringWebHandler.DefaultGatewayFilterChain(this, this.index + 1);
            return filter.filter(exchange, chain);
        } else {
            return Mono.empty();
        }
    });
}
```

这里会一个个循环执行return filter.filter(exchange, chain)，具体是哪个GatewayFilter，就会执行对应的filter逻辑，下面主要介绍几个比较重要的GatewayFilter

3.1 NoLoadBalancerClientFilter#filter

这是网关负责负载均衡的GlobalFilter

```java
public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        URI url = (URI)exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
        String schemePrefix = (String)exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_SCHEME_PREFIX_ATTR);
        if (url != null && ("lb".equals(url.getScheme()) || "lb".equals(schemePrefix))) {
            ServerWebExchangeUtils.addOriginalRequestUrl(exchange, url);
            if (log.isTraceEnabled()) {
                log.trace("LoadBalancerClientFilter url before: " + url);
            }

            ServiceInstance instance = this.choose(exchange);
            if (instance == null) {
                throw NotFoundException.create(this.properties.isUse404(), "Unable to find instance for " + url.getHost());
            } else {
                URI uri = exchange.getRequest().getURI();
                String overrideScheme = instance.isSecure() ? "https" : "http";
                if (schemePrefix != null) {
                    overrideScheme = url.getScheme();
                }

                URI requestUrl = this.loadBalancer.reconstructURI(new DelegatingServiceInstance(instance, overrideScheme), uri);
                if (log.isTraceEnabled()) {
                    log.trace("LoadBalancerClientFilter url chosen: " + requestUrl);
                }

                exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, requestUrl);
                return chain.filter(exchange);
            }
        } else {
            return chain.filter(exchange);
        }
    }
```

通过执行ServiceInstance instance = this.choose(exchange);去服务中心获取对应微服务名的实例列表，通过负载均衡算法选出一个服务实例，通过执行URI requestUrl = this.loadBalancer.reconstructURI(new DelegatingServiceInstance(instance, overrideScheme), uri);重建请求url，通过执行exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, requestUrl);绑定url到请求的上下文。

3.2 NettyRoutingFilter#filter

这里通过httpClient发送请求到下游服务，即这里请求的url为：

```java
public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    URI requestUrl = (URI)exchange.getRequiredAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
    String scheme = requestUrl.getScheme();
    if (!ServerWebExchangeUtils.isAlreadyRouted(exchange) && ("http".equals(scheme) || "https".equals(scheme))) {
        ServerWebExchangeUtils.setAlreadyRouted(exchange);
        ServerHttpRequest request = exchange.getRequest();
        HttpMethod method = HttpMethod.valueOf(request.getMethodValue());
        String url = requestUrl.toASCIIString();
        HttpHeaders filtered = HttpHeadersFilter.filterRequest(this.getHeadersFilters(), exchange);
        DefaultHttpHeaders httpHeaders = new DefaultHttpHeaders();
        filtered.forEach(httpHeaders::set);
        boolean preserveHost = (Boolean)exchange.getAttributeOrDefault(ServerWebExchangeUtils.PRESERVE_HOST_HEADER_ATTRIBUTE, false);
        Route route = (Route)exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        Flux<HttpClientResponse> responseFlux = ((RequestSender)this.getHttpClient(route, exchange).headers((headers) -> {
            headers.add(httpHeaders);
            headers.remove("Host");
            if (preserveHost) {
                String host = request.getHeaders().getFirst("Host");
                headers.add("Host", host);
            }

        }).request(method).uri(url)).send((req, nettyOutbound) -> {
            if (log.isTraceEnabled()) {
                nettyOutbound.withConnection((connection) -> {
                    log.trace("outbound route: " + connection.channel().id().asShortText() + ", inbound: " + exchange.getLogPrefix());
                });
            }

            return nettyOutbound.send(request.getBody().map(this::getByteBuf));
        }).responseConnection((res, connection) -> {
            exchange.getAttributes().put(ServerWebExchangeUtils.CLIENT_RESPONSE_ATTR, res);
            exchange.getAttributes().put(ServerWebExchangeUtils.CLIENT_RESPONSE_CONN_ATTR, connection);
            ServerHttpResponse response = exchange.getResponse();
            HttpHeaders headers = new HttpHeaders();
            res.responseHeaders().forEach((entry) -> {
                headers.add((String)entry.getKey(), (String)entry.getValue());
            });
            String contentTypeValue = headers.getFirst("Content-Type");
            if (StringUtils.hasLength(contentTypeValue)) {
                exchange.getAttributes().put("original_response_content_type", contentTypeValue);
            }

            this.setResponseStatus(res, response);
            HttpHeaders filteredResponseHeaders = HttpHeadersFilter.filter(this.getHeadersFilters(), headers, exchange, Type.RESPONSE);
            if (!filteredResponseHeaders.containsKey("Transfer-Encoding") && filteredResponseHeaders.containsKey("Content-Length")) {
                response.getHeaders().remove("Transfer-Encoding");
            }

            exchange.getAttributes().put(ServerWebExchangeUtils.CLIENT_RESPONSE_HEADER_NAMES, filteredResponseHeaders.keySet());
            response.getHeaders().putAll(filteredResponseHeaders);
            return Mono.just(res);
        });
        Duration responseTimeout = this.getResponseTimeout(route);
        if (responseTimeout != null) {
            responseFlux = responseFlux.timeout(responseTimeout, Mono.error(new TimeoutException("Response took longer than timeout: " + responseTimeout))).onErrorMap(TimeoutException.class, (th) -> {
                return new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT, th.getMessage(), th);
            });
        }

        return responseFlux.then(chain.filter(exchange));
    } else {
        return chain.filter(exchange);
    }
}
```

通过HttpClient.ResponseReceiver<?> send(BiFunction<? super HttpClientRequest, ? super NettyOutbound, ? extends Publisher<Void>> var1)发送出去，

4、HandlerResultHandler#handleResult

三、智投项目网关

1、引入依赖

```xml
<!--GateWay ⽹关,内置webflux 依赖-->
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>
```

2、路由配置

```properties
spring.cloud.gateway.routes[0].id = system_gate_route
spring.cloud.gateway.routes[0].uri = lb://rpa-console-system-api
spring.cloud.gateway.routes[0].predicates[0] = Path=/api/system/**

 spring.cloud.gateway.routes[1].id = material_gate_route
 spring.cloud.gateway.routes[1].uri = lb://rpa-console-material-api
 spring.cloud.gateway.routes[1].predicates[0] = Path=/api/material/**

 spring.cloud.gateway.routes[2].id = tencentad_gate_route
 spring.cloud.gateway.routes[2].uri = lb://rpa-console-tencent-ad-api
 spring.cloud.gateway.routes[2].predicates[0] = Path=/api/tencentad/**

 spring.cloud.gateway.routes[3].id = config_gate_route
 spring.cloud.gateway.routes[3].uri = lb://rpa-console-config-api
 spring.cloud.gateway.routes[3].predicates[0] = Path=/api/config/**

 spring.cloud.gateway.routes[4].id = bi_gate_route
 spring.cloud.gateway.routes[4].uri = lb://rpa-console-bi-api
 spring.cloud.gateway.routes[4].predicates[0] = Path=/api/bi/**
```

3、全局过滤器配置，全局过滤器会对所有路由匹配过的路径进行增强处理

    @Component
    @Slf4j
    public class GatewayFilter implements GlobalFilter, Ordered {
    	private final FilterService filterService;
    
    	@Autowired
    	GatewayFilter(FilterService filterService, BlackListConfig blackListConfig) {
        	this.filterService = filterService;
    	}
    
    	@Override
    	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        	// 从上下⽂中取出request和response对象
        	ServerHttpRequest request = exchange.getRequest();
        	ServerHttpResponse response = exchange.getResponse();
    
        	//校验黑名单
        	Mono<Void> blresponse = filterService.validateBlackList(request, response);
        	if (blresponse != null) {
            	return blresponse;
        	}
        	//校验白名单API，无需token
        	if (filterService.validateWhiteList(exchange, chain, request)) {
            	return chain.filter(exchange);
        	}
        	//Token校验
        	Mono<Void> tokenResponse = filterService.validateToken(request, response);
        	if (tokenResponse != null) {
            	return tokenResponse;
        	}
        	//校验忽略名单API，需校验token
        	if (filterService.validateIgnoreList(exchange, chain, request)) {
            	return chain.filter(exchange);
        	}
        	//权限URL校验
        	Mono<Void> upResponse = filterService.validateUserPermission(request, response);
        	if (upResponse != null) {
            	return upResponse;
        	}
    
        	log.info("合法请求，放⾏...");
        	// 合法请求，放⾏，执⾏后续的过滤器
        	return chain.filter(exchange);
        }
    }
