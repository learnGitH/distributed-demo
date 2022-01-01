Netty编程实战

# 一、IO模型

IO模型就是说用什么样的通道进行数据的发送和接收，Java共支持3种网络编程IO模式：BIO，NIO,AIO

## 1、BIO（Blocking IO）

BIO是同步阻塞模型，一个客户端连接对应一个处理线程，典型的模型如下：

[![4HBcA1.md.png](https://z3.ax1x.com/2021/10/02/4HBcA1.md.png)](https://imgtu.com/i/4HBcA1)

BIO服务端示例代码：

```java
/**
 * bio服务端
 * @author shb
 */
public class SocketServer {

    static ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8888);
        while(true){
            System.out.println("等待客户端的连接。。");
            //阻塞方法,如果没有客户端连接就会一直阻塞在这里
            Socket clientSocket = serverSocket.accept();
            System.out.println("有客户端连接了。。。。。");
            //单线程的方式
            handler(clientSocket);

            //多线程的方式
           // threadPool.execute(() -> {
            //    try {
            //        handler(clientSocket);
            //    } catch (IOException e) {
            //        e.printStackTrace();
            //    }
            //});
        }
    }

    private static void handler(Socket clientSocket) throws IOException {
        byte[] bytes = new byte[12];
        System.out.println("准备read。。");
        //接收客户端的数据，阻塞方法，没有数据可读时就阻塞
        int read = clientSocket.getInputStream().read(bytes);
        System.out.println("read完毕。。");
        if (read != -1){
            System.out.println("接收到客户端的数据：" + new String(bytes,0,read));
        }
        clientSocket.getOutputStream().write("HelloClient".getBytes());
        clientSocket.getOutputStream().flush();
    }

}
```

BIO客户端示例代码：

```java
/**
 * bio客户端
 * @author shb
 */
public class SocketClient {

    public static void main(String[] args) throws IOException {
        //连接服务器的地址和端口
        Socket socket = new Socket("localhost",8888);
        //向服务器发送数据
        socket.getOutputStream().write("bio客户端测试".getBytes());
        socket.getOutputStream().flush();
        System.out.println("向服务端发送数据结束!!!!!");
        byte[] bytes = new byte[1024];
        //接收服务端回传的数据
        socket.getInputStream().read(bytes);
        System.out.println("接收到服务端的数据：" + new String(bytes));
        socket.close();
    }

}
```

缺点：

由于accept()是一个阻塞方法，如果没有客户端连接的话，将一直阻塞着，而且在单线程的情况下由于read也是一个阻塞的操作，如果连接不做数据的读写操作会导致线程阻塞，浪费资源，而且其它客户端也进不来，多线程可以解决这个问题，但是很多客户端如果仅仅只是连接下，又不做读写操作，那么会操作大量的资源浪费，如果线程很多，会导致服务器线程太多，压力太大，比如[C10K](https://juejin.cn/post/6844903815137722376)问题。

应用场景：

BIO方式适用于连接数目比较小并且一次发送大量数据的场景，这种方式对服务器资源要求比较高，并发局限于应用中。但程序简单易理解。

## 2、NIO（Non Blocking IO）

[![4H60eg.md.png](https://z3.ax1x.com/2021/10/02/4H60eg.md.png)](https://imgtu.com/i/4H60eg)

同步非阻塞，服务器实现模式为**一个线程可以处理多个请求(连接)**，客户端发送的连接请求都会注册到**多路复用器selector**上，多路复用器轮询到连接有IO请求就进行处理，JDK1.4开始引入。

**应用场景：**

NIO方式适用于连接数目多且连接比较短（轻操作） 的架构， 比如聊天服务器， 弹幕系统， 服务器间通讯，编程比较复杂

NIO非阻塞服务端代码示例：

```java
package com.haibin.netty.io.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * nio服务端示例
 * @author shb
 */
public class NioServer {
    //保存客户端连接
    static List<SocketChannel> channelList = new ArrayList<>();
    public static void main(String[] args) throws IOException {
        //创建NIO ServerSocketChannel,与BIO的serverSocket类似
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.socket().bind(new InetSocketAddress(8888));
        //设置ServerSocketChannel为非阻塞
        serverSocket.configureBlocking(false);
        System.out.println("服务启动成功");
        while(true){
            //非阻塞模式accept方法不会阻塞，否则会阻塞
            //NIO的非阻塞是由操作系统内部实现的，底层调用了linux内核的accept函数
            SocketChannel socketChannel = serverSocket.accept();
            if (socketChannel != null){
                System.out.println("连接成功");
                //设置SocketChannel为非阻塞
                socketChannel.configureBlocking(false);
                //保存客户端连接在List中
                channelList.add(socketChannel);
            }
            //遍历连接进行数据读取
            Iterator<SocketChannel> iterator = channelList.iterator();
            while(iterator.hasNext()){
                SocketChannel sc = iterator.next();
                ByteBuffer byteBuffer = ByteBuffer.allocate(128);
                //非阻塞模式read方法不会阻塞，否则会阻塞
                int len = sc.read(byteBuffer);
                //如果有数据，把数据打印出来
                if (len > 0){
                    System.out.println("接收到消息：" + new String(byteBuffer.array()));
                }else if (len == -1){       //如果客户端断开，把socket从集合中去掉
                    iterator.remove();
                    System.out.println("客户端断开连接");
                }
            }
        }

    }

}
```

总结：如果连接数太多的话，会有大量的无效遍历，假如有10000个连接，其中只有1000个连接有写数据，但是由于其他9000个连接并没有断开，我们还是要每次轮询遍历一万次，其中有十分之九的遍历都是无效的，这显然不是一个让人很满意的状态。

NIO引入多路复用器代码示例：

```java
package com.haibin.netty.io.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * nioSelector服务端示例
 * @author shb
 */
public class NioSelectorServer {

    public static void main(String[] args) throws IOException {
        //创建NIO ServerSocketChannel
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.socket().bind(new InetSocketAddress(9000));
        //设置ServerSocketChannel为非阻塞
        serverSocket.configureBlocking(false);
        //打开Selector处理channel,即创建epoll
        Selector selector = Selector.open();
        //把ServerSocketChannel注册到selector上，并且selector对客户端accept连接操作感兴趣
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("服务启动成功");
        while(true){
            //阻塞等待需要处理的事件发生
            selector.select();
            //获取selector中注册的全部事件的SelectionKey实例
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            //遍历SelectionKey对事件进行处理
            while(iterator.hasNext()){
                SelectionKey key = iterator.next();
                //如果是OP_ACCEPT事件，则进行连接获取贺事件注册
                if (key.isAcceptable()){
                    ServerSocketChannel server = (ServerSocketChannel)key.channel();
                    SocketChannel socketChannel = server.accept();
                    socketChannel.configureBlocking(false);
                    //这里只注册了读事件，如果需要给客户端发送数据可以注册写事件
                    socketChannel.register(selector,SelectionKey.OP_READ);
                    System.out.println("客户端连接成功");
                }else if (key.isReadable()){        //如果是OP_READ事件，则进行读取贺打印
                    SocketChannel socketChannel = (SocketChannel)key.channel();
                    ByteBuffer byteBuffer = ByteBuffer.allocate(128);
                    int len = socketChannel.read(byteBuffer);
                    //如果有数据，把数据打印出来
                    if (len > 0){
                        System.out.println("接收到消息：" + new String(byteBuffer.array()));
                    }else if(len == -1){        //如果客户端断开连接，关闭Socket
                        System.out.println("客户端断开连接");
                        socketChannel.close();
                    }
                }
                //从事件集合里删除本次处理的key,防止下次select重复处理
                iterator.remove();
            }
        }
    }

}
```

NIO 有三大核心组件： **Channel(通道)， Buffer(缓冲区)，Selector(多路复用器)**

1)、channel 类似于流，每个 channel 对应一个 buffer缓冲区，buffer 底层就是个数组

2)、channel 会注册到 selector 上，由 selector 根据 channel 读写事件的发生将其交由某个空闲的线程处理

3)、NIO 的 Buffer 和 channel 都是既可以读也可以写

![https://note.youdao.com/yws/public/resource/916f44987d1fe0e35ec935bf5391d762/xmlnote/F69C7F7706E64740A629E8E1056F5DCE/106569](https://note.youdao.com/yws/public/resource/916f44987d1fe0e35ec935bf5391d762/xmlnote/F69C7F7706E64740A629E8E1056F5DCE/106569)

NIO底层在JDK1.4版本是用linux的内核函数select()或poll()来实现，跟上面的NioServer代码类似，selector每次都会轮询所有的sockchannel看下哪个channel有读写事件，有的话就处理，没有就继续遍历，JDK1.5开始引入了epoll基于事件响应机制来优化NIO。

底层实现：

NioSelectorServer 代码里如下几个方法非常重要，我们从Hotspot与Linux内核函数级别来理解下

```java
Selector.open()  //创建多路复用器
socketChannel.register(selector, SelectionKey.OP_READ)  //将channel注册到多路复用器上
selector.select()  //阻塞等待需要处理的事件发生
```

![img](https://note.youdao.com/yws/public/resource/916f44987d1fe0e35ec935bf5391d762/xmlnote/E7B425DD0F1142E28C7174E865FF0A05/106573)

总结：NIO整个调用流程就是Java调用了操作系统的内核函数来创建Socket，获取到Socket的文件描述符，再创建一个Selector对象，对应操作系统的Epoll描述符，将获取到的Socket连接的文件描述符的事件绑定到Selector对应的Epoll文件描述符上，进行事件的异步通知，这样就实现了使用一条线程，并且不需要太多的无效的遍历，将事件处理交给了操作系统内核(操作系统中断程序实现)，大大提高了效率。

Epoll函数详解

(1) int epoll_create(int size);       

​      [<img src="https://z3.ax1x.com/2021/10/02/4bpyqK.md.png" alt="4bpyqK.md.png" style="zoom: 80%;" />](https://imgtu.com/i/4bpyqK)

创建一个epoll实例，并返回一个非负数作为文件描述符，用于对epoll接口的所有后续调用。参数size代表可能会容纳size个描述符，但size不是一个最大值，只是提示操作系统它的数量级，现在这个参数基本上已经弃用了。

所以说调用Selector selector = Selector.open();执行这一句本质就是创建了一个epoll对象，是通过调用操作系统函数epoll_create创建的。

[![4bCFnP.md.png](https://z3.ax1x.com/2021/10/02/4bCFnP.md.png)](https://imgtu.com/i/4bCFnP)

（2） int epoll_ctl(int epfd, int op, int fd, struct epoll_event *event);              

使用文件描述符epfd引用的epoll实例，对目标文件描述符fd执行op操作。

参数epfd表示epoll对应的文件描述符，参数fd表示socket对应的文件描述符。

参数op有以下几个值：

EPOLL_CTL_ADD：注册新的fd到epfd中，并关联事件event；

EPOLL_CTL_MOD：修改已经注册的fd的监听事件；

EPOLL_CTL_DEL：从epfd中移除fd，并且忽略掉绑定的event，这时event可以为null；

参数event是一个结构体

```c
struct epoll_event {
    __uint32_t   events;      /* Epoll events */
    epoll_data_t data;        /* User data variable */
    };

    typedef union epoll_data {
    void        *ptr;
    int          fd;
    __uint32_t   u32;
    __uint64_t   u64;
    } epoll_data_t;
```

events有很多可选值，这里只举例最常见的几个：

EPOLLIN ：表示对应的文件描述符是可读的；

EPOLLOUT：表示对应的文件描述符是可写的；

EPOLLERR：表示对应的文件描述符发生了错误；

成功则返回0，失败返回-1

（3）int epoll_wait(int epfd, struct epoll_event *events, int maxevents, int timeout);      

等待文件描述符epfd上的事件。

epfd是Epoll对应的文件描述符，events表示调用者所有可用事件的集合，maxevents表示最多等到多少个事件就返回，timeout是超时时间。

I/O多路复用底层主要用的Linux 内核·函数（select，poll，epoll）来实现，windows不支持epoll实现，windows底层是基于winsock2的select函数实现的(不开源)

|              | **select**                               | **poll**                                 | **epoll(jdk 1.5及以上)**                                     |
| ------------ | ---------------------------------------- | ---------------------------------------- | ------------------------------------------------------------ |
| **操作方式** | 遍历                                     | 遍历                                     | 回调                                                         |
| **底层实现** | 数组                                     | 链表                                     | 哈希表                                                       |
| **IO效率**   | 每次调用都进行线性遍历，时间复杂度为O(n) | 每次调用都进行线性遍历，时间复杂度为O(n) | 事件通知方式，每当有IO事件就绪，系统注册的回调函数就会被调用，时间复杂度O(1) |
| **最大连接** | 有上限                                   | 无上限                                   | 无上限                                                       |

## 3、AIO（NIO 2.0）

**异步非阻塞， 由操作系统完成后回调通知服务端程序启动线程去处理， 一般适用于连接数较多且连接时间较长的应用**

**应用场景：**

AIO方式适用于连接数目多且连接比较长(重操作)的架构，JDK7 开始支持

AIO代码示例：

```java
package com.haibin.netty.io.aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class AIOServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        final AsynchronousServerSocketChannel serverChannel = AsynchronousServerSocketChannel.open().bind(new InetSocketAddress(8888));
        serverChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {

            @Override
            public void completed(AsynchronousSocketChannel socketChannel, Object attachment) {
                try{
                    System.out.println("2--"+Thread.currentThread().getName());
                    //再此接收客户端连接，如果不写这行代码后面的客户端连接不上服务器
                    serverChannel.accept(attachment,this);
                    System.out.println(socketChannel.getRemoteAddress());
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    socketChannel.read(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
                        @Override
                        public void completed(Integer result, ByteBuffer attachment) {
                            System.out.println("3--" + Thread.currentThread().getName());
                            buffer.flip();
                            System.out.println(new String(buffer.array(),0,result));
                            socketChannel.write(ByteBuffer.wrap("HelloClient".getBytes()));
                        }

                        @Override
                        public void failed(Throwable exc, ByteBuffer attachment) {
                            exc.printStackTrace();
                        }
                    });
                }catch (IOException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                exc.printStackTrace();
            }
        });
        System.out.println("1--"+Thread.currentThread().getName());
        Thread.sleep(Integer.MAX_VALUE);
    }
}
```

```java
package com.haibin.netty.io.aio;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;

public class AIOClient {
    public static void main(String... args) throws Exception {
        AsynchronousSocketChannel socketChannel = AsynchronousSocketChannel.open();
        socketChannel.connect(new InetSocketAddress("127.0.0.1", 9000)).get();
        socketChannel.write(ByteBuffer.wrap("HelloServer".getBytes()));
        ByteBuffer buffer = ByteBuffer.allocate(512);
        Integer len = socketChannel.read(buffer).get();
        if (len != -1) {
            System.out.println("客户端收到信息：" + new String(buffer.array(), 0, len));
        }
    }
}
```

**BIO、 NIO、 AIO 对比：**

![img](https://note.youdao.com/yws/public/resource/916f44987d1fe0e35ec935bf5391d762/xmlnote/17DCC73717114E569D317F28E1D27261/84315)

# 二、Netty核心概念详解

## 1、概述

NIO 的类库和 API 繁杂， 使用麻烦： 需要熟练掌握Selector、 ServerSocketChannel、 SocketChannel、 ByteBuffer等。 

开发工作量和难度都非常大： 例如客户端面临断线重连、 网络闪断、心跳处理、半包读写、 网络拥塞和异常流的处理等等。

Netty 对 JDK 自带的 NIO 的 API 进行了良好的封装，解决了上述问题。且Netty拥有高性能、 吞吐量更高，延迟更低，减少资源消耗，最小化不必要的内存复制等优点。

Netty 现在都在用的是4.x，5.x版本已经废弃，Netty 4.x 需要JDK 6以上版本支持

## 2、Netty的使用场景

1）互联网行业：在分布式系统中，各个节点之间需要远程服务调用，高性能的 RPC 框架必不可少，Netty 作为异步高性能的通信框架，往往作为基础通信组件被这些 RPC 框架使用。典型的应用有：阿里分布式服务框架 Dubbo 的 RPC 框架使用 Dubbo 协议进行节点间通信，Dubbo 协议默认使用 Netty 作为基础通信组件，用于实现。各进程节点之间的内部通信。Rocketmq底层也是用的Netty作为基础通信组件。

2）游戏行业：无论是手游服务端还是大型的网络游戏，Java 语言得到了越来越广泛的应用。Netty 作为高性能的基础通信组件，它本身提供了 TCP/UDP 和 HTTP 协议栈。

3）大数据领域：经典的 Hadoop 的高性能通信和序列化组件 Avro 的 RPC 框架，默认采用 Netty 进行跨界点通信，它的 Netty Service 基于 Netty 框架二次封装实现。

netty相关开源项目：https://netty.io/wiki/related-projects.html

## 3、Netty通讯示例

引入maven依赖：

```java
<dependency>
    <groupId>io.netty</groupId>
    <artifactId>netty-all</artifactId>
    <version>4.1.35.Final</version>
</dependency>
```

服务端代码：

```java
package com.haibin.netty.io.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Netty服务端demo
 * @author shb
 */
public class NettyService {

    public static void main(String[] args) throws InterruptedException {
        //创建两个线程组bossGroup和workGroup,含有的子线程NioEventLoop的个数默认为cpu核数的两倍
        //bossGroup只是处理连接请求，真正的和客户端业务处理，会交给workGroup完成
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try{
            //创建服务器端的启动对象
            ServerBootstrap bootstrap = new ServerBootstrap();
            //使用链式编程来配置参数
            bootstrap.group(bossGroup,workerGroup)  //设置两个线程组
                    .channel(NioServerSocketChannel.class)      //使用NioServerSocketChannel作为服务器的通道实现
                    //初始化服务器连接队列大小，服务器处理客户端连接请求是顺序处理的，所以同一时间只能处理一个客户端连接。
                    //多个客户端同时来的时候，服务端将不能处理的客户端连接请求放在队列中等待处理
                    .option(ChannelOption.SO_BACKLOG,1024)
                    .childHandler(new ChannelInitializer<SocketChannel>() {     //创建通道初始化对象，设置初始化参数
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            //对workGroup的SocketChannel设置处理器
                            socketChannel.pipeline().addLast(new NettyServerHandler());
                        }
                    });
            System.out.println("netty server start。。");
            //绑定一个端口并且同步，生成了一个ChannelFuture异步对象，通过isDone()等方法可以判断异步事件的执行情况
            //启动服务器（并绑定端口），bind是异步操作，sync方法是等待异步操作执行完毕
            ChannelFuture cf = bootstrap.bind(8888).sync();
            //给cf注册监听器，监听我们关心的事件
            /*cf.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isSuccess()){
                        System.out.println("监听端口9000成功");
                    }else{
                        System.out.println("监听端口9000失败");
                    }
                }
            });*/
            //对通道关闭进行监听，closeFuture是异步操作，监听通道关闭
            //通过sync方法同步等待通道关闭处理完毕，这里会阻塞等待通道关闭完成
            cf.channel().closeFuture().sync();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
```

```java
package com.haibin.netty.io.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

/**
 * 自定义Handler需要继承netty规定好的某个HandlerAdapter(规范)
 * @author shb
 */
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * 读取客户端发送的数据
     *
     * @param ctx 上下文对象，含有通道channel,管道pipeline
     * @param msg 就是客户端发送的数据
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("服务器读取线程 " + Thread.currentThread().getName());
       // Channel channel = ctx.channel();
       // ChannelPipeline pipeline = ctx.pipeline();  //本质是一个双向链接，出站入站
        //将 msg 转成一个ByteBuf,类似NIO的ByteBuffer
        ByteBuf buf = (ByteBuf)msg;
        System.out.println("客户端发送消息是：" + buf.toString(CharsetUtil.UTF_8));
        super.channelRead(ctx, msg);
    }

    /**
     * 数据读取完毕处理方法
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ByteBuf buf = Unpooled.copiedBuffer("HelloClient", CharsetUtil.UTF_8);
        ctx.writeAndFlush(buf);
    }

    /**
     * 处理异常，一般是需要关闭通道
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
```

客户端代码：

```java
package com.haibin.netty.io.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Netty客户端demo
 * @author shb
 */
public class NettyClient {

    public static void main(String[] args) throws InterruptedException {
        //客户端需要一个事件循环组
        EventLoopGroup group = new NioEventLoopGroup();
        try{
            //创建客户端启动对象
            //注意客户端使用的不是ServerBootstrap而是Bootstrap
            Bootstrap bootstrap =   new Bootstrap();
            //设置相关参数
            bootstrap.group(group)      //设置线程组
                    .channel(NioSocketChannel.class)      //使用NioSocketChannel作为客户端的通道实现
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            //加入处理器
                            socketChannel.pipeline().addLast(new NettyClientHandler());
                        }
                    });
            System.out.println("netty client start");
            //启动客户端去连接服务器
            ChannelFuture channelFuture = bootstrap.connect("127.0.0.1",8888).sync();
            //对关闭通道进行监听
            channelFuture.channel().closeFuture().sync();
        }finally {
            group.shutdownGracefully();
        }
    }

}
```

```java
package com.haibin.netty.io.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ByteBuf buf = Unpooled.copiedBuffer("HelloServer", CharsetUtil.UTF_8);
        ctx.writeAndFlush(buf);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        System.out.println("收到服务端的消息:" + buf.toString(CharsetUtil.UTF_8));
        System.out.println("服务端的地址： " + ctx.channel().remoteAddress());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
```

## 4、Netty线程模型

[<img src="https://z3.ax1x.com/2021/10/02/4bLh4I.md.png" alt="4bLh4I.md.png" style="zoom:80%;" />](https://imgtu.com/i/4bLh4I)

**模型解释：**

1) Netty 抽象出两组线程池BossGroup和WorkerGroup，BossGroup专门负责接收客户端的连接, WorkerGroup专门负责网络的读写

2) BossGroup和WorkerGroup类型都是NioEventLoopGroup

3) NioEventLoopGroup 相当于一个事件循环**线程组**, 这个组中含有多个事件循环线程 ， 每一个事件循环线程是NioEventLoop

4) 每个NioEventLoop都有一个selector , 用于监听注册在其上的socketChannel的网络通讯

5) 每个Boss  NioEventLoop线程内部循环执行的步骤有 3 步

- 处理accept事件 , 与client 建立连接 , 生成 NioSocketChannel 
- 将NioSocketChannel注册到某个worker  NIOEventLoop上的selector
- 处理任务队列的任务 ， 即runAllTasks

6) 每个worker  NIOEventLoop线程循环执行的步骤

- 轮询注册到自己selector上的所有NioSocketChannel 的read, write事件
- 处理 I/O 事件， 即read , write 事件， 在对应NioSocketChannel 处理业务
- runAllTasks处理任务队列TaskQueue的任务 ，一些耗时的业务处理一般可以放入TaskQueue中慢慢处理，这样不影响数据在 pipeline 中的流动处理

7) 每个worker NIOEventLoop处理NioSocketChannel业务时，会使用 pipeline (管道)，管道中维护了很多 handler 处理器用来处理 channel 中的数据

## 5、Netty模块组件

**【Bootstrap、ServerBootstrap】：**

Bootstrap 意思是引导，一个 Netty 应用通常由一个 Bootstrap 开始，主要作用是配置整个 Netty 程序，串联各个组件，Netty 中 Bootstrap 类是客户端程序的启动引导类，ServerBootstrap 是服务端启动引导类。

**【Future、ChannelFuture】：**

正如前面介绍，在 Netty 中所有的 IO 操作都是异步的，不能立刻得知消息是否被正确处理。

但是可以过一会等它执行完成或者直接注册一个监听，具体的实现就是通过 Future 和 ChannelFutures，他们可以注册一个监听，当操作执行成功或失败时监听会自动触发注册的监听事件。

**【Channel】：**

Netty 网络通信的组件，能够用于执行网络 I/O 操作。Channel 为用户提供：

1）当前网络连接的通道的状态（例如是否打开？是否已连接？）

2）网络连接的配置参数 （例如接收缓冲区大小）

3）提供异步的网络 I/O 操作(如建立连接，读写，绑定端口)，异步调用意味着任何 I/O 调用都将立即返回，并且不保证在调用结束时所请求的 I/O 操作已完成。

4）调用立即返回一个 ChannelFuture 实例，通过注册监听器到 ChannelFuture 上，可以 I/O 操作成功、失败或取消时回调通知调用方。

5）支持关联 I/O 操作与对应的处理程序。

不同协议、不同的阻塞类型的连接都有不同的 Channel 类型与之对应。

下面是一些常用的 Channel 类型：

NioSocketChannel，异步的客户端 TCP Socket 连接。

 NioServerSocketChannel，异步的服务器端 TCP Socket 连接。 

NioDatagramChannel，异步的 UDP 连接。 

NioSctpChannel，异步的客户端 Sctp 连接。

 NioSctpServerChannel，异步的 Sctp 服务器端连接。 

这些通道涵盖了 UDP 和 TCP 网络 IO 以及文件 IO。              

**【Selector】：**

Netty 基于 Selector 对象实现 I/O 多路复用，通过 Selector 一个线程可以监听多个连接的 Channel 事件。

当向一个 Selector 中注册 Channel 后，Selector 内部的机制就可以自动不断地查询(Select) 这些注册的 Channel 是否有已就绪的 I/O 事件（例如可读，可写，网络连接完成等），这样程序就可以很简单地使用一个线程高效地管理多个 Channel 。

**【NioEventLoop】：**

NioEventLoop 中维护了一个线程和任务队列，支持异步提交执行任务，线程启动时会调用 NioEventLoop 的 run 方法，执行 I/O 任务和非 I/O 任务：

I/O 任务，即 selectionKey 中 ready 的事件，如 accept、connect、read、write 等，由 processSelectedKeys 方法触发。

非 IO 任务，添加到 taskQueue 中的任务，如 register0、bind0 等任务，由 runAllTasks 方法触发。

**【NioEventLoopGroup】：**

NioEventLoopGroup，主要管理 eventLoop 的生命周期，可以理解为一个线程池，内部维护了一组线程，每个线程(NioEventLoop)负责处理多个 Channel 上的事件，而一个 Channel 只对应于一个线程。

**【ChannelHandler】：**

ChannelHandler 是一个接口，处理 I/O 事件或拦截 I/O 操作，并将其转发到其 ChannelPipeline(业务处理链)中的下一个处理程序。

ChannelHandler 本身并没有提供很多方法，因为这个接口有许多的方法需要实现，方便使用期间，可以继承它的子类：

ChannelInboundHandler 用于处理入站 I/O 事件。 ChannelOutboundHandler 用于处理出站 I/O 操作。              

或者使用以下适配器类：

ChannelInboundHandlerAdapter 用于处理入站 I/O 事件。 ChannelOutboundHandlerAdapter 用于处理出站 I/O 操作。              

**【ChannelHandlerContext】：**

保存 Channel 相关的所有上下文信息，同时关联一个 ChannelHandler 对象。

**【ChannelPipline】：**

保存 ChannelHandler 的 List，用于处理或拦截 Channel 的入站事件和出站操作。

ChannelPipeline 实现了一种高级形式的拦截过滤器模式，使用户可以完全控制事件的处理方式，以及 Channel 中各个的 ChannelHandler 如何相互交互。

在 Netty 中每个 Channel 都有且仅有一个 ChannelPipeline 与之对应，它们的组成关系如下： 

​    ![0](https://note.youdao.com/yws/public/resource/bbc5cfef81b2951d769807ed748343b9/xmlnote/324C604C8D5241E7A415FF19FCF9F91F/84844)

一个 Channel 包含了一个 ChannelPipeline，而 ChannelPipeline 中又维护了一个由 ChannelHandlerContext 组成的双向链表，并且每个 ChannelHandlerContext 中又关联着一个 ChannelHandler。

read事件(入站事件)和write事件(出站事件)在一个双向链表中，入站事件会从链表 head 往后传递到最后一个入站的 handler，出站事件会从链表 tail 往前传递到最前一个出站的 handler，两种类型的 handler 互不干扰。

## 6、ByteBuf详解

从结构上来说，ByteBuf 由一串字节数组构成。数组中每个字节用来存放信息。

ByteBuf 提供了两个索引，一个用于读取数据，一个用于写入数据。这两个索引通过在字节数组中移动，来定位需要读或者写信息的位置。

当从 ByteBuf 读取时，它的 readerIndex（读索引）将会根据读取的字节数递增。

同样，当写 ByteBuf 时，它的 writerIndex 也会根据写入的字节数进行递增。

![https://note.youdao.com/yws/public/resource/bbc5cfef81b2951d769807ed748343b9/xmlnote/200440C3D0954D23A44191AD0A73FDA0/84872](https://note.youdao.com/yws/public/resource/bbc5cfef81b2951d769807ed748343b9/xmlnote/200440C3D0954D23A44191AD0A73FDA0/84872)

需要注意的是极限的情况是 readerIndex 刚好读到了 writerIndex 写入的地方。

如果 readerIndex 超过了 writerIndex 的时候，Netty 会抛出 IndexOutOf-BoundsException 异常。

示例代码：

```java
package com.haibin.netty.io.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

/**
 * ByteBuf实践demo
 * @author shb
 */
public class NettyByteBuf {

    public static void main(String[] args){
        //创建byteBuf对象，该对象内部包含一个字节数组byte[10]
        //通过readerindex和writeIndex和capacity，将buffer分成三个区域
        //已经读取的区域：[0,readerindex)
        //可读取的区域：[readerindex,writeIndex)
        //可写的区域：[writerIndex,capacity)
        ByteBuf byteBuf = Unpooled.buffer(10);
        System.out.println("byteBuf=" + byteBuf);

        for (int i = 0; i < 8; i++){
            byteBuf.writeByte(i);
        }
        System.out.println("byteBuf=" + byteBuf);

        for (int i = 0; i < 5; i++){
            System.out.println(byteBuf.getByte(i));
        }

        System.out.println("byteBuf=" + byteBuf);

        for (int i = 0; i < 5; i++){
            System.out.println(byteBuf.readByte());
        }

        System.out.println("byteBuf=" + byteBuf);

        //用Unpooled工具类创建ByteBuf
        ByteBuf byteBuf2 = Unpooled.copiedBuffer("hello,shenhaibin!", CharsetUtil.UTF_8);
        //使用相关的方法
        if (byteBuf2.hasArray()){
            byte[] content = byteBuf2.array();
            //将content转成字符串
            System.out.println(new String(content,CharsetUtil.UTF_8));
            System.out.println("byteBuf2="+byteBuf2);

            System.out.println(byteBuf2.getByte(0));        //获取数组0这个位置的字符h的ascii码，h=104
            int len = byteBuf2.readableBytes();                 //可读的字节数 12
            System.out.println("len=" + len);
            //使用for取出各个字节
            for (int i = 0; i < len; i++){
                System.out.println((char)byteBuf2.getByte(i));
            }
            //范围读取
            System.out.println(byteBuf2.getCharSequence(0,6,CharsetUtil.UTF_8));
            System.out.println(byteBuf2.getCharSequence(6,6,CharsetUtil.UTF_8));
        }
    }

}
```

## 7、handler的生命周期回调接口调用顺序

```java
package com.haibin.netty.io.netty.reconnect;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 *  handler的生命周期回调接口调用顺序:
 *  handlerAdded -> channelRegistered -> channelActive -> channelRead -> channelReadComplete
 *  -> channelInactive -> channelUnRegistered -> handlerRemoved
 *
 * handlerAdded: 新建立的连接会按照初始化策略，把handler添加到该channel的pipeline里面，也就是channel.pipeline.addLast(new LifeCycleInBoundHandler)执行完成后的回调；
 * channelRegistered: 当该连接分配到具体的worker线程后，该回调会被调用。
 * channelActive：channel的准备工作已经完成，所有的pipeline添加完成，并分配到具体的线上上，说明该channel准备就绪，可以使用了。
 * channelRead：客户端向服务端发来数据，每次都会回调此方法，表示有数据可读；
 * channelReadComplete：服务端每次读完一次完整的数据之后，回调该方法，表示数据读取完毕；
 * channelInactive：当连接断开时，该回调会被调用，说明这时候底层的TCP连接已经被断开了。
 * channelUnRegistered: 对应channelRegistered，当连接关闭后，释放绑定的workder线程；
 * handlerRemoved： 对应handlerAdded，将handler从该channel的pipeline移除后的回调方法。
 */
public class LifeCycleInBoundHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRegistered(ChannelHandlerContext ctx)
            throws Exception {
        System.out.println("channelRegistered: channel注册到NioEventLoop");
        super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx)
            throws Exception {
        System.out.println("channelUnregistered: channel取消和NioEventLoop的绑定");
        super.channelUnregistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx)
            throws Exception {
        System.out.println("channelActive: channel准备就绪");
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx)
            throws Exception {
        System.out.println("channelInactive: channel被关闭");
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        System.out.println("channelRead: channel中有可读的数据" );
        super.channelRead(ctx, msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx)
            throws Exception {
        System.out.println("channelReadComplete: channel读数据完成");
        super.channelReadComplete(ctx);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx)
            throws Exception {
        System.out.println("handlerAdded: handler被添加到channel的pipeline");
        super.handlerAdded(ctx);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx)
            throws Exception {
        System.out.println("handlerRemoved: handler从channel的pipeline中移除");
        super.handlerRemoved(ctx);
    }

}

Connected to the target VM, address: '127.0.0.1:61178', transport: 'socket'
netty server start。。
handlerAdded: handler被添加到channel的pipeline
channelRegistered: channel注册到NioEventLoop
channelActive: channel准备就绪
channelRead: channel中有可读的数据
服务器读取线程 nioEventLoopGroup-3-1
客户端发送消息是:HelloServer
channelReadComplete: channel读数据完成
Disconnected from the target VM, address: '127.0.0.1:61178', transport: 'socket'
channelReadComplete: channel读数据完成
channelInactive: channel被关闭
channelUnregistered: channel取消和NioEventLoop的绑定
handlerRemoved: handler从channel的pipeline中移除

Process finished with exit code 130
```

# 三、Netty实战聊天室系统

像我们平时使用最多的QQ、微信、飞书等都是聊天室

服务端：

```java
package com.haibin.netty.io.netty.chat;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class ChatServer {

    public static void main(String[] args) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workGroup = new NioEventLoopGroup(8);
        try{
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup,workGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG,1024)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            //加入特殊分隔符分包解码器
                            //pipeline.addLast(new DelimiterBasedFrameDecoder(1024, Unpooled.copiedBuffer("_"
                            // .getBytes())));
                            ChannelPipeline pipeline = ch.pipeline();
                            //向pipeline加入解码器
                            pipeline.addLast("decoder",new StringDecoder());
                            //向pipeline加入编码器
                            pipeline.addLast("encoder",new StringEncoder());
                            //加入自己的业务处理handler
                            pipeline.addLast(new ChatServerHandler());
                        }
                    });
            System.out.println("聊天室server启动。。");
            ChannelFuture channelFuture = bootstrap.bind(8888).sync();
            //关闭通道
            channelFuture.channel().closeFuture().sync();
        }finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }
}

package com.haibin.netty.io.netty.chat;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.text.SimpleDateFormat;

public class ChatServerHandler extends SimpleChannelInboundHandler<String> {

    //GlobalEventExecutor.INSTANCE是全局的事件执行器，是一个单例
    private static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        //将该客户加入聊天的信息推送给其它在线的客户端
        //该方法会将channelGroup中所有的channel遍历，并发送消息
        channelGroup.writeAndFlush("[ 客户端 ]" + channel.remoteAddress() + " 上线了 " + sdf.format(new
                java.util.Date()) + "\n");
        //将当前channel加入到channelGroup
        channelGroup.add(channel);
        System.out.println(ctx.channel().remoteAddress() + " 上线了" + "\n");
    }

    //表示channel处于不活动状态，提示离线了
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        //将客户离开信息推送给当前在线的客户
        channelGroup.writeAndFlush("[ 客户端 ]" + channel.remoteAddress() + " 下线了" + "\n");
        System.out.println(ctx.channel().remoteAddress() + "下线了" + "\n");
        System.out.println("channelGroup size=" + channelGroup.size());
    }

    //读取数据
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        //获取到当前channel
        Channel channel = ctx.channel();
        //这时我们遍历channelGroup,根据不同的情况，回送不同的信息
        channelGroup.forEach((ch) -> {
            if (channel != ch){ //不是当前的 channel,转发消息
                ch.writeAndFlush("[ 客户端 ]" + channel.remoteAddress() + " 发送了消息：" + msg + "\n");
            }else{  //回显自己发送的消息给自己
                ch.writeAndFlush("[ 自己 ]发送了消息：" + msg + "\n");
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //关闭通道
        ctx.close();
    }
}
```

客户端：

```java
package com.haibin.netty.io.netty.chat;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.Scanner;

public class ChatClient {

    public static void main(String[] args) throws Exception{
        EventLoopGroup group = new NioEventLoopGroup();
        try{
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            //向pipeline加入解码器
                            pipeline.addLast("decoder",new StringDecoder());
                            //向pipeline加入编码器
                            pipeline.addLast("encoder",new StringEncoder());
                            //加入自己的业务处理handler
                            pipeline.addLast(new ChatClientHanlder());
                        }
                    });
            ChannelFuture channelFuture = bootstrap.connect("127.0.0.1",8888).sync();
            //得到channel
            Channel channel = channelFuture.channel();
            System.out.println("========" + channel.localAddress() + "========");
            //客户端需要输入信息， 创建一个扫描器
            Scanner scanner = new Scanner(System.in);
            while(scanner.hasNextLine()){
                String msg = scanner.nextLine();
                //通过channel 发送到服务器
                channel.writeAndFlush(msg);
            }
        }finally {
            group.shutdownGracefully();
        }
    }
}

package com.haibin.netty.io.netty.chat;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ChatClientHanlder extends SimpleChannelInboundHandler<String> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg){
        System.out.println(msg.trim());
    }
}
```

# 四、核心ChannelHandler详解及实践

## 1、Netty编解码

Netty涉及到编解码的组件有Channel、ChannelHandler、ChannelPipe等，先大概了解下这几个组件的作用。

**ChannelHandler**

ChannelHandler充当了处理入站和出站数据的应用程序逻辑容器。例如，实现ChannelInboundHandler接口（或ChannelInboundHandlerAdapter），你就可以接收入站事件和数据，这些数据随后会被你的应用程序的业务逻辑处理。当你要给连接的客户端发送响应时，也可以从ChannelInboundHandler冲刷数据。你的业务逻辑通常写在一个或者多个ChannelInboundHandler中。ChannelOutboundHandler原理一样，只不过它是用来处理出站数据的。

**ChannelPipeline**

ChannelPipeline提供了ChannelHandler链的容器。**以客户端应用程序为例**，如果事件的运动方向是从客户端到服务端的，那么我们称这些事件为**出站的**，即客户端发送给服务端的数据会通过pipeline中的一系列**ChannelOutboundHandler(ChannelOutboundHandler调用是****从tail到head方向逐个调用每个handler的逻辑)**，并被这些Handler处理，反之则称为**入站的，**入站只调用pipeline里的**ChannelInboundHandler**逻辑**(ChannelInboundHandler调用是从head到tail方向逐个调用每个handler的逻辑)。

当你通过Netty发送或者接受一个消息的时候，就将会发生一次数据转换。入站消息会被**解码**：从字节转换为另一种格式（比如java对象）；如果是出站消息，它会被**编码成字节**。

Netty提供了一系列实用的编码解码器，他们都实现了ChannelInboundHadnler或者ChannelOutboundHandler接口。在这些类中，channelRead方法已经被重写了。以入站为例，对于每个从入站Channel读取的消息，这个方法会被调用。随后，它将调用由已知解码器所提供的decode()方法进行解码，并将已经解码的字节转发给ChannelPipeline中的下一个ChannelInboundHandler。

Netty提供了很多编解码器，比如编解码字符串的StringEncoder和StringDecoder，编解码对象的ObjectEncoder和ObjectDecoder等。

如果要实现高效的编解码可以用protobuf，但是protobuf需要维护大量的proto文件比较麻烦，现在一般可以使用protostuff。

protostuff是一个基于protobuf实现的序列化方法，它较于protobuf最明显的好处是，在几乎不损耗性能的情况下做到了不用我们写.proto文件来实现序列化。使用它也非常简单，代码如下：

引入依赖：

```xml
<dependency>
    <groupId>com.dyuproject.protostuff</groupId>
    <artifactId>protostuff-api</artifactId>
    <version>1.0.10</version>
</dependency>
<dependency>
    <groupId>com.dyuproject.protostuff</groupId>
    <artifactId>protostuff-core</artifactId>
    <version>1.0.10</version>
</dependency>
<dependency>
    <groupId>com.dyuproject.protostuff</groupId>
    <artifactId>protostuff-runtime</artifactId>
    <version>1.0.10</version>
</dependency>
```

客户端编码：

```java
package com.haibin.netty.io.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("MyClientHandler发送数据");
        ByteBuf buf = Unpooled.copiedBuffer(ProtostuffUtil.serializer(new User(1,"haibin")));
        ctx.writeAndFlush(buf);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("收到服务器消息：" + msg);
    }
}
```

服务端解码：

```java
package com.haibin.netty.io.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        System.out.println("从客户端读取到Object: " + ProtostuffUtil.deserializer(bytes,User.class));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
```

## 2、Netty粘包拆包

TCP是一个流协议，就是没有界限的一长串二进制数据。TCP作为传输层协议并不不了解上层业务数据的具体含义，它会根据TCP缓冲区的实际情况进行数据包的划分，所以在业务上认为是一个完整的包，可能会被TCP拆分成多个包进行发送，也有可能把多个小的包封装成一个大的数据包发送，这就是所谓的TCP粘包和拆包问题。面向流的通信是无消息保护边界的。

如下图所示，client发了两个数据包D1和D2，但是server端可能会收到如下几种情况的数据。

![https://note.youdao.com/yws/public/resource/b8970e44473486a48178193d68929008/xmlnote/E8B058FB337E43F2B423EE497B9F11DF/84598](https://note.youdao.com/yws/public/resource/b8970e44473486a48178193d68929008/xmlnote/E8B058FB337E43F2B423EE497B9F11DF/84598)

解决方案：

1）消息定长度，传输的数据大小固定长度，例如每段的长度固定为100字节，如果不够空位补空格

2）在数据包尾部添加特殊分隔符，比如下划线，中划线等，这种方法简单易行，但选择分隔符的时候一定要注意每条数据的内部一定不能出现分隔符。

3）发送长度：发送每条数据的时候，将数据的长度一并发送，比如可以选择每条数据的前4位是数据的长度，应用层处理时可以根据长度来判断每条数据的开始和结束。

Netty提供了多个解码器，可以进行分包的操作，如下：

- LineBasedFrameDecoder （回车换行分包）
- DelimiterBasedFrameDecoder（特殊分隔符分包）
- FixedLengthFrameDecoder（固定长度报文来分包）

## 3、Netty心跳检测机制

所谓心跳, 即在 TCP 长连接中, 客户端和服务器之间定期发送的一种特殊的数据包, 通知对方自己还在线, 以确保 TCP 连接的有效性.在 Netty 中, 实现心跳机制的关键是 IdleStateHandler, 看下它的构造器：

```java
public IdleStateHandler(int readerIdleTimeSeconds, int writerIdleTimeSeconds, int allIdleTimeSeconds) {
    this((long)readerIdleTimeSeconds, (long)writerIdleTimeSeconds, (long)allIdleTimeSeconds, TimeUnit.SECONDS);
}
```

这里解释下三个参数的含义：

- readerIdleTimeSeconds: 读超时. 即当在指定的时间间隔内没有从 Channel 读取到数据时, 会触发一个 READER_IDLE 的 IdleStateEvent 事件.
- writerIdleTimeSeconds: 写超时. 即当在指定的时间间隔内没有数据写入到 Channel 时, 会触发一个 WRITER_IDLE 的 IdleStateEvent 事件.
- allIdleTimeSeconds: 读/写超时. 即当在指定的时间间隔内没有读或写操作时, 会触发一个 ALL_IDLE 的 IdleStateEvent 事件.

注：这三个参数默认的时间单位是秒。若需要指定其他时间单位，可以使用另一个构造方法：

```java
public IdleStateHandler(long readerIdleTime, long writerIdleTime, long allIdleTime, TimeUnit unit) {
    this(false, readerIdleTime, writerIdleTime, allIdleTime, unit);
}
```

要实现Netty服务端心跳检测机制需要在服务器端的ChannelInitializer中加入如下的代码：

 pipeline.addLast(new IdleStateHandler(3, 0, 0, TimeUnit.SECONDS));       

初步地看下IdleStateHandler源码，先看下IdleStateHandler中的channelRead方法：

​    ![0](https://note.youdao.com/yws/public/resource/b8970e44473486a48178193d68929008/xmlnote/8765340B14D94966AFA3C61295D545FE/85352)

红框代码其实表示该方法只是进行了透传，不做任何业务逻辑处理，让channelPipe中的下一个handler处理channelRead方法

我们再看看channelActive方法：

​    ![0](https://note.youdao.com/yws/public/resource/b8970e44473486a48178193d68929008/xmlnote/EB8465AFA2554F82AF2799FD1147DF5B/85357)

这里有个initialize的方法，这是IdleStateHandler的精髓，接着探究：

​    ![0](https://note.youdao.com/yws/public/resource/b8970e44473486a48178193d68929008/xmlnote/C59F37EF4E2840C69869A668E8B59855/85360)

这边会触发一个Task，ReaderIdleTimeoutTask，这个task里的run方法源码是这样的：

​    ![0](https://note.youdao.com/yws/public/resource/b8970e44473486a48178193d68929008/xmlnote/5E106E0DFED445B698646185136FD0AA/85374)

第一个红框代码是用当前时间减去最后一次channelRead方法调用的时间，假如这个结果是6s，说明最后一次调用channelRead已经是6s之前的事情了，你设置的是5s，那么nextDelay则为-1，说明超时了，那么第二个红框代码则会触发下一个handler的userEventTriggered方法：

​    ![0](https://note.youdao.com/yws/public/resource/b8970e44473486a48178193d68929008/xmlnote/3C45761D9C1D407B95569C1A6B3382D4/85370)

如果没有超时则不触发userEventTriggered方法。       

服务端代码：

```java
package com.haibin.netty.io.netty.heartbeat;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

public class HeartBeatServer {

    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workGroup = new NioEventLoopGroup();
        try{
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup,workGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline channelPipeline = socketChannel.pipeline();
                            channelPipeline.addLast("decoder",new StringDecoder());
                            channelPipeline.addLast("encoder",new StringEncoder());
                            channelPipeline.addLast(new IdleStateHandler(3,0,0, TimeUnit.SECONDS));
                            channelPipeline.addLast(new HeartBeatServerHandler());
                        }
                    });
            System.out.println("netty server start。。");
            ChannelFuture channelFuture = serverBootstrap.bind(8888).sync();
            channelFuture.channel().closeFuture().sync();
        }finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }
}
```

```java
package com.haibin.netty.io.netty.heartbeat;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;

public class HeartBeatServerHandler extends SimpleChannelInboundHandler<String> {

    int readIdleTimes = 0;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception {
        System.out.println(" ====== > [server] message received : " + s);
        if ("Heartbeat Packet".equals(s)){
            ctx.channel().writeAndFlush("ok");
        }else{
            System.out.println(" 其他信息处理...");
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        IdleStateEvent event = (IdleStateEvent) evt;
        String eventType = null;
        switch (event.state()){
            case READER_IDLE:
                eventType = "读空闲";
                readIdleTimes++; // 读空闲的计数加1
                break;
            case WRITER_IDLE:
                eventType = "写空闲";
                // 不处理
                break;
            case ALL_IDLE:
                eventType = "读写空闲";
                // 不处理
                break;
        }
        System.out.println(ctx.channel().remoteAddress() + "超时事件：" + eventType);
        if (readIdleTimes > 3){
            System.out.println(" [server]读空闲超过3次，关闭连接，释放更多资源");
            ctx.channel().writeAndFlush("idle close");
            ctx.channel().close();
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.err.println("=== " + ctx.channel().remoteAddress() + " is active ===");
    }

}
```

客户端代码：

```java
package com.haibin.netty.io.netty.heartbeat;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.Random;

public class HeartBeatClient {

    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try{
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline channelPipeline = socketChannel.pipeline();
                            channelPipeline.addLast("decoder",new StringDecoder());
                            channelPipeline.addLast("encoder",new StringEncoder());
                            channelPipeline.addLast(new HeartBeatClientHandler());
                        }
                    });
            System.out.println("netty client start。。");
            Channel channel = bootstrap.connect("127.0.0.1", 8888).sync().channel();
            String text = "Heartbeat Packet";
            Random random = new Random();
            while(channel.isActive()){
                int num = random.nextInt(8);
                Thread.sleep(num*1000);
                channel.writeAndFlush(text);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            group.shutdownGracefully();
        }
    }

    static class HeartBeatClientHandler extends SimpleChannelInboundHandler<String>{
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
            System.out.println(" client received :" + msg);
            if (msg != null && msg.equals("idle close")) {
                System.out.println(" 服务端关闭连接，客户端也关闭");
                ctx.channel().closeFuture();
            }
        }
    }

}
```

## 4、Netty断线自动重连实现

1)、客户端启动连接服务端时，如果网络或服务端有问题，客户端连接失败，可以重连，重连的逻辑加在客户端。

2)、系统运行过程中网络故障或服务端故障，导致客户端与服务端断开连接了也需要重连，可以在客户端处理数据的Handler的channelInactive方法中进行重连。

```java
package com.haibin.netty.io.netty.reconnect;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.concurrent.TimeUnit;

public class NettyClient {

    private String host;
    private int port;
    private Bootstrap bootstrap;
    private EventLoopGroup group;

    public static void main(String[] args) throws Exception {
        NettyClient nettyClient = new NettyClient("localhost", 9000);
        nettyClient.connect();
    }

    public NettyClient(String host, int port) {
        this.host = host;
        this.port = port;
        init();
    }

    private void init() {
        //客户端需要一个事件循环组
        group = new NioEventLoopGroup();
        //创建客户端启动对象
        // bootstrap 可重用, 只需在NettyClient实例化的时候初始化即可.
        bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        //加入处理器
                        ch.pipeline().addLast(new NettyClientHandler(NettyClient.this));
                    }
                });
    }

    public void connect() throws Exception {
        System.out.println("netty client start。。");
        //启动客户端去连接服务器端
        ChannelFuture cf = bootstrap.connect(host, port);
        cf.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    //重连交给后端线程执行
                    future.channel().eventLoop().schedule(() -> {
                        System.err.println("重连服务端...");
                        try {
                            connect();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }, 3000, TimeUnit.MILLISECONDS);
                } else {
                    System.out.println("服务端连接成功...");
                }
            }
        });
        //对通道关闭进行监听
        cf.channel().closeFuture().sync();
    }

}
```

```java
package com.haibin.netty.io.netty.reconnect;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

public class NettyClientHandler extends ChannelInboundHandlerAdapter {
    private NettyClient nettyClient;

    public NettyClientHandler(NettyClient nettyClient) {
        this.nettyClient = nettyClient;
    }

    /**
     * 当客户端连接服务器完成就会触发该方法
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ByteBuf buf = Unpooled.copiedBuffer("HelloServer".getBytes(CharsetUtil.UTF_8));
        ctx.writeAndFlush(buf);
    }

    //当通道有读取事件时会触发，即服务端发送数据给客户端
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        System.out.println("收到服务端的消息:" + buf.toString(CharsetUtil.UTF_8));
        System.out.println("服务端的地址： " + ctx.channel().remoteAddress());
    }

    // channel 处于不活动状态时调用
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.err.println("运行中断开重连。。。");
        nettyClient.connect();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

}
```

# 五、深入了解Netty核心线程模型和底层数据交互源码

## 1、NioEventLoopGroup

```java
EventLoopGroup bossGroup = new NioEventLoopGroup(1);
```

初始化的时候如果有传线程数量的话以传的为主，如果没有传含有的子线程NioEventLoop的个数默认为cpu核数的两倍

```java
private static final int DEFAULT_EVENT_LOOP_THREADS = Math.max(1, SystemPropertyUtil.getInt("io.netty.eventLoopThreads", NettyRuntime.availableProcessors() * 2));

protected MultithreadEventLoopGroup(int nThreads, Executor executor, Object... args) {
    super(nThreads == 0 ? DEFAULT_EVENT_LOOP_THREADS : nThreads, executor, args);
}
```

这里的children是NioEventLoopGroup的一个成员变量，通过for循环一个个赋值，

```java
this.children = new EventExecutor[nThreads];
this.children[i] = this.newChild((Executor)executor, args);
```

所以跟上面介绍的类似，一个NioEventLoopGroup里面有多个NioEventLoop

```
NioEventLoopGroup.class
protected EventLoop newChild(Executor executor, Object... args) throws Exception {
    return new NioEventLoop(this, executor, (SelectorProvider)args[0], ((SelectStrategyFactory)args[1]).newSelectStrategy(), (RejectedExecutionHandler)args[2]);
}
```

一个NioEventLoop里面有Selector和TaskQueue

```java
SingleThreadEventExecutor.class
this.taskQueue = this.newTaskQueue(this.maxPendingTasks);
protected Queue<Runnable> newTaskQueue(int maxPendingTasks) {
        return new LinkedBlockingQueue(maxPendingTasks);
}

NioEventLoop.class
 this.provider = selectorProvider;
            NioEventLoop.SelectorTuple selectorTuple = this.openSelector();//调用nio方法
            this.selector = selectorTuple.selector;
```

## 2、ServerBootstrap

```java
//创建服务器端的启动对象
ServerBootstrap bootstrap = new ServerBootstrap();
//使用链式编程来配置参数
bootstrap.group(bossGroup,workerGroup)  //设置两个线程组
        .channel(NioServerSocketChannel.class)      //使用NioServerSocketChannel作为服务器的通道实现
        //初始化服务器连接队列大小，服务器处理客户端连接请求是顺序处理的，所以同一时间只能处理一个客户端连接。
        //多个客户端同时来的时候，服务端将不能处理的客户端连接请求放在队列中等待处理
        .option(ChannelOption.SO_BACKLOG,1024)
        .childHandler(new ChannelInitializer<SocketChannel>() {     //创建通道初始化对象，设置初始化参数
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                //对workGroup的SocketChannel设置处理器
                socketChannel.pipeline().addLast(new NettyServerHandler());
            }
        });
```

上面本质就是通过链式编程的形式给ServerBootstrap的各个成员变量赋初值，以便在后续使用。

## 3、NioServerSocketChannel

NioServerSocketChannel初始化主要返回ServerSocketChannel，这步也是对nio的封装

```
NioServerSocketChannel.class
private static java.nio.channels.ServerSocketChannel newSocket(SelectorProvider provider) {
        try {
        	//ServerSocketChannel serverSocket = ServerSocketChannel.open();
            return provider.openServerSocketChannel();
        } catch (IOException var2) {
            throw new ChannelException("Failed to open a server socket.", var2);
        }
  }
    
public NioServerSocketChannel() {
    this(newSocket(DEFAULT_SELECTOR_PROVIDER));
}

```

同时也执行this.pipeline = this.newChannelPipeline();为该成员变量pipeline赋初始值,通过pipeline将各个handler串连起来，这里只是初始化tail和head,后续调用的时候会使用到。

```java
protected DefaultChannelPipeline(Channel channel) {
    this.channel = (Channel)ObjectUtil.checkNotNull(channel, "channel");
    this.succeededFuture = new SucceededChannelFuture(channel, (EventExecutor)null);
    this.voidPromise = new VoidChannelPromise(channel, true);
    this.tail = new DefaultChannelPipeline.TailContext(this);
    this.head = new DefaultChannelPipeline.HeadContext(this);
    this.head.next = this.tail;
    this.tail.prev = this.head;
}
```

同时也将该channel设置为非阻塞和接收连接，也是对nio的封装

```
protected AbstractNioChannel(Channel parent, SelectableChannel ch, int readInterestOp) {
    super(parent);
    this.ch = ch;
    //readInterestOp传进来是16也就是SelectionKey.OP_ACCEPT，这里只是普通的赋值，还没进行绑定
    this.readInterestOp = readInterestOp;

    try {
    	//serverSocket.configureBlocking(false);
        ch.configureBlocking(false);
    } catch (IOException var7) {
        try {
            ch.close();
        } catch (IOException var6) {
            if (logger.isWarnEnabled()) {
                logger.warn("Failed to close a partially initialized socket.", var6);
            }
        }

        throw new ChannelException("Failed to enter non-blocking mode.", var7);
    }
}
```

## 4、源码剖析图：

![https://note.youdao.com/yws/public/resource/ab45dc97644411c44fbd27ee95d8244e/xmlnote/27ABFFC5A76E455281D97034FD4F191D/85410](https://note.youdao.com/yws/public/resource/ab45dc97644411c44fbd27ee95d8244e/xmlnote/27ABFFC5A76E455281D97034FD4F191D/85410)

## 5、线程模型总结：

![https://note.youdao.com/yws/public/resource/ab45dc97644411c44fbd27ee95d8244e/xmlnote/96E69603C44742FABC0E164EA395D49B/85408](https://note.youdao.com/yws/public/resource/ab45dc97644411c44fbd27ee95d8244e/xmlnote/96E69603C44742FABC0E164EA395D49B/85408)

# 六、Netty高并发高性能架构设计精髓

## 1、主从Reactor线程模型

[![4OmRCn.md.png](https://z3.ax1x.com/2021/10/04/4OmRCn.md.png)](https://imgtu.com/i/4OmRCn)

Reactor模式 是一种「事件驱动」模式。

「Reactor线程模型」就是通过 单个线程 使用Java NIO包中的Selector的select()方法，进行监听。当获取到事件(如accept、read等)后，就会分配(dispatch)事件进行相应的事件处理(handle)。

如果要给 Reactor线程模型 下一个更明确的定义，应该是：

- Reactor线程模式 = Reactor(I/O多路复用)+ 线程池

其中Reactor负责监听和分配事件，线程池负责处理事件。

1)主Reactor

主 Reactor 单独监听server socket，accept新连接，然后将建立的 SocketChannel 注册给指定的 从Reactor，

2)从Reactor

从Reactor 将连接加入到连接队列进行监听，并创建handler进行事件处理。执行事件的读写、分发，把业务处理就扔给worker线程池完成。

3)Worker线程池

处理所有业务事件，充分利用多核机器的资源，提高性能。

## 2、NIO多路复用非阻塞

多路：实际指的就是多个不同的tcp连接
复用：一个线程可以维护多个不同的io操作
好处： 占用cpu资源非常小、保证线程安全问题

## 3、无锁串行化设计思想

在大多数场景下，并行多线程处理可以提升系统的并发性能。但是，如果对于共享资源的并发访问处理不当，会带来严重的锁竞争，这最终会导致性能的下降。为了尽可能的避免锁竞争带来的性能损耗，可以通过串行化设计，即消息的处理尽可能在同一个线程内完成，期间不进行线程切换，这样就避免了多线程竞争和同步锁。NIO的多路复用就是一种无锁串行化的设计思想(理解下Redis和Netty的线程模型)，一个线程处理所有的连接并且顺序的循环每一个读请求并进行处理。

为了尽可能提升性能，Netty采用了串行无锁化设计，在IO线程内部进行串行操作，避免多线程竞争导致的性能下降。表面上看，串行化设计似乎CPU利用率不高，并发程度不够。但是，通过调整NIO线程池的线程参数，可以同时启动多个串行化的线程并行运行，这种局部无锁化的串行线程设计相比一个队列-多个工作线程模型性能更优。

Netty的NioEventLoop读取到消息之后，直接调用ChannelPipeline的fireChannelRead(Object msg)，只要用户不主动切换线程，一直会由NioEventLoop调用到用户的Handler，期间不进行线程切换，这种串行化处理方式避免了多线程操作导致的锁的竞争，从性能角度看是最优的。

写netty程序本质是写一个个的handler,因为其它模板netty都帮我们定义好了，由于每一个handler都有先后顺序，所以我们需要保证先后执行。

## 4、支持高性能序列化协议

参考前面介绍的netty编解码

## 5、零拷贝（直接内存使用）

直接内存：

接内存（Direct Memory）并不是虚拟机运行时数据区的一部分，也不是Java虚拟机规范中定义的内存区域，某些情况下这部分内存也会被频繁地使用，而且也可能导致OutOfMemoryError异常出现。Java里用DirectByteBuffer可以分配一块直接内存(堆外内存)，元空间对应的内存也叫作直接内存，它们对应的都是机器的物理内存。

![https://note.youdao.com/yws/public/resource/ab45dc97644411c44fbd27ee95d8244e/xmlnote/701F773CDA8B496EB7D7689C55D027F6/107328](https://note.youdao.com/yws/public/resource/ab45dc97644411c44fbd27ee95d8244e/xmlnote/701F773CDA8B496EB7D7689C55D027F6/107328)

```java
package com.haibin.netty.io.netty;

import java.nio.ByteBuffer;

/**
 * 直接内存与堆内存的区别
 */
public class DirectMemoryTest {

    public static void heapAccess(){
        long startTime = System.currentTimeMillis();
        //分配堆内存
        ByteBuffer buffer = ByteBuffer.allocate(1000);
        for (int i = 0; i < 100000; i++){
            for (int j = 0; j < 200; j++){
                buffer.putInt(j);
            }
            buffer.flip();
            for (int j = 0; j < 200; j++){
                buffer.getInt();
            }
            buffer.clear();
        }
        long endTime = System.currentTimeMillis();
        System.out.println("堆内存访问:" + (endTime - startTime) + "ms");
    }

    public static void directAccess() {
        long startTime = System.currentTimeMillis();
        //分配直接内存
        ByteBuffer buffer = ByteBuffer.allocateDirect(1000);
        for (int i = 0; i < 100000; i++){
            for (int j = 0; j < 200; j++){
                buffer.putInt(j);
            }
            buffer.flip();
            for (int j = 0; j < 200; j++){
                buffer.getInt();
            }
            buffer.clear();
        }
        long endTime = System.currentTimeMillis();
        System.out.println("直接内存访问:" + (endTime - startTime) + "ms");
    }

    public static void heapAllocate(){
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++){
            ByteBuffer.allocate(100);
        }
        long endTime = System.currentTimeMillis();
        System.out.println("堆内存申请:" + (endTime - startTime) + "ms");
    }

    public static void directAllocate() {
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            ByteBuffer.allocateDirect(100);
        }
        long endTime = System.currentTimeMillis();
        System.out.println("直接内存申请:" + (endTime - startTime) + "ms");
    }

    public static void main(String[] args){
        for (int i = 0; i < 10; i++) {
            heapAccess();
            directAccess();
        }

        System.out.println();

        for (int i = 0; i < 10; i++) {
            heapAllocate();
            directAllocate();
        }
    }
}
堆内存访问:41ms
直接内存访问:26ms
堆内存访问:25ms
直接内存访问:16ms
堆内存访问:43ms
直接内存访问:31ms
堆内存访问:26ms
直接内存访问:16ms
堆内存访问:28ms
直接内存访问:16ms
堆内存访问:27ms
直接内存访问:14ms
堆内存访问:29ms
直接内存访问:14ms
堆内存访问:30ms
直接内存访问:14ms
堆内存访问:27ms
直接内存访问:15ms
堆内存访问:27ms
直接内存访问:14ms

堆内存申请:9ms
直接内存申请:24ms
堆内存申请:6ms
直接内存申请:19ms
堆内存申请:81ms
直接内存申请:31ms
堆内存申请:1ms
直接内存申请:15ms
堆内存申请:2ms
直接内存申请:50ms
堆内存申请:2ms
直接内存申请:20ms
堆内存申请:1ms
直接内存申请:17ms
堆内存申请:2ms
直接内存申请:21ms
堆内存申请:5ms
直接内存申请:23ms
堆内存申请:5ms
直接内存申请:113ms

```

从程序运行结果看出直接内存申请较慢，但访问效率高。在java虚拟机实现上，本地IO一般会直接操作直接内存（直接内存=>系统调用=>硬盘/网卡），而非直接内存则需要二次拷贝（堆内存=>直接内存=>系统调用=>硬盘/网卡）。

直接内存分配源码分析：

```java
public static ByteBuffer allocateDirect(int capacity) {
    return new DirectByteBuffer(capacity);
}

DirectByteBuffer(int cap) {                   // package-private

        super(-1, 0, cap, cap);
        boolean pa = VM.isDirectMemoryPageAligned();
        int ps = Bits.pageSize();
        long size = Math.max(1L, (long)cap + (pa ? ps : 0));
    	//判断是否有足够的直接内存空间分配，可通过-XX:MaxDirectMemorySize=<size>参数指定直接内存最		大可分配空间，如果不指定默认为最大堆内存大小，
    	//在分配直接内存时如果发现空间不够会显示调用System.gc()触发一次full gc回收掉一部分无用的直接内		存的引用对象，同时直接内存也会被释放掉
    	//如果释放完分配空间还是不够会抛出异常java.lang.OutOfMemoryError
        Bits.reserveMemory(size, cap);

        long base = 0;
        try {
            // 调用unsafe本地方法分配直接内存
            base = unsafe.allocateMemory(size);
        } catch (OutOfMemoryError x) {
            // 分配失败，释放内存
            Bits.unreserveMemory(size, cap);
            throw x;
        }
        unsafe.setMemory(base, size, (byte) 0);
        if (pa && (base % ps != 0)) {
            // Round up to page boundary
            address = base + ps - (base & (ps - 1));
        } else {
            address = base;
        }
   		 // 使用Cleaner机制注册内存回收处理函数，当直接内存引用对象被GC清理掉时，
    	// 会提前调用这里注册的释放直接内存的Deallocator线程对象的run方法
        cleaner = Cleaner.create(this, new Deallocator(base, size, cap));
        att = null;
    }

// 申请一块本地内存。内存空间是未初始化的，其内容是无法预期的。
// 使用freeMemory释放内存，使用reallocateMemory修改内存大小
public native long allocateMemory(long var1);

```

```c++
// openjdk-8/hotspot/src/share/vm/prims/unsafe.cpp
UNSAFE_ENTRY(jlong, Unsafe_AllocateMemory(JNIEnv *env, jobject unsafe, jlong size))
  UnsafeWrapper("Unsafe_AllocateMemory");
  size_t sz = (size_t)size;
  if (sz != (julong)size || size < 0) {
    THROW_0(vmSymbols::java_lang_IllegalArgumentException());
  }
  if (sz == 0) {
    return 0;
  }
  sz = round_to(sz, HeapWordSize);
// 调用os::malloc申请内存，内部使用malloc这个C标准库的函数申请内存
  void* x = os::malloc(sz, mtInternal);
  if (x == NULL) {
    THROW_0(vmSymbols::java_lang_OutOfMemoryError());
  }
  //Copy::fill_to_words((HeapWord*)x, sz / HeapWordSize);
  return addr_to_java(x);
UNSAFE_END
```

使用直接内存的优缺点：

优点：

- 不占用堆内存空间，减少了发生GC的可能
- java虚拟机实现上，本地IO会直接操作直接内存（直接内存=>系统调用=>硬盘/网卡），而非直接内存则需要二次拷贝（堆内存=>直接内存=>系统调用=>硬盘/网卡）

缺点：

- 初始分配较慢
- 没有JVM直接帮助管理内存，容易发生内存溢出。为了避免一直没有FULL GC，最终导致直接内存把物理内存耗完。我们可以指定直接内存的最大值，通过-XX：MaxDirectMemorySize来指定，当达到阈值的时候，调用system.gc来进行一次FULL GC，间接把那些没有被使用的直接内存回收掉

Netty零拷贝

<img src="https://note.youdao.com/yws/public/resource/ab45dc97644411c44fbd27ee95d8244e/xmlnote/9C889DD8449F4254A3E2881690435DA6/106808" alt="https://note.youdao.com/yws/public/resource/ab45dc97644411c44fbd27ee95d8244e/xmlnote/9C889DD8449F4254A3E2881690435DA6/106808" style="zoom:80%;" />

案例说明：

```
/**
 * 自定义Handler需要继承netty规定好的某个HandlerAdapter(规范)
 * @author shb
 */
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * 读取客户端发送的数据
     *
     * @param ctx 上下文对象，含有通道channel,管道pipeline
     * @param msg 就是客户端发送的数据
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("服务器读取线程 " + Thread.currentThread().getName());
       // Channel channel = ctx.channel();
       // ChannelPipeline pipeline = ctx.pipeline();  //本质是一个双向链接，出站入站
        //将 msg 转成一个ByteBuf,类似NIO的ByteBuffer
        ByteBuf buf = (ByteBuf)msg;	//这里是netty帮我们封装好的直接内存
        System.out.println("客户端发送消息是：" + buf.toString(CharsetUtil.UTF_8));
        super.channelRead(ctx, msg);
    }

    /**
     * 数据读取完毕处理方法
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    	//这里是堆内存
        ByteBuf buf = Unpooled.copiedBuffer("HelloClient", CharsetUtil.UTF_8);
        ctx.writeAndFlush(buf);
    }

    /**
     * 处理异常，一般是需要关闭通道
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
```

Netty的接收和发送ByteBuf采用DIRECT BUFFERS，使用堆外**直接内存**进行Socket读写，不需要进行字节缓冲区的二次拷贝。

如果使用传统的JVM堆内存（HEAP BUFFERS）进行Socket读写，JVM会将堆内存Buffer拷贝一份到直接内存中，然后才能写入Socket中。JVM堆内存的数据是不能直接写入Socket中的。相比于堆外直接内存，消息在发送过程中多了一次缓冲区的内存拷贝。

可以看下netty的读写源码，比如read源码NioByteUnsafe.read()

```java
byteBuf = allocHandle.allocate(allocator);
public ByteBuf ioBuffer(int initialCapacity) {
    return PlatformDependent.hasUnsafe() ? this.directBuffer(initialCapacity) : this.heapBuffer(initialCapacity);
}
```

## 6、ByteBuf内存池设计

随着JVM虚拟机和JIT即时编译技术的发展，对象的分配和回收是个非常轻量级的工作。但是对于缓冲区Buffer(相当于一个内存块)，情况却稍有不同，特别是对于堆外直接内存的分配和回收，是一件耗时的操作。为了尽量重用缓冲区，Netty提供了基于ByteBuf内存池的缓冲区重用机制。需要的时候直接从池子里获取ByteBuf使用即可，使用完毕之后就重新放回到池子里去。下面我们一起看下Netty ByteBuf的实现：

​    ![0](https://note.youdao.com/yws/public/resource/ab45dc97644411c44fbd27ee95d8244e/xmlnote/D9F1BF5FB75C492FA28D1362201520E3/85205)

可以看下netty的读写源码里面用到的ByteBuf内存池，比如read源码NioByteUnsafe.read()

![0](https://note.youdao.com/yws/public/resource/ab45dc97644411c44fbd27ee95d8244e/xmlnote/ABD2AF90FE2B45EB8B0C32AC70320070/85212)

​    ![0](https://note.youdao.com/yws/public/resource/ab45dc97644411c44fbd27ee95d8244e/xmlnote/4D2C3F1CB70E4A9BB83C3C5BDA84AC41/85213)

​    ![0](https://note.youdao.com/yws/public/resource/ab45dc97644411c44fbd27ee95d8244e/xmlnote/30ADA0FBA8814081B798EDFA0E28A1C9/85221)

 继续看newDirectBuffer方法，我们发现它是一个抽象方法，由AbstractByteBufAllocator的子类负责具体实现，代码如下：

![0](https://note.youdao.com/yws/public/resource/ab45dc97644411c44fbd27ee95d8244e/xmlnote/17E15C71223B46EE89948FE709C0E256/85225)

 代码跳转到PooledByteBufAllocator的newDirectBuffer方法，从Cache中获取内存区域PoolArena，调用它的allocate方法进行内存分配：

![0](https://note.youdao.com/yws/public/resource/ab45dc97644411c44fbd27ee95d8244e/xmlnote/A09C5EDB133D400D965A00E3D5EAA614/85227)

 PoolArena的allocate方法如下：

![0](https://note.youdao.com/yws/public/resource/ab45dc97644411c44fbd27ee95d8244e/xmlnote/A570C8160A8449809E61AAA2C9A67564/85229)

 我们重点分析newByteBuf的实现，它同样是个抽象方法，由子类DirectArena和HeapArena来实现不同类型的缓冲区分配

![0](https://note.youdao.com/yws/public/resource/ab45dc97644411c44fbd27ee95d8244e/xmlnote/B17CC750601A46399D7D7BEB1DF65A64/85231)

 我们这里使用的是直接内存，因此重点分析DirectArena的实现

![0](https://note.youdao.com/yws/public/resource/ab45dc97644411c44fbd27ee95d8244e/xmlnote/42D12F3F27E4461AA3287CC0EB051E07/85235)

 最终执行了PooledUnsafeDirectByteBuf的newInstance方法，代码如下：

![0](https://note.youdao.com/yws/public/resource/ab45dc97644411c44fbd27ee95d8244e/xmlnote/18D0EEAF8AF343ECB20E695D889DC60E/85240)

 通过RECYCLER的get方法循环使用ByteBuf对象，如果是非内存池实现，则直接创建一个新的ByteBuf对象。

## 7、灵活的TCP参数配置能力

合理设置TCP参数在某些场景下对于性能的提升可以起到显著的效果，例如接收缓冲区SO_RCVBUF和发送缓冲区SO_SNDBUF。如果设置不当，对性能的影响是非常大的。通常建议值为128K或者256K。

Netty在启动辅助类ChannelOption中可以灵活的配置TCP参数，满足不同的用户场景。

```java
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package io.netty.channel;

import io.netty.buffer.ByteBufAllocator;
import io.netty.util.AbstractConstant;
import io.netty.util.ConstantPool;
import java.net.InetAddress;
import java.net.NetworkInterface;

public class ChannelOption<T> extends AbstractConstant<ChannelOption<T>> {
    private static final ConstantPool<ChannelOption<Object>> pool = new ConstantPool<ChannelOption<Object>>() {
        protected ChannelOption<Object> newConstant(int id, String name) {
            return new ChannelOption(id, name);
        }
    };
    public static final ChannelOption<ByteBufAllocator> ALLOCATOR = valueOf("ALLOCATOR");
    public static final ChannelOption<RecvByteBufAllocator> RCVBUF_ALLOCATOR = valueOf("RCVBUF_ALLOCATOR");
    public static final ChannelOption<MessageSizeEstimator> MESSAGE_SIZE_ESTIMATOR = valueOf("MESSAGE_SIZE_ESTIMATOR");
    public static final ChannelOption<Integer> CONNECT_TIMEOUT_MILLIS = valueOf("CONNECT_TIMEOUT_MILLIS");
    /** @deprecated */
    @Deprecated
    public static final ChannelOption<Integer> MAX_MESSAGES_PER_READ = valueOf("MAX_MESSAGES_PER_READ");
    public static final ChannelOption<Integer> WRITE_SPIN_COUNT = valueOf("WRITE_SPIN_COUNT");
    /** @deprecated */
    @Deprecated
    public static final ChannelOption<Integer> WRITE_BUFFER_HIGH_WATER_MARK = valueOf("WRITE_BUFFER_HIGH_WATER_MARK");
    /** @deprecated */
    @Deprecated
    public static final ChannelOption<Integer> WRITE_BUFFER_LOW_WATER_MARK = valueOf("WRITE_BUFFER_LOW_WATER_MARK");
    public static final ChannelOption<WriteBufferWaterMark> WRITE_BUFFER_WATER_MARK = valueOf("WRITE_BUFFER_WATER_MARK");
    public static final ChannelOption<Boolean> ALLOW_HALF_CLOSURE = valueOf("ALLOW_HALF_CLOSURE");
    public static final ChannelOption<Boolean> AUTO_READ = valueOf("AUTO_READ");
    public static final ChannelOption<Boolean> AUTO_CLOSE = valueOf("AUTO_CLOSE");
    public static final ChannelOption<Boolean> SO_BROADCAST = valueOf("SO_BROADCAST");
    public static final ChannelOption<Boolean> SO_KEEPALIVE = valueOf("SO_KEEPALIVE");
    public static final ChannelOption<Integer> SO_SNDBUF = valueOf("SO_SNDBUF");
    public static final ChannelOption<Integer> SO_RCVBUF = valueOf("SO_RCVBUF");
    public static final ChannelOption<Boolean> SO_REUSEADDR = valueOf("SO_REUSEADDR");
    public static final ChannelOption<Integer> SO_LINGER = valueOf("SO_LINGER");
    public static final ChannelOption<Integer> SO_BACKLOG = valueOf("SO_BACKLOG");
    public static final ChannelOption<Integer> SO_TIMEOUT = valueOf("SO_TIMEOUT");
    public static final ChannelOption<Integer> IP_TOS = valueOf("IP_TOS");
    public static final ChannelOption<InetAddress> IP_MULTICAST_ADDR = valueOf("IP_MULTICAST_ADDR");
    public static final ChannelOption<NetworkInterface> IP_MULTICAST_IF = valueOf("IP_MULTICAST_IF");
    public static final ChannelOption<Integer> IP_MULTICAST_TTL = valueOf("IP_MULTICAST_TTL");
    public static final ChannelOption<Boolean> IP_MULTICAST_LOOP_DISABLED = valueOf("IP_MULTICAST_LOOP_DISABLED");
    public static final ChannelOption<Boolean> TCP_NODELAY = valueOf("TCP_NODELAY");
    /** @deprecated */
    @Deprecated
    public static final ChannelOption<Boolean> DATAGRAM_CHANNEL_ACTIVE_ON_REGISTRATION = valueOf("DATAGRAM_CHANNEL_ACTIVE_ON_REGISTRATION");
    public static final ChannelOption<Boolean> SINGLE_EVENTEXECUTOR_PER_GROUP = valueOf("SINGLE_EVENTEXECUTOR_PER_GROUP");

    public static <T> ChannelOption<T> valueOf(String name) {
        return (ChannelOption)pool.valueOf(name);
    }

    public static <T> ChannelOption<T> valueOf(Class<?> firstNameComponent, String secondNameComponent) {
        return (ChannelOption)pool.valueOf(firstNameComponent, secondNameComponent);
    }

    public static boolean exists(String name) {
        return pool.exists(name);
    }

    /** @deprecated */
    @Deprecated
    public static <T> ChannelOption<T> newInstance(String name) {
        return (ChannelOption)pool.newInstance(name);
    }

    private ChannelOption(int id, String name) {
        super(id, name);
    }

    /** @deprecated */
    @Deprecated
    protected ChannelOption(String name) {
        this(pool.nextId(), name);
    }

    public void validate(T value) {
        if (value == null) {
            throw new NullPointerException("value");
        }
    }
}
```

