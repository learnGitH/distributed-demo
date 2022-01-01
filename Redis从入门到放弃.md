# Redis从入门到放弃

## 一、redis核心数据结构详解

官网文档：https://redis.io/commands

中文文档：http://redisdoc.com/

<img src="https://img02.sogoucdn.com/app/a/100540022/2021081111305681768184.png" style="zoom: 50%;" />

### 1、string

#### 1.1 string常用操作

SET key value							//存入字符串键值对

MSET key value [key value ...]  			//批量存入字符串键值对

SETNX key value						//存入一个不存在的字符串键值对

GET key								//获取一个字符串键值

MGET key [key...]						//批量获取字符串键值对

DEL key [key...]							//删除对应的键

EXPIRE key seconds 					//设置一个键的过期时间（秒）

INCR key								//将key中存储的数字值加1

DRCR key								//将key中存储的数字值减1

INCRBY key increment					//将key中存储的数字值加increment

DECRBY key decrement					//将key中存储的数字值减decrement

#### 1.2 string应用场景

##### 1.2.1 单值缓存

SET key value

GET key

##### 1.2.2 对象缓存

SET user value(json格式数据)

MSET a 1 b 2

MGET a b

##### 1.2.2 分布式锁

SETNX order:1001 1003233	//返回1代表获取锁成功，返回0代表获取锁失败

。。。执行业务

DEL order:1001

##### 1.2.3 计数器

INCR article:readcount:文章id         //记录文章的阅读量

GET article:readcount:文章id

##### 1.2.4 存放session信息

用redis实现session共享

##### 1.2.5 分布式系统全局序列号

INCRBY orderId 1000		//分布式系统全局序列号

### 2、hash

#### 2.1 hash常用操作

HSET key field value				//存储一个哈希表key的键值

HSETNX key field value				//存储一个不存在的哈希表key的键值

HMSET key field value [field value...]	//在一个哈希表key中存储多个键值对

HGET  key  field 					//获取哈希表key对应的field键值

HMGET  key  field  [field ...]			//批量获取哈希表key中多个field键值

HDEL  key  field  [field ...] 			//删除哈希表key中的field键值

HLEN  key						//返回哈希表key中field的数量

HGETALL key						//返回哈希表key中所有的键值

HINCRBY key field increment			//为哈希表key中field键的值加上增量increment

#### 2.2 hash应用场景

##### 2.2.1 对象缓存

HMSET user 1:name haiin 1:age 18

HMGET user 1:name 1:balance

##### 2.2.2 电商购物车

以用户id为key、商品id为field、商品数量为value

hset cart:1001 10088 1		//添加商品

hincrby cart:1001 1008 1		//增加商品数量

hlen cart:1001   			//商品总数

hdel cart:1001 10088		//删除商品

hgetall cart:10088			//获取购物车所有商品

### 3、list

#### 3.1 list 常用操作

LPUSH  key  value [value ...] 		//将一个或多个值value插入到key列表的表头(最左边)

​	RPUSH  key  value [value ...]	 	//将一个或多个值value插入到key列表的表尾(最右边)

LPOP  key					//移除并返回key列表的头元素

RPOP  key					//移除并返回key列表的尾元素

LRANGE  key  start  stop			//返回列表key中指定区间内的元素，区间以偏移量start和stop指定

BLPOP  key  [key ...]  timeout		//从key列表表头弹出一个元素，若列表中没有元素，阻塞等待timeout秒,如果timeout=0,一直阻塞等待

BRPOP  key  [key ...]  timeout 	       //从key列表表尾弹出一个元素，若列表中没有元素，阻塞等待timeout秒,如果timeout=0,一直阻塞等待

#### 3.2 list应用场景

##### 3.2.1 常用数据结构

Stack(栈) = LPUSH + LPOP

Queue(队列) = LPUSH + RPOP

Blocking MQ(阻塞队列) = LPUSH + BRPOP

##### 3.2.2 微博和微信公众号信息流

例如我关注了一个公众号java知音，那么java知音要是在公众号发文章之后就会执行：LPUSH msg:我的ID 消息ID

### 4、set

#### 4.1 set常用操作

SADD key member [member...] 		//往集合key中存入元素，元素存在则忽略,若key不存在则新建

SREM key member [member...]		//从集合key中删除元素

SMEMBERS key					//获取集合key中所有元素

SCARD key 						//获取集合key的元素个数

SISMEMBER key member			//判断member元素是否存在于集合key中

SRANDMEMBER key [count]			//从集合key中选出count个元素，元素不从key中删除

SPOP key [count]					//从集合key中选出count个元素，元素从key中删除

SINTER key [key...]					//交集运算

SINTERSTORE destination key [key...]    //将交集结果存入新集合destination中

SUNION key [key...]					//并集运算

SUNIONSTORE destination key [key...]	//将并集结果存入新集合destination中

SDIFF key [key...]					//差集运算

SDIFFSTORE destination key [key...]	//将差集结果存入新集合destination中

#### 4.2 set引用场景

##### 4.2.1 微信抽奖小程序

SADD key [userID..]							//将所有待抽奖的用户放入这个set集合中

SMEMBERS key 							//查看参与抽奖所有用户

SRANDMEMBER key [count] / SPOP key [count]	//抽取count名中奖者

##### 4.2.2 微信微博点赞，收藏，标签

SADD like:消息ID  用户ID						//点赞

SREM like:消息ID  用户ID					//取消点赞

SISMEMBER like:消息ID  用户ID				//检查用户是否点过赞

SMEMBERS like:消息ID  						//获取点赞列表

SCARD like:消息ID 							//获取点赞用户数

##### 4.2.3 关注模型

由于set提供了交集、并集、差集的功能，这样可以微博上的关注模型，例如我关注一位博主，可以显示我和他共同关注的人，我关注的人也关注这位博主的信息

### 5、zset

#### 5.1 zset常用操作

ZADD key score member [score member] 		//往有序集合key中加入带分值元素

ZREM key member [member...]				//从有序集合key中删除元素

ZSCORE key member						//返回有序集合key中元素member的分值

ZINCRBY key increment member				//为有序集合key中元素member的分值加上increment 

ZCARD key								//返回有序集合key中元素个数

ZRANGE key start stop [WITHSCORES]			//正序获取有序集合key从start下标到stop下标的元素

ZREVRANGE key start stop [WITHSCORES]		//倒序获取有序集合key从start下标到stop下标的元素

ZUNIONSTORE destkey numkeys key [key ...] 	//并集计算

ZINTERSTORE destkey numkeys key [key …]	//交集计算

#### 5.2 zset应用场景

##### 5.2.1 微博热搜排行榜

ZINCRBY hotNews:20210805 1 新闻信息			//点击新闻信息

ZREVRANGE  hotNews:20210805  0  9  WITHSCORES 	展示当日排行前十

### 6、高级命令

#### 6.1 keys

keys是全量遍历键，用来列出所有满足特定正则字符串规则的key,当redis数据量比较大时，性能比较差，要避免使用

192.168.159.160:8005> set test1 1

OK

192.168.159.160:8005> set test4 4

OK

192.168.159.160:8005> set test8 8

OK

192.168.159.160:8005> keys test*

1) "test4"

2) "test1"

3) "test8"

#### 6.2 scan

scan是渐进式遍历键：SCAN cursor [MATCH pattern] [COUNT count]

scan参数提供了三个参数，第一个cursor整数值（hash桶的索引值），第二个是key的正则模式，第三个是一次遍历的key的数量（参考值，底层遍历的数量不一定），并不是符合条件的结果数量。第一次遍历时，cursor值为0，然后将返回结果中第一个整数值作为下一次遍历的cursor。一直遍历到返回的cursor值为0时结束。

但是scan并非完美无瑕，如果在scan的过程中如果有键的变化（增加、删除、修改），那么遍历效果可能会碰到如下问题：新增的键可能没有遍历到，遍历出重复的键等情况，也就是说scan并不能保证完整的遍历出来所有的键，这些是我们在开发时需要考虑的。

192.168.159.160:8005> keys *

\1) "test4"

\2) "test1"

\3) "stock"

\4) "b"

\5) "test8"

192.168.159.160:8005> scan 0 match test* count 1

\1) "4"

\2)  "test4"

192.168.159.160:8005> scan 4 match test* count 1

\1) "1"

\2)  "test8"

192.168.159.160:8005> scan 1 match test* count 1

\1) "5"

\2)  "test1"

192.168.159.160:8005> scan 5 match test* count 1

\1) "0"

\2) (empty list or set)

192.168.159.160:8005> 

#### 6.3 info

info能够查看redis服务运行信息，分为9大块，每个块都有非常多的参数，这9块分别是：

Server 服务器运行的环境参数 Clients 客户端相关信息 Memory 服务器运行内存统计数据 Persistence 持久化信息 Stats 通用统计数据 Replication 主从复制相关信息 CPU CPU 使用情况 Cluster 集群信息 KeySpace 键值对统计数量信息

## 二、redis的单线程和高性能

### 1、redis是单线程吗？

Redis的单线程主要是指Redis的网络IO和键值对读写是由一个线程来完成的，这也是Redis对外提供键值存储服务的主要流程。但Redis的其他功能，比如持久化、异步删除、集群数据同步等，其实是由额外的线程来执行的。

### 2、redis单线程为什么还能这么快？

因为它所有的数据都在内存中，所有的运算都是内存级别的运算，而且单线程避免了多线程的切换性能损耗问题。正因为Redis是单线程，所以要小心使用Redis命令，对于那些耗时的指令（比如keys）,一定要谨慎使用，一不小心就可能会导致Redis卡顿。

### 3、redis单线程如何处理那么多的并发客户端连接？

redis的IO多路复用：redis利用epoll来实现IO多路复用，将连接信息和事件放到队列中，依次放到文件事件分发器，事件分发器将事件分发给事件处理器。

<img src="https://img04.sogoucdn.com/app/a/100540022/2021081114222563267882.png" style="zoom: 67%;" />

在redis.config可以查看和修改最大的连接数，默认10000：

192.168.159.160:8005> config get maxclients

\1) "maxclients"

\2) "10000"

## 三、redis的持久化方式

### 1、RDB

下载完后不对配置进行修改，Redis默认内存的持久化方式采用的是rdb的方式，主要保存在名字为dump.rdb的二进制文件。

rdb默认的数据集：

save 900 1			//表示在900秒如果至少有一个键被改变，则把内存的信息写入dump.rdb文件

save 300 10			//表示在300秒内如果至少有一个键被改变，则把内存的信息写入dump.rdb文件

save 60 10000		//表示在60秒内如果至少有一个键被改变，则把内存的信息写入dump.rdb文件

如果想关闭RDB只需要将所有的save保存策略注释掉即可

还可以手动执行命令生成RDB快照，进入redis客户端执行命令save或bgsave可以生成dump.rdb文件， 每次命令执行都会将所有redis内存快照到一个新的rdb文件里，并覆盖原有rdb快照文件。

Redis 借助操作系统提供的写时复制技术（Copy-On-Write, COW），在生成快照的同时，依然可以正常处理写命令。简单来说，bgsave 子	进程是由主线程 fork 生成的，可以共享主线程的所有内存数据。bgsave 子进程运行后，开始读取主线程的内存数据，并把它们写入 RDB 文件。此时，如果主线程对这些数据也都是读操作，那么，主线程和 bgsave 子进程相互不影响。但是，如果主线程要修改一块数据，那么，这块数据就会被复制一份，生成该数据的副本。然后，bgsave 子进程会把这个副本数据写入 RDB 文件，而在这个过程中，主线程仍然可以直接修改原来的数据。

save与bgsave对比：

<img src="https://img03.sogoucdn.com/app/a/100540022/2021081114292144443024.png" style="zoom: 80%;" />

配置自动生成rdb文件后台使用的是bgsave方式。

### 2、AOF

快照功能并不是非常持久（durable）:如果redis因为某些原因而造成故障停机，那么服务器将丢失最近写入、且仍未保存到快照中的那些数据。从1.1版本开始，Redis增加了一种完全耐久的持久化方式：AOF(append-only file)持久化，将修改的每一条指令记录进文件appendonly.aof中（先写入os cache,没隔一段时间fsync到磁盘）

比如我执行：set name haibin

那么在aof文件就会记录如下：

*3

$3

set

$4

name

$6

haibin

这是一种resp协议格式数据，星号后面的数字代表命令有多少个参数，$号后面的数字代表这个参数有几个字符

开启方式：在redis.conf文件中将#appendonly yes注释去掉即可打开aof的功能

有三个配置项：

appendfsync always：每次有新命令追加到 AOF 文件时就执行一次 fsync ，非常慢，也非常安全。 

appendfsync everysec：每秒 fsync 一次，足够快，并且在故障时只会丢失 1 秒钟的数据。 

appendfsync no：从不 fsync ，将数据交给操作系统来处理。更快，也更不安全的选择。

推荐（并且也是默认）的措施为每秒 fsync 一次， 这种 fsync 策略可以兼顾速度和安全性。这样的话， 当 Redis 重新启动时， 程序就可以通过重新执行 AOF 文件中的命令来达到重建数据集的目的。

AOF的重写功能:AOF文件里有很多没用的指令，所有AOF会定期根据内存的最新数据生成aof文件

例如执行：

incr id

incr id

incr id

incr id

incr id

那么重写后aof文件里变成：

 *3 $3 SET $2 id $1 5

有以下两个配置来控制aof自动重写频率：

\# auto‐aof‐rewrite‐min‐size 64mb //aof文件至少要达到64M才会自动重写，文件太小恢复速度本来就很快，重写的意义不大 # auto‐aof‐rewrite‐percentage 100 //aof文件自上一次重写后文件大小增长了100%则再次触发重写

当然AOF还可以手动重写，进入redis客户端执行命令bgrewriteaof重写AOF 注意，AOF重写redis会fork出一个子进程去做(与bgsave命令类似)，不会对redis正常命令处理有太多影响

RDB 和 AOF ，该如何选择？

<img src="https://img02.sogoucdn.com/app/a/100540022/2021081114324084880240.png" style="zoom:80%;" />

生产环境可以都启用，redis启动时如果既有rdb文件又有aof文件则优先选择aof文件恢复数据，因为aof一般来说数据更全一点。

### 3、Redis4.0后的混合持久化

重启 Redis 时，我们很少使用 RDB来恢复内存状态，因为会丢失大量数据。我们通常使用 AOF 日志重放，但是重放 AOF 日志性能相对 RDB来说要慢很多，这样在 Redis 实例很大的情况下，启动需要花费很长的时间。 Redis 4.0 为了解决这个问题，带来了一个新的持久化选项——混合持久化。

\# aof-use-rdb-preamble yes		//开启混合持久化（前提需要先开启aof: appendonly yes）

如果开启了混合持久化，AOF在重写时，不再是单纯将内存数据转换为RESP命令写入AOF文件，而是将重写这一刻之前的内存做RDB快照处理，并且将RDB快照内容和增量的AOF修改内存数据的命令存在一起，都写入新的AOF文件，新的文件一开始不叫appendonly.aof，等到重写完新的AOF文件才会进行改名，覆盖原有的AOF文件，完成新旧两个AOF文件的替换。 于是在 Redis 重启的时候，可以先加载 RDB 的内容，然后再重放增量 AOF 日志就可以完全替代之前的AOF 全量文件重放，因此重启效率大幅得到提升。

混合持久化AOF文件结构如下:

![](https://img03.sogoucdn.com/app/a/100540022/2021081114350604804111.png)

Redis数据备份策略：

1). 写crontab定时调度脚本，每小时都copy一份rdb或aof的备份到一个目录中去，仅仅保留最近48 小时的备份 

2). 每天都保留一份当日的数据备份到一个目录中去，可以保留最近1个月的备份 

3). 每次copy备份的时候，都把太旧的备份给删了 

4). 每天晚上将当前机器上的备份复制一份到其他机器上，以防机器损坏

## 四、redis部署方式

### 1、单机模式

#### 1.1 安装配置

\1) 下载地址：https://redis.io/download

\2) 先下载后的压缩包上传到linux进行解压：tar -zxvf redis-5.0.13.tar.gz

) 安装gcc:yum install gcc

\4) 进入到解压好的redis-5.0.13目录下，执行命令make进行编译与安装

5）修改配置(redis.conf)

daemonize yes			#后台启动

protected-mode no		#关闭保护模式，开启的话，只有本机才可以访问redis 

\# bind 127.0.0.1              #需要注释掉bind(bind绑定的是自己机器网卡的ip，如果有多块网卡可以配多个ip，代表允许客户 					端通过机器的哪些网卡ip去访问，内网一般可以不配置bind，注释掉即可)

6）启动服务：src/redis-server redis.conf

\7) 验证启动是否成功：ps -ef|grep redis

\8) 进入redis客户端：src/redis-cli(这里不加参数默认连接的是6397的redis服务，如果是别的端口需要指定，指定方式：src/redis-cli -p 端口)

9）退出客户端：quit

#### 1.2 通过客户端jedis进行连接

1）引入pom.xml文件

```java
<dependency>
	<groupId>redis.clients</groupId>
	<artifactId>jedis</artifactId>
	<version>2.9.0</version>
</dependency>
```

2）代码逻辑

```
/**
 * 单机模式jedis连接配置及其案例
 */
public class JedisSingleTest {
    public static void main(String[] args) throws IOException {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(20);
        jedisPoolConfig.setMaxIdle(10);
        jedisPoolConfig.setMinIdle(5);
        //单机配置
        JedisPool jedisPool =  new JedisPool(jedisPoolConfig,"192.168.159.169",6379,3000,null);
        Jedis jedis = null;
        try{
            //从redis连接池里拿出一个连接执行命令
            jedis = jedisPool.getResource();
            System.out.println(jedis.set("single","haibin"));
            System.out.println(jedis.get("single"));
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            //注意这里不是关闭连接，在JedisPool模式下，Jedis会被归还给资源池
            if (jedis != null){
                jedis.close();
            }
        }

    }

}
```

3）结果

<img src="https://img03.sogoucdn.com/app/a/100540022/2021081114442367975222.png" style="zoom:80%;" />

#### 1.3 springboot整合redis进行连接

1)引入pom.xml文件

```java
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<dependency>
  <groupId>org.apache.commons</groupId>
  <artifactId>commons-pool2</artifactId>
</dependency>
```

2）核心配置文件

```java
server:
  port: 8080

spring:
  redis:
    database: 0
    timeout: 3000
    host: 192.168.159.168
    port: 6381
    lettuce:
      pool:
        max-idle: 50
        min-idle: 10
        max-active: 100
        max-wait: 1000
```

\3) 代码逻辑

```java
@Autowired
private StringRedisTemplate stringRedisTemplate;
/**
 * 测试string
 */
@RequestMapping("/test_string")
public void testString(){
    stringRedisTemplate.opsForValue().set("a","1");
    stringRedisTemplate.opsForValue().set("b","2");
    stringRedisTemplate.opsForValue().set("c","3");
    System.out.println(stringRedisTemplate.opsForValue().get("a"));
    System.out.println(stringRedisTemplate.opsForValue().get("b"));
    System.out.println(stringRedisTemplate.opsForValue().get("c"));
    System.out.println(stringRedisTemplate.delete("c"));
    System.out.println(stringRedisTemplate.opsForValue().size("a"));
}
```

4)结果

```java
1
2
3
true
1
```

### 2、主从模式

#### 2.1 redis主从架构

<img src="https://img03.sogoucdn.com/app/a/100540022/2021081115004162488768.png" style="zoom:80%;" />

#### 2.2 redis配置文件配置

这里在同一台机器进行部署演示，然后以不同的端口区分（通过不同的机器道理一样）。master:192.168.159.171:6379,slave:192.168.159.171:6380,192.168.159.171:6381

1）redis-6379.conf

```java
redis-6379.conf:
port 6379
daemonize yes			#后台启动
protected-mode no		#关闭保护模式，开启的话，只有本机才可以访问redis 
pidfile /var/run/redis_6379.pid  # 把pid进程号写入pidfile配置的文件
logfile "6379.log"
dir /usr/local/redis-5.0.3/data/6379  # 指定数据存放目录
# 需要注释掉bind
# bind 127.0.0.1（bind绑定的是自己机器网卡的ip，如果有多块网卡可以配多个ip，代表允许客户端通过机器的哪些网卡ip
```

2)redis-6380.conf

```java
redis-6380.conf:
port 6380
daemonize yes        #后台启动
protected-mode no     #关闭保护模式，开启的话，只有本机才可以访问redis
pidfile /var/run/redis_6380.pid  # 把pid进程号写入pidfile配置的文件
logfile "6380.log"
dir /usr/local/redis-5.0.3/data/6380  # 指定数据存放目录
# 需要注释掉bind
# bind 127.0.0.1（bind绑定的是自己机器网卡的ip，如果有多块网卡可以配多个ip，代表允许客户端通过机器的哪些网卡ip
#主从配置
replicaof 192.168.159.171 6379   # 从本机6379的redis实例复制数据，Redis 5.0之前使用slaveof
replica-read-only yes  # 配置从节点只读
```

3)redis-6381.conf

```java
redis-6381.conf:
port 6381
daemonize yes        #后台启动
protected-mode no     #关闭保护模式，开启的话，只有本机才可以访问redis
pidfile /var/run/redis_6381.pid  # 把pid进程号写入pidfile配置的文件
logfile "6381.log"
dir /usr/local/redis-5.0.3/data/6381  # 指定数据存放目录
# 需要注释掉bind
# bind 127.0.0.1（bind绑定的是自己机器网卡的ip，如果有多块网卡可以配多个ip，代表允许客户端通过机器的哪些网卡ip
#主从配置
replicaof 192.168.159.171 6379   # 从本机6379的redis实例复制数据，Redis 5.0之前使用slaveof
replica-read-only yes  # 配置从节点只读
```

4)启动redis

src/redis-server redis-6379.conf

redis-server redis-6380.conf

redis-sesrver redis-6381.conf

5)客户端连接

src/redis-cli -p 6379

src/redis-cli -p 6380

src/redis-cli -p 6381

6)测试

主节点操作：

<img src="https://img01.sogoucdn.com/app/a/100540022/2021081115241666646532.png"  />

从节点操作：

<img src="https://img04.sogoucdn.com/app/a/100540022/2021081115241644259426.png" style="zoom:80%;" />

#### 2.3 redis主从工作原理

如果你为master配置了一个slave，不管这个slave是否是第一次连接上Master，它都会发送一个**PSYNC**命令给master请求复制数据。

master收到PSYNC命令后，会在后台进行数据持久化通过bgsave生成最新的rdb快照文件，持久化期间，master会继续接收客户端的请求，它会把这些可能修改数据集的请求缓存在内存中。当持久化进行完毕以后，master会把这份rdb文件数据集发送给slave，slave会把接收到的数据进行持久化生成rdb，然后再加载到内存中。然后，master再将之前缓存在内存中的命令发送给slave。

当master与slave之间的连接由于某些原因而断开时，slave能够自动重连Master，如果master收到了多个slave并发连接请求，它只会进行一次持久化，而不是一个连接一次，然后再把这一份持久化的数据发送给多个并发连接的slave。

1）主从复制(全量复制)流程图：

<img src="https://img03.sogoucdn.com/app/a/100540022/2021081115314053245872.png" style="zoom: 67%;" />

2)数据部分复制

当master和slave断开重连后，一般都会对整份数据进行复制。但从redis2.8版本开始，redis改用可以支持部分数据复制的命令PSYNC去master同步数据，slave与master能够在网络连接断开重连后只进行部分数据复制(断点续传)。master会在其内存中创建一个复制数据用的缓存队列，缓存最近一段时间的数据，master和它所有的slave都维护了复制的数据下标offset和master的进程id，因此，当网络连接断开后，slave会请求master继续进行未完成的复制，从所记录的数据下标开始。如果master进程id变化了，或者从节点数据下标offset太旧，已经不在master的缓存队列里了，那么将会进行一次全量数据的复制。

主从复制(部分复制，断点续传)流程图：

<img src="https://img01.sogoucdn.com/app/a/100540022/2021081115314072822228.png" style="zoom:67%;" />

3)从节点复制

如果有很多从节点，为了缓解**主从复制风暴**(多个从节点同时复制主节点导致主节点压力过大)，可以做如下架构，让部分从节点与从节点(与主节点同步)同步数据

<img src="https://img03.sogoucdn.com/app/a/100540022/2021081115314052503049.png" style="zoom:80%;" />

#### 2.4 通过客户端jedis进行连接

1）引入pom.xml文件

```java
<dependency>
	<groupId>redis.clients</groupId>
	<artifactId>jedis</artifactId>
	<version>2.9.0</version>
</dependency>
```

2）代码逻辑

```java
/**
 * 主从模式jedis连接配置及其案例
 */
public class JedisMasterAndSlaveTest {

    public static void main(String[] args){
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(20);
        jedisPoolConfig.setMaxIdle(10);
        jedisPoolConfig.setMinIdle(5);

        //主从配置
        JedisPool jedisPool = new JedisPool(jedisPoolConfig,"192.168.159.171",6379,3000,null);
        Jedis jedis = null;
        try{
            jedis = jedisPool.getResource();
            System.out.println(jedis.set("masterAndSlave","haibin"));
            System.out.println(jedis.get("masterAndSlave"));

            //管道示例
            Pipeline pl = jedis.pipelined();
            for (int i = 0; i < 10; i++){
                pl.incr("pipelineKey");
                pl.set("haibin" + i,"haibin");
            }
            List<Object> results = pl.syncAndReturnAll();
            System.out.println(results);

            //lua脚本模拟一个商品减库存的原子操作
            jedis.set("product_count_10016","15");
            String script = " local count = redis.call('get', KEYS[1]) " +
                    " local a = tonumber(count) " +
                    " local b = tonumber(ARGV[1]) " +
                    " if a >= b then " +
                    " redis.call('set', KEYS[1], a-b) " +
                    " return 1 " +
                    " end " +
                    " return 0 ";
            Object obj = jedis.eval(script, Arrays.asList("product_count_10016"),Arrays.asList("10"));
            System.out.println(obj);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            //注意这里不是关闭连接，在JedisPool模式下，Jedis会被归还给资源池
            if (jedis != null){
                jedis.close();
            }
        }
    }

}
```

3)redis管道（Pipeline）

客户端可以一次性发送多个请求而不用等待服务器的响应，待所有命令都发送完后再一次性读取服务的响应，这样可以极大的降低多条命令执行的网络传输开销，管道执行多条命令的网络开销实际上只相当于一次命令执行的网络开销。需要注意到是用pipeline方式打包命令发送，redis必须在处理完所有命令前先缓存起所有命令的处理结果。打包的命令越多，缓存消耗内存也越多。所以并不是打包的命令越多越好。

pipeline中发送的每个command都会被server立即执行，如果执行失败，将会在此后的响应中得到信息；也就是pipeline并不是表达“所有command都一起成功”的语义，管道中前面命令失败，后面命令不会有影响，继续执行。

详细代码示例见上面jedis连接示例：

```java
//管道示例
Pipeline pl = jedis.pipelined();
for (int i = 0; i < 10; i++){
    pl.incr("pipelineKey");
    pl.set("haibin" + i,"haibin");
}
List<Object> results = pl.syncAndReturnAll();
System.out.println(results);
```

4)redis Lua脚本

Redis在2.6推出了脚本功能，允许开发者使用Lua语言编写脚本传到Redis中执行。使用脚本的好处如下:

1、**减少网络开销**：本来5次网络请求的操作，可以用一个请求完成，原先5次请求的逻辑放在redis服务器上完成。使用脚本，减少了网络往返时延。**这点跟管道类似**。

2、**原子操作**：Redis会将整个脚本作为一个整体执行，中间不会被其他命令插入。**管道不是原子的，不过redis的批量操作命令(类似mset)是原子的。**

3、**替代redis的事务功能**：redis自带的事务功能很鸡肋，而redis的lua脚本几乎实现了常规的事务功能，官方推荐如果要使用redis的事务功能可以用redis lua替代。

从Redis2.6.0版本开始，通过内置的Lua解释器，可以使用EVAL命令对Lua脚本进行求值。EVAL命令的格式如下：

```
EVAL script numkeys key [key ...] arg [arg ...]
```

script参数是一段Lua脚本程序，它会被运行在Redis服务器上下文中，这段脚本**不必(也不应该)定义为一个Lua函数**。numkeys参数用于指定键名参数的个数。键名参数 key [key ...] 从EVAL的第三个参数开始算起，表示在脚本中所用到的那些Redis键(key)，这些键名参数可以在 Lua中通过全局变量KEYS数组，用1为基址的形式访问( KEYS[1] ， KEYS[2] ，以此类推)。

在命令的最后，那些不是键名参数的附加参数 arg [arg ...] ，可以在Lua中通过全局变量**ARGV**数组访问，访问的形式和KEYS变量类似( ARGV[1] 、 ARGV[2] ，诸如此类)。例如

```java
127.0.0.1:6379> eval "return {KEYS[1],KEYS[2],ARGV[1],ARGV[2]}" 2 key1 key2 first second
1) "key1"
2) "key2"
3) "first"
4) "second"　
```

其中 "return {KEYS[1],KEYS[2],ARGV[1],ARGV[2]}" 是被求值的Lua脚本，数字2指定了键名参数的数量， key1和key2是键名参数，分别使用 KEYS[1] 和 KEYS[2] 访问，而最后的 first 和 second 则是附加参数，可以通过 ARGV[1] 和 ARGV[2] 访问它们。

在 Lua 脚本中，可以使用**redis.call()**函数来执行Redis命令

Jedis调用示例详见上面jedis连接示例：

```java
//lua脚本模拟一个商品减库存的原子操作
jedis.set("product_count_10016","15");
String script = " local count = redis.call('get', KEYS[1]) " +
        " local a = tonumber(count) " +
        " local b = tonumber(ARGV[1]) " +
        " if a >= b then " +
        " redis.call('set', KEYS[1], a-b) " +
        " return 1 " +
        " end " +
        " return 0 ";
Object obj = jedis.eval(script, Arrays.asList("product_count_10016"),Arrays.asList("10"));
System.out.println(obj);
```

**注意，不要在Lua脚本中出现死循环和耗时的运算，否则redis会阻塞，将不接受其他的命令， 所以使用时要注意不能出现死循环、耗时的运算。redis是单进程、单线程执行脚本。管道不会阻塞redis。**

### 3、哨兵模式

#### 3.1哨兵架构

<img src="https://img03.sogoucdn.com/app/a/100540022/2021081117065847157353.png" style="zoom: 50%;" />

sentinel哨兵是特殊的redis服务，不提供读写服务，主要用来监控redis实例节点。

哨兵架构下client端第一次从哨兵找出redis的主节点，后续就直接访问redis的主节点，不会每次都通过sentinel代理访问redis的主节点，当redis的主节点发生变化，哨兵会第一时间感知到，并且将新的redis主节点通知给client端(这里面redis的client端一般都实现了订阅功能，订阅sentinel发布的节点变动消息)

#### 3.2redis哨兵架构搭建

1)sentinel-26379.conf

```java
port 26379
daemonize yes
pidfile "/var/run/redis-sentinel-26379.pid"
logfile "26379.log"
dir "/shenhaibin/redis/redis-5.0.13/data"
# sentinel monitor <master-redis-name> <master-redis-ip> <master-redis-port> <quorum>
# quorum是一个数字，指明当有多少个sentinel认为一个master失效时(值一般为：sentinel总数/2 + 1)，master才算真正失效
sentinel monitor mymaster 192.168.159.171 6379 2   # mymaster这个名字随便取，客户端访问时会用到
```

2)sentinel-26380.conf

```java
port 26380
daemonize yes
pidfile "/var/run/redis-sentinel-26380.pid"
logfile "26380.log"
dir "/shenhaibin/redis/redis-5.0.13/data"
# sentinel monitor <master-redis-name> <master-redis-ip> <master-redis-port> <quorum>
# quorum是一个数字，指明当有多少个sentinel认为一个master失效时(值一般为：sentinel总数/2 + 1)，master才算真正失效
sentinel monitor mymaster 192.168.159.171 6379 2   # mymaster这个名字随便取，客户端访问时会用到
```

3)sentinel-26381.conf

```java
port 26381
daemonize yes
pidfile "/var/run/redis-sentinel-26381.pid"
logfile "26381.log"
dir "/shenhaibin/redis/redis-5.0.13/data"
# sentinel monitor <master-redis-name> <master-redis-ip> <master-redis-port> <quorum>
# quorum是一个数字，指明当有多少个sentinel认为一个master失效时(值一般为：sentinel总数/2 + 1)，master才算真正失效
sentinel monitor mymaster 192.168.159.171 6379 2   # mymaster这个名字随便取，客户端访问时会用到
```

redis的配置文件跟主从模式一摸一样这里就不再重复讲解

4）启动sentinel哨兵实例

src/redis-sentinel sentinel-26379.conf

src/redis-sentinel sentinel-26380.conf

src/redis-sentinel sentinel-26381.conf

5)查看sentinel的info信息

src/redis-cli -p 26379

此时可以看到sentinel的info里已经识别出redis的主从

sentinel集群都启动完毕后，会将哨兵集群的元数据信息写入所有sentinel的配置文件里去(追加在文件的最下面)，我们查看下如下配置文件sentinel-26379.conf，如下所示：

```java
sentinel known-replica mymaster 192.168.159.171 6381
sentinel known-replica mymaster 192.168.159.171 6380
sentinel known-sentinel mymaster 192.168.159.171 26380 f3fea4ea95cdfd4e2f667beeeaade922cc18b41a
sentinel known-sentinel mymaster 192.168.159.171 26381 cabfdda0031c991ef781af37b475a2b71ad61583
```

当redis主节点如果挂了，哨兵集群会重新选举出新的redis主节点，同时会修改所有sentinel节点配置文件的集群元数据信息，比如6379的redis如果挂了，假设选举出的新主节点是6380，则sentinel文件里的集群元数据信息会变成如下所示：

```
sentinel known-replica mymaster 192.168.159.171 6381
sentinel known-replica mymaster 192.168.159.171 6379
sentinel known-sentinel mymaster 192.168.159.171 26380 f3fea4ea95cdfd4e2f667beeeaade922cc18b41a
sentinel known-sentinel mymaster 192.168.159.171 26381 cabfdda0031c991ef781af37b475a2b71ad61583
```

同时还会修改sentinel文件里之前配置的mymaster对应的6379端口，改为6380

```
sentinel monitor mymaster 192.168.159.171 6380 2
```

当6379的redis实例再次启动时，哨兵集群根据集群元数据信息就可以将6379端口的redis节点作为从节点加入集群

#### 3.3通过客户端jedis进行连接

1）引入pom.xml文件

```java
<dependency>
	<groupId>redis.clients</groupId>
	<artifactId>jedis</artifactId>
	<version>2.9.0</version>
</dependency>
```

2）代码逻辑

```java
public class JedisSentinelTest {

    public static void main(String[] args) throws IOException {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(20);
        config.setMaxIdle(10);
        config.setMinIdle(5);

        String masterName = "mymaster";
        Set<String> sentinels = new HashSet<String>();
        sentinels.add(new HostAndPort("192.168.159.171",26379).toString());
        sentinels.add(new HostAndPort("192.168.159.171",26380).toString());
        sentinels.add(new HostAndPort("192.168.159.171",26381).toString());
        //JedisSentinelPool其实本质跟JedisPool类似，都是与redis主节点建立的连接池
        //JedisSentinelPool并不是说与sentinel建立的连接池，而是通过sentinel发现redis主节点并与其建立连接
        JedisSentinelPool jedisSentinelPool = new JedisSentinelPool(masterName, sentinels, config, 3000, null);
        Jedis jedis = null;
        try {
            jedis = jedisSentinelPool.getResource();
            System.out.println(jedis.set("sentinel", "haibin"));
            System.out.println(jedis.get("sentinel"));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //注意这里不是关闭连接，在JedisPool模式下，Jedis会被归还给资源池。
            if (jedis != null)
                jedis.close();
        }
    }
}
```

#### 3.4哨兵的redis整合springboot连接

1)引入pom.xml文件

```java
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<dependency>
  <groupId>org.apache.commons</groupId>
  <artifactId>commons-pool2</artifactId>
</dependency>
```

2）核心配置文件

```
server:
  port: 8080

spring:
  redis:
    database: 0
    timeout: 3000
    sentinel:	#哨兵模式
      master: mymaster	#主服务器所在集群名称
      nodes: 192.168.159.164:26379,192.168.159.164:26380,192.168.159.164:26381
    lettuce:
      pool:
        max-idle: 50
        min-idle: 10
        max-active: 100
        max-wait: 1000
```

3)测试代码

```
private static final Logger logger = LoggerFactory.getLogger(TestSentinelController.class);

@Autowired
private StringRedisTemplate stringRedisTemplate;

/**
 * 测试节点挂了哨兵重新选举新的master节点，客户端是否能动态感知到
 * 新的master选举出来后，哨兵会把消息发布出去，客户端实际上是实现了一个消息监听机制，
 * 当哨兵把新master的消息发布出去，客户端会立马感知到新master的信息，从而动态切换访问的masterip
 *
 * @throws InterruptedException
 */
@RequestMapping("/test_sentinel")
public void testSentinel() throws InterruptedException {
    int i = 1;
    while (true){
        try {
            stringRedisTemplate.opsForValue().set("haibin"+i, i+"");
            System.out.println("设置key："+ "haibin" + i);
            i++;
            Thread.sleep(1000);
        }catch (Exception e){
            logger.error("错误：", e);
        }
    }
}
```

### 4、高可用模式

在redis3.0以前的版本要实现集群一般是借助哨兵sentinel工具来监控master节点的状态，如果master节点异常，则会做主从切换，将某一台slave作为master，哨兵的配置略微复杂，并且性能和高可用性等各方面表现一般，特别是在主从切换的瞬间存在访问瞬断的情况，而且哨兵模式只有一个主节点对外提供服务，没法支持很高的并发，且单个主节点内存也不宜设置得过大，否则会导致持久化文件过大，影响数据恢复或主从同步的效率

#### 4.1高可用架构

<img src="https://img04.sogoucdn.com/app/a/100540022/2021081216515292134471.png" style="zoom: 50%;" />

redis集群是一个由多个主从节点群组成的分布式服务器群，它具有复制、高可用和分片特性。Redis集群不需要sentinel哨兵·也能完成节点移除和故障转移的功能。需要将每个节点设置成集群模式，这种集群模式没有中心节点，可水平扩展，据官方文档称可以线性扩展到上万个节点(**官方推荐不超过1000个节点**)。redis集群的性能和高可用性均优于之前版本的哨兵模式，且集群配置非常简单

#### 4.2高可用架构搭建

redis集群需要至少三个master节点，我们这里搭建三个master节点，并且给每个master再搭建一个slave节点，总共6个redis节点，这里用三台机器部署6个redis实例，每台机器一主一从,分别为：192.168.159.171:8001,

192.168.159.171:8004,192.168.159.160:8002,192.168.159.160:8005,192.168.159.167:8003,192.168.159.167:8006。

1）192.168.159.171.8001配置

```java
（1）mkdir -p /usr/local/redis-cluster
（2）mkdir 8001

第一步：把之前的redis.conf配置文件copy到8001下，修改如下内容：
（1）daemonize yes
（2）port 8001（分别对每个机器的端口号进行设置）
（3）pidfile /var/run/redis_8001.pid  # 把pid进程号写入pidfile配置的文件
（4）dir /usr/local/redis-cluster/8001/（指定数据文件存放位置，必须要指定不同的目录位置，不然会丢失数据）
（5）cluster-enabled yes（启动集群模式）
（6）cluster-config-file nodes-8001.conf（集群节点信息文件，这里800x最好和port对应上）
（7）cluster-node-timeout 10000
(8)# bind 127.0.0.1（bind绑定的是自己机器网卡的ip，如果有多块网卡可以配多个ip，代表允许客户端通过机器的哪些网卡ip去访问，内网一般可以不配置bind，注释掉即可）
(9)protected-mode  no   （关闭保护模式）
(10)appendonly yes
#如果要设置密码需要增加如下配置：
(11)requirepass haibin     (设置redis访问密码)
(12)masterauth haibin      (设置集群节点间访问密码，跟上面一致)
```

2）192.168.159.171.8004配置

与1）配置文件类似，只需改下端口即可

3）192.168.159.160.8002配置

与1）配置文件类似，只需改下地址和端口即可

4）192.168.159.160.8005配置

与1）配置文件类似，只需改下地址和端口即可

5）192.168.159.167.8003配置

与1）配置文件类似，只需改下地址和端口即可

6）192.168.159.167.8006配置

与1）配置文件类似，只需改下地址和端口即可

7）分别启动6个redis实例

/shenhaibin/redis/redis-5.0.13/src/redis-server  /usr/local/redis-cluster/800*/redis.conf   

8)验证是否启动成功

ps -ef | grep redis

9)集群关联

src/redis-cli -a haibin --cluster create --cluster-replicas 1 192.168.159.171:8001 192.168.159.160:8002 192.168.159.167:8003 192.168.159.171:8004 192.168.159.160:8005 192.168.159.167:8006

10）验证集群

连接任意一个客户端：/src/redis-cli -a haibin-c -h 192.168.159.171 -p 8001

11)关闭集群（需要逐个进行关闭）

   src/redis-cli -a haibin-c -h 192.168.159.171 -p 800* shutdown              

#### 4.3通过客户端jedis进行连接

1）引入pom.xml文件

```java
<dependency>
	<groupId>redis.clients</groupId>
	<artifactId>jedis</artifactId>
	<version>2.9.0</version>
</dependency>
```

2）代码逻辑

```java
public class JedisClusterTest {
    public static void main(String[] args) throws IOException {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(20);
        jedisPoolConfig.setMaxIdle(10);
        jedisPoolConfig.setMinIdle(5);
        //高可用配置
        Set<HostAndPort> jedisClusterNode = new HashSet<HostAndPort>();
        jedisClusterNode.add(new HostAndPort("192.168.159.171",8001));
        jedisClusterNode.add(new HostAndPort("192.168.159.160",8002));
        jedisClusterNode.add(new HostAndPort("192.168.159.167",8003));
        jedisClusterNode.add(new HostAndPort("192.168.159.171",8004));
        jedisClusterNode.add(new HostAndPort("192.168.159.160",8005));
        jedisClusterNode.add(new HostAndPort("192.168.159.167",8006));
        JedisCluster jedisCluster = null;
        try{
            //connectionTimeout：指的是连接一个url的连接等待时间
            //soTimeout：指的是连接上一个url，获取response的返回等待时间
            jedisCluster = new JedisCluster(jedisClusterNode,6000,5000,10,"haibin",jedisPoolConfig);
            System.out.println(jedisCluster.set("cluster", "haibin"));
            System.out.println(jedisCluster.get("cluster"));
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            //注意这里不是关闭连接，在JedisPool模式下，Jedis会被归还给资源池
            if (jedisCluster  != null){
                jedisCluster .close();
            }
        }
    }
}
```

#### 4.4redis整合springboot进行连接

1)引入pom.xml文件

```java
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<dependency>
  <groupId>org.apache.commons</groupId>
  <artifactId>commons-pool2</artifactId>
</dependency>
```

2）核心配置文件

```java
server:
  port: 8080

spring:
  redis:
    database: 0
    timeout: 3000
    password: haibin
    cluster:
      nodes: 192.168.159.171:8001,192.168.159.160:8002,192.168.159.167:8003,192.168.159.171:8004,192.168.159.160:8005,192.168.159.167:8006
    lettuce:
      pool:
        max-idle: 50
        min-idle: 10
        max-active: 100
        max-wait: 1000
```

3)代码逻辑

```java
@RestController
public class TestClusterController {

    private static final Logger logger = LoggerFactory.getLogger(TestClusterController.class);

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @RequestMapping("/test_cluster")
    public void testCluster() throws InterruptedException {
        stringRedisTemplate.opsForValue().set("haibin", "666");
        System.out.println(stringRedisTemplate.opsForValue().get("haibin"));
    }

}
```

### 5、redis集群原理分析

Redis Cluster 将所有数据划分为 16384 个 slots(槽位)，每个节点负责其中一部分槽位。槽位的信息存储于每个节点中。当 Redis Cluster 的客户端来连接集群时，它也会得到一份集群的槽位配置信息并将其缓存在客户端本地。这样当客户端要查找某个 key 时，可以直接定位到目标节点。同时因为槽位的信息可能会存在客户端与服务器不一致的情况，还需要纠正机制来实现槽位信息的校验调整。

#### 5.1槽位定位算法

Cluster 默认会对 key 值使用 crc16 算法进行 hash 得到一个整数值，然后用这个整数值对 16384 进行取模来得到具体槽位。HASH_SLOT = CRC16(key) mod 16384

#### 5.2跳转重定位

当客户端向一个错误的节点发出了指令，该节点会发现指令的 key 所在的槽位并不归自己管理，这时它会向客户端发送一个特殊的跳转指令携带目标操作的节点地址，告诉客户端去连这个节点去获取数据。客户端收到指令后除了跳转到正确的节点上去操作，还会同步更新纠正本地的槽位映射表缓存，后续所有 key 将使用新的槽位映射表。

#### 5.3Redis集群节点间的通信机制

redis cluster节点间采取gossip协议进行通信 

- 维护集群的元数据(集群节点信息，主从角色，节点数量，各节点共享的数据等)有两种方式：集中式和gossip

  1）集中式：

  优点在于元数据的更新和读取，时效性非常好，一旦元数据出现变更立即就会更新到集中式的存储中，其他节点读取的时候立即就可以立即感知到；不足在于所有的元数据的更新压力全部集中在一个地方，可能导致元数据的存储压力。 很多中间件都会借助zookeeper集中式存储元数据。

  2）gossip：

  gossip协议包含多种消息，包括ping，pong，meet，fail等等。 

  meet：某个节点发送meet给新加入的节点，让新节点加入集群中，然后新节点就会开始与其他节点进行通信；

  ping：每个节点都会频繁给其他节点发送ping，其中包含自己的状态还有自己维护的集群元数据，互相通过ping交换元数据(类似自己感知到的集群节点增加和移除，hash slot信息等)； 

  pong: 对ping和meet消息的返回，包含自己的状态和其他信息，也可以用于信息广播和更新； 

  fail: 某个节点判断另一个节点fail之后，就发送fail给其他节点，通知其他节点，指定的节点宕机了。

  gossip协议的优点在于元数据的更新比较分散，不是集中在一个地方，更新请求会陆陆续续，打到所有节点上去更新，有一定的延时，降低了压力；缺点在于元数据更新有延时可能导致集群的一些操作会有一些滞后。

  **gossip通信的10000端口** 

  每个节点都有一个专门用于节点间gossip通信的端口，就是自己提供服务的端口号+10000，比如7001，那么用于节点间通信的就是17001端口。 每个节点每隔一段时间都会往另外几个节点发送ping消息，同时其他几点接收到ping消息之后返回pong消息。

#### 5.4网络抖动

真实世界的机房网络往往并不是风平浪静的，它们经常会发生各种各样的小问题。比如网络抖动就是非常常见的一种现象，突然之间部分连接变得不可访问，然后很快又恢复正常。

为解决这种问题，Redis Cluster 提供了一种选项cluster-node-timeout，表示当某个节点持续 timeout 的时间失联时，才可以认定该节点出现故障，需要进行主从切换。如果没有这个选项，网络抖动会导致主从频繁切换 (数据的重新复制)。

#### 5.5redis集群选举原理分析

当slave发现自己的master变为FAIL状态时，便尝试进行Failover，以期成为新的master。由于挂掉的master可能会有多个slave，从而存在多个slave竞争成为master节点的过程， 其过程如下：

1).slave发现自己的master变为FAIL

2).将自己记录的集群currentEpoch加1，并广播FAILOVER_AUTH_REQUEST 信息

3).其他节点收到该信息，只有master响应，判断请求者的合法性，并发送FAILOVER_AUTH_ACK，对每一个epoch只发送一次ack

4).尝试failover的slave收集master返回的FAILOVER_AUTH_ACK

5).slave收到超过半数master的ack后变成新Master(这里解释了集群为什么至少需要三个主节点，如果只有两个，当其中一个挂了，只剩一个主节点是不能选举成功的)

6).slave广播Pong消息通知其他集群节点。

从节点并不是在主节点一进入 FAIL 状态就马上尝试发起选举，而是有一定延迟，一定的延迟确保我们等待FAIL状态在集群中传播，slave如果立即尝试选举，其它masters或许尚未意识到FAIL状态，可能会拒绝投票

•延迟计算公式：

 DELAY = 500ms + random(0 ~ 500ms) + SLAVE_RANK * 1000ms

•SLAVE_RANK表示此slave已经从master复制数据的总量的rank。Rank越小代表已复制的数据越新。这种方式下，持有最新数据的slave将会首先发起选举（理论上）。

#### 5.6集群脑裂数据丢失问题

redis集群没有过半机制会有脑裂问题，网络分区导致脑裂后多个主节点对外提供写服务，一旦网络分区恢复，会将其中一个主节点变为从节点，这时会有大量数据丢失。

规避方法可以在redis配置里加上参数(这种方法不可能百分百避免数据丢失，参考集群leader选举机制)：

 min-replicas-to-write 1 //写数据成功最少同步的slave数量，这个数量可以模仿大于半数机制配置，比如集群总共三个节点可以配置1，加上leader就是2，超过了半数              

**注意**：这个配置在一定程度上会影响集群的可用性，比如slave要是少于1个，这个集群就算leader正常也不能提供服务了，需要具体场景权衡选择。

#### 5.7集群是否完整才能对外提供服务

当redis.conf的配置cluster-require-full-coverage为no时，表示当负责一个插槽的主库下线且没有相应的从库进行故障恢复时，集群仍然可用，如果为yes则集群不可用。

#### 5.8Redis集群为什么至少需要三个master节点，并且推荐节点数为奇数？

因为新master的选举需要大于半数的集群master节点同意才能选举成功，如果只有两个master节点，当其中一个挂了，是达不到选举新master的条件的。

 奇数个master节点可以在满足选举该条件的基础上节省一个节点，比如三个master节点和四个master节点的集群相比，大家如果都挂了一个master节点都能选举新master节点，如果都挂了两个master节点都没法选举新master节点了，所以奇数的master节点更多的是**从节省机器资源角度出发**说的。

#### 5.9Redis集群对批量操作命令的支持

对于类似mset，mget这样的多个key的原生批量操作命令，redis集群只支持所有key落在同一slot的情况，如果有多个key一定要用mset命令在redis集群上操作，则可以在key的前面加上{XX}，这样参数数据分片hash计算的只会是大括号里的值，这样能确保不同的key能落到同一slot里去，示例如下：

 mset {user1}:1:name haibin {user1}:1:age 18              

假设name和age计算的hash slot值不一样，但是这条命令在集群下执行，redis只会用大括号里的 user1 做hash slot计算，所以算出来的slot值肯定相同，最后都能落在同一slot。

#### 5.10哨兵leader选举流程

当一个master服务器被某sentinel视为下线状态后，该sentinel会与其他sentinel协商选出sentinel的leader进行故障转移工作。每个发现master服务器进入下线的sentinel都可以要求其他sentinel选自己为sentinel的leader，选举是先到先得。同时每个sentinel每次选举都会自增配置纪元(选举周期)，每个纪元中只会选择一个sentinel的leader。如果所有超过一半的sentinel选举某sentinel作为leader。之后该sentinel进行故障转移操作，从存活的slave中选举出新的master，这个选举过程跟集群的master选举很类似。

哨兵集群只有一个哨兵节点，redis的主从也能正常运行以及选举master，如果master挂了，那唯一的那个哨兵节点就是哨兵leader了，可以正常选举新master。

不过为了高可用一般都推荐至少部署三个哨兵节点。为什么推荐奇数个哨兵节点原理跟集群奇数个master节点类似。

### 6、StringRedisTemplate与RedisTemplate详解

spring 封装了 RedisTemplate 对象来进行对redis的各种操作，它支持所有的 redis 原生的 api。在RedisTemplate中提供了几个常用的接口方法的使用，分别是:

```java
private ValueOperations<K, V> valueOps;
private HashOperations<K, V> hashOps;
private ListOperations<K, V> listOps;
private SetOperations<K, V> setOps;
private ZSetOperations<K, V> zSetOps;
```

RedisTemplate中定义了对5种数据结构操作

```java
redisTemplate.opsForValue();//操作字符串
redisTemplate.opsForHash();//操作hash
redisTemplate.opsForList();//操作list
redisTemplate.opsForSet();//操作set
redisTemplate.opsForZSet();//操作有序set
```

StringRedisTemplate继承自RedisTemplate，也一样拥有上面这些操作。

StringRedisTemplate默认采用的是String的序列化策略，保存的key和value都是采用此策略序列化保存的。

RedisTemplate默认采用的是JDK的序列化策略，保存的key和value都是采用此策略序列化保存的。

**Redis客户端命令对应的RedisTemplate中的方法列表：**

| **String类型结构**                                         |                                                             |
| ---------------------------------------------------------- | ----------------------------------------------------------- |
| Redis                                                      | RedisTemplate rt                                            |
| set key value                                              | rt.opsForValue().set("key","value")                         |
| get key                                                    | rt.opsForValue().get("key")                                 |
| del key                                                    | rt.delete("key")                                            |
| strlen key                                                 | rt.opsForValue().size("key")                                |
| getset key value                                           | rt.opsForValue().getAndSet("key","value")                   |
| getrange key start end                                     | rt.opsForValue().get("key",start,end)                       |
| append key value                                           | rt.opsForValue().append("key","value")                      |
|                                                            |                                                             |
| **Hash结构**                                               |                                                             |
| hmset key field1 value1 field2 value2...                   | rt.opsForHash().putAll("key",map) //map是一个集合对象       |
| hset key field value                                       | rt.opsForHash().put("key","field","value")                  |
| hexists key field                                          | rt.opsForHash().hasKey("key","field")                       |
| hgetall key                                                | rt.opsForHash().entries("key") //返回Map对象                |
| hvals key                                                  | rt.opsForHash().values("key") //返回List对象                |
| hkeys key                                                  | rt.opsForHash().keys("key") //返回List对象                  |
| hmget key field1 field2...                                 | rt.opsForHash().multiGet("key",keyList)                     |
| hsetnx key field value                                     | rt.opsForHash().putIfAbsent("key","field","value"           |
| hdel key field1 field2                                     | rt.opsForHash().delete("key","field1","field2")             |
| hget key field                                             | rt.opsForHash().get("key","field")                          |
|                                                            |                                                             |
| **List结构**                                               |                                                             |
| lpush list node1 node2 node3...                            | rt.opsForList().leftPush("list","node")                     |
| rt.opsForList().leftPushAll("list",list) //list是集合对象  |                                                             |
| rpush list node1 node2 node3...                            | rt.opsForList().rightPush("list","node")                    |
| rt.opsForList().rightPushAll("list",list) //list是集合对象 |                                                             |
| lindex key index                                           | rt.opsForList().index("list", index)                        |
| llen key                                                   | rt.opsForList().size("key")                                 |
| lpop key                                                   | rt.opsForList().leftPop("key")                              |
| rpop key                                                   | rt.opsForList().rightPop("key")                             |
| lpushx list node                                           | rt.opsForList().leftPushIfPresent("list","node")            |
| rpushx list node                                           | rt.opsForList().rightPushIfPresent("list","node")           |
| lrange list start end                                      | rt.opsForList().range("list",start,end)                     |
| lrem list count value                                      | rt.opsForList().remove("list",count,"value")                |
| lset key index value                                       | rt.opsForList().set("list",index,"value")                   |
|                                                            |                                                             |
| **Set结构**                                                |                                                             |
| sadd key member1 member2...                                | rt.boundSetOps("key").add("member1","member2",...)          |
| rt.opsForSet().add("key", set) //set是一个集合对象         |                                                             |
| scard key                                                  | rt.opsForSet().size("key")                                  |
| sidff key1 key2                                            | rt.opsForSet().difference("key1","key2") //返回一个集合对象 |
| sinter key1 key2                                           | rt.opsForSet().intersect("key1","key2")//同上               |
| sunion key1 key2                                           | rt.opsForSet().union("key1","key2")//同上                   |
| sdiffstore des key1 key2                                   | rt.opsForSet().differenceAndStore("key1","key2","des")      |
| sinter des key1 key2                                       | rt.opsForSet().intersectAndStore("key1","key2","des")       |
| sunionstore des key1 key2                                  | rt.opsForSet().unionAndStore("key1","key2","des")           |
| sismember key member                                       | rt.opsForSet().isMember("key","member")                     |
| smembers key                                               | rt.opsForSet().members("key")                               |
| spop key                                                   | rt.opsForSet().pop("key")                                   |
| srandmember key count                                      | rt.opsForSet().randomMember("key",count)                    |
| srem key member1 member2...                                | rt.opsForSet().remove("key","member1","member2",...)        |

## 五、redis缓存设计与性能优化

<img src="https://img01.sogoucdn.com/app/a/100540022/2021081115580042262280.png" style="zoom: 50%;" />

### 1、缓存设计

#### 1.1 缓存穿透

缓存穿透是指查询一个根本不存在的数据，缓存层和存储层都不会命中，通常处于容错的考虑，如果从存储层查不到数据则不写入缓存层。缓存穿透将导致不存在的数据每次请求都要到存储层去查询，失去了缓存保护后端存储的意义。

造成缓存穿透的基本原因有两个:

第一，自身业务代码或者数据从出现问题。

第二，一些恶意攻击、爬虫等造成大量空命中。

缓存穿透问题解决方案：

1）、缓存空对象

```java
String get(String key) {
    // 从缓存中获取数据
    String cacheValue = cache.get(key);
    // 缓存为空
    if (StringUtils.isBlank(cacheValue)) {
        // 从存储中获取
        String storageValue = storage.get(key);
        cache.set(key, storageValue);
        // 如果存储数据为空， 需要设置一个过期时间(300秒)
        if (storageValue == null) {
            cache.expire(key, 60 * 5);
        }
        return storageValue;
    } else {
        // 缓存非空
        return cacheValue;
    }
}
```

2）布隆过滤器

对于恶意攻击，向服务器请求大量不存在的数据造成的缓存穿透，还可以使用布隆过滤器先做一次过滤，对于不存在的数据布隆过滤器一般都能够过滤掉，不让请求再往后端发送。当布隆过滤器说某个值存在时，这个值可能不存在；当它说不存在时，那就肯定不存在。

<img src="https://img03.sogoucdn.com/app/a/100540022/2021081116214205246208.png" style="zoom:80%;" />

布隆过滤器就是一个大型的位数组和几个不一样的无偏hash函数。所谓无偏就是能够把元素的hash值算得比较均匀。

向布隆过滤器中添加key时，会使用多个hash函数对key进行hash算得一个整型索引值然后对位数组长度进行取模运算得到一个位置，每个hash函数都会算得一个不同得的位置。再把位数组的这几个位置都置为1就完成了add操作。

向布隆过滤器询问key是否存在时，跟add一样，也会把hash的几个位置都算出来，看看位数组中这几个位置是否都为1，只要有一个为0，那么说明布隆过滤器中这个key不存在。如果都是1，这并不能说明这个key就一定存在，只是极有可能存在，因为这些位置为1可能是因为其它的key存在所致。如果这个位数组比较稀疏，这个概率就很大，如果这个位数组比较拥挤，这个概率就会降低。

这种方法适用于数据命中不高、数据相对固定、实时性（通常是数据集较大）的应用场景，代码维护较为复杂，但是缓存空间占用很少。

可以用redisson实现布隆过滤器，引入依赖：

```java
<dependency>
	<groupId>org.redisson</groupId>
	<artifactId>redisson</artifactId>
	<version>3.6.5</version>
</dependency>
```

伪代码示例：

```java
public class RedissonBloomFilter {
    public static void main(String[] args) {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://localhost:6379");
        //构造Redisson
        RedissonClient redisson = Redisson.create(config);

        RBloomFilter<String> bloomFilter = redisson.getBloomFilter("nameList");
        //初始化布隆过滤器：预计元素为100000000L,误差率为3%,根据这两个参数会计算出底层的bit数组大小
        bloomFilter.tryInit(100000000L,0.03);
        //将zhuge插入到布隆过滤器中
        bloomFilter.add("haibin");

        //判断下面号码是否在布隆过滤器中
        System.out.println(bloomFilter.contains("a"));//false
        System.out.println(bloomFilter.contains("b"));//false
        System.out.println(bloomFilter.contains("haibin"));//true
    }
}
```

使用布隆过滤器需要把所有数据提前放入布隆过滤器，并且在增加数据时也要往布隆过滤器里放，布隆过滤器缓存过滤伪代码：

```java
//初始化布隆过滤器
RBloomFilter<String> bloomFilter = redisson.getBloomFilter("nameList");
//初始化布隆过滤器：预计元素为100000000L,误差率为3%
bloomFilter.tryInit(100000000L,0.03);

//把所有数据存入布隆过滤器
void init(){
  for (String key: keys) {
     bloomFilter.put(key);
  }
}

 String get(String key) {
   // 从布隆过滤器这一级缓存判断下key是否存在
   Boolean exist = bloomFilter.contains(key);
   if(!exist){
      return "";
    }
   // 从缓存中获取数据
  String cacheValue = cache.get(key);
  // 缓存为空
  if (StringUtils.isBlank(cacheValue)) {
      // 从存储中获取
      String storageValue = storage.get(key);
      cache.set(key, storageValue);
      // 如果存储数据为空， 需要设置一个过期时间(300秒)
      if (storageValue == null) {
        cache.expire(key, 60 * 5);
       }
      return storageValue;
      } else {
      // 缓存非空
      return cacheValue;
      }
  }
```

#### 1.2 缓存失效（击穿）

由于大批量缓存在同一时间失效可能导致大量请求同时穿透缓存直达数据库，可能会造成数据库瞬间压力过大甚至挂掉，对于这种情况我们在批量增加缓存时最好将这一批数据的缓存过期时间设置为一个时间段内的不同时间。

示例伪代码：

```java
String get(String key) {
    // 从缓存中获取数据
    String cacheValue = cache.get(key);
    // 缓存为空
    if (StringUtils.isBlank(cacheValue)) {
        // 从存储中获取
        String storageValue = storage.get(key);
        cache.set(key, storageValue);
        //设置一个过期时间(300到600之间的一个随机数)
        int expireTime = new Random().nextInt(300)  + 300;
        if (storageValue == null) {
            cache.expire(key, expireTime);
        }
        return storageValue;
    } else {
        // 缓存非空
        return cacheValue;
    }
}
```

#### 1.3 缓存雪崩

缓存雪崩指的是缓存层支撑不住或宕掉后， 流量会像奔逃的野牛一样， 打向后端存储层。

由于缓存层承载着大量请求， 有效地保护了存储层， 但是如果缓存层由于某些原因不能提供服务(比如超大并发过来，缓存层支撑不住，或者由于缓存设计不好，类似大量请求访问bigkey，导致缓存能支撑的并发急剧下降)， 于是大量请求都会打到存储层， 存储层的调用量会暴增， 造成存储层也会级联宕机的情况。 

预防和解决缓存雪崩问题， 可以从以下三个方面进行着手。

1） 保证缓存层服务高可用性，比如使用Redis Sentinel或Redis Cluster。

2） 依赖隔离组件为后端限流熔断并降级。比如使用Sentinel或Hystrix限流降级组件。

比如服务降级，我们可以针对不同的数据采取不同的处理方式。当业务应用访问的是非核心数据（例如电商商品属性，用户信息等）时，暂时停止从缓存中查询这些数据，而是直接返回预定义的默认降级信息、空值或是错误提示信息；当业务应用访问的是核心数据（例如电商商品库存）时，仍然允许查询缓存，如果缓存缺失，也可以继续通过数据库读取。

3） 提前演练。 在项目上线前， 演练缓存层宕掉后， 应用以及后端的负载情况以及可能出现的问题， 在此基础上做一些预案设定。

#### 1.4 热点缓存key重优化

开发人员使用“缓存+过期时间”的策略既可以加速数据读写， 又保证数据的定期更新， 这种模式基本能够满足绝大部分需求。 但是有两个问题如果同时出现， 可能就会对应用造成致命的危害：

- 当前key是一个热点key（例如一个热门的娱乐新闻），并发量非常大。
- 重建缓存不能在短时间完成， 可能是一个复杂计算， 例如复杂的SQL、 多次IO、 多个依赖等。

在缓存失效的瞬间， 有大量线程来重建缓存， 造成后端负载加大， 甚至可能会让应用崩溃。

要解决这个问题主要就是要避免大量线程同时重建缓存。

我们可以利用互斥锁来解决，此方法只允许一个线程重建缓存， 其他线程等待重建缓存的线程执行完， 重新从缓存获取数据即可。

示例伪代码：

```java
String get(String key) {
    // 从Redis中获取数据
    String value = redis.get(key);
    // 如果value为空， 则开始重构缓存
    if (value == null) {
        // 只允许一个线程重建缓存， 使用nx， 并设置过期时间ex
        String mutexKey = "mutext:key:" + key;
        if (redis.set(mutexKey, "1", "ex 180", "nx")) {
            // 从数据源获取数据
            value = db.get(key);
            // 回写Redis， 并设置过期时间
            redis.setex(key, timeout, value);
            // 删除key_mutex
            redis.delete(mutexKey);
        }// 其他线程休息50毫秒后重试
        else {
            Thread.sleep(50);
            get(key);
        }
    }
    return value;
}
```

#### 1.5 缓存与数据库双写不一致

在大并发下，同时操作数据库与缓存会存在数据不一致性问题

1）双写不一致情况

<img src="https://img04.sogoucdn.com/app/a/100540022/2021081116372406376426.png" style="zoom: 67%;" />

2)读写并发不一致

<img src="https://img03.sogoucdn.com/app/a/100540022/2021081116372464416361.png" style="zoom: 50%;" />

3)解决方案

1、对于并发几率很小的数据(如个人维度的订单数据、用户数据等)，这种几乎不用考虑这个问题，很少会发生缓存不一致，可以给缓存数据加上过期时间，每隔一段时间触发读的主动更新即可。

2、就算并发很高，如果业务上能容忍短时间的缓存数据不一致(如商品名称，商品分类菜单等)，缓存加上过期时间依然可以解决大部分业务对于缓存的要求。

3、如果不能容忍缓存数据不一致，可以通过加**读写锁**保证并发读写或写写的时候按顺序排好队，**读读的时候相当于无锁**。

4、也可以用阿里开源的canal通过监听数据库的binlog日志及时的去修改缓存，但是引入了新的中间件，增加了系统的复杂度。

<img src="https://img02.sogoucdn.com/app/a/100540022/2021081116372422482657.png" style="zoom:67%;" />

4)总结

以上我们针对的都是**读多写少**的情况加入缓存提高性能，如果**写多读多**的情况又不能容忍缓存数据不一致，那就没必要加缓存了，可以直接操作数据库。放入缓存的数据应该是对实时性、一致性要求不是很高的数据。切记不要为了用缓存，同时又要保证绝对的一致性做大量的过度设计和控制，增加系统复杂性！

### 2、开发规范与性能优化

#### 2.1、键值设计

##### 2.1.1. key名设计

- (1)【建议】: 可读性和可管理性

以业务名(或数据库名)为前缀(防止key冲突)，用冒号分隔，比如业务名:表名:id

​                trade:order:1              

- (2)【建议】：简洁性

保证语义的前提下，控制key的长度，当key较多时，内存占用也不容忽视，例如：

​                user:{uid}:friends:messages:{mid} 简化为 u:{uid}:fr:m:{mid}              

- (3)【强制】：不要包含特殊字符

反例：包含空格、换行、单双引号以及其他转义字符

##### 2.1.2 value设计

- (1)【强制】：拒绝bigkey(防止网卡流量、慢查询)

在Redis中，一个字符串最大512MB，一个二级数据结构（例如hash、list、set、zset）可以存储大约40亿个(2^32-1)个元素，但实际中如果下面两种情况，我就会认为它是bigkey。

1. 字符串类型：它的big体现在单个value值很大，一般认为超过10KB就是bigkey。
2. 非字符串类型：哈希、列表、集合、有序集合，它们的big体现在元素个数太多。

一般来说，string类型控制在10KB以内，hash、list、set、zset元素个数不要超过5000。

反例：一个包含200万个元素的list。

非字符串的bigkey，不要使用del删除，使用hscan、sscan、zscan方式渐进式删除，同时要注意防止bigkey过期时间自动删除问题(例如一个200万的zset设置1小时过期，会触发del操作，造成阻塞）

**bigkey的危害：**

1.导致redis阻塞

2.网络拥塞

bigkey也就意味着每次获取要产生的网络流量较大，假设一个bigkey为1MB，客户端每秒访问量为1000，那么每秒产生1000MB的流量，对于普通的千兆网卡(按照字节算是128MB/s)的服务器来说简直是灭顶之灾，而且一般服务器会采用单机多实例的方式来部署，也就是说一个bigkey可能会对其他实例也造成影响，其后果不堪设想。

\3. 过期删除

有个bigkey，它安分守己（只执行简单的命令，例如hget、lpop、zscore等），但它设置了过期时间，当它过期后，会被删除，如果没有使用Redis 4.0的过期异步删除(**lazyfree-lazy-expire yes**)，就会存在阻塞Redis的可能性。

**bigkey的产生：**

一般来说，bigkey的产生都是由于程序设计不当，或者对于数据规模预料不清楚造成的，来看几个例子：

(1) 社交类：粉丝列表，如果某些明星或者大v不精心设计下，必是bigkey。

(2) 统计类：例如按天存储某项功能或者网站的用户集合，除非没几个人用，否则必是bigkey。

(3) 缓存类：将数据从数据库load出来序列化放到Redis里，这个方式非常常用，但有两个地方需要注意，第一，是不是有必要把所有字段都缓存；第二，有没有相关关联的数据，有的同学为了图方便把相关数据都存一个key下，产生bigkey。

**如何优化bigkey**

\1. 拆

big list： list1、list2、...listN

big hash：可以讲数据分段存储，比如一个大的key，假设存了1百万的用户数据，可以拆分成200个key，每个key下面存放5000个用户数据

\2. 如果bigkey不可避免，也要思考一下要不要每次把所有元素都取出来(例如有时候仅仅需要hmget，而不是hgetall)，删除也是一样，尽量使用优雅的方式来处理。

- (2)【推荐】：选择适合的数据类型。

例如：实体类型(要合理控制和使用数据结构，但也要注意节省内存和性能之间的平衡)

反例：

​                set user:1:name tom set user:1:age 19 set user:1:favor football              

正例:

​                hmset user:1 name tom age 19 favor football              

3.【推荐】：控制key的生命周期，redis不是垃圾桶。

建议使用expire设置过期时间(条件允许可以打散过期时间，防止集中过期)。

#### 2.2、命令使用

1.【推荐】 O(N)命令关注N的数量

例如hgetall、lrange、smembers、zrange、sinter等并非不能使用，但是需要明确N的值。有遍历的需求可以使用hscan、sscan、zscan代替。

2.【推荐】：禁用命令

禁止线上使用keys、flushall、flushdb等，通过redis的rename机制禁掉命令，或者使用scan的方式渐进式处理。

3.【推荐】合理使用select

redis的多数据库较弱，使用数字进行区分，很多客户端支持较差，同时多业务用多数据库实际还是单线程处理，会有干扰。

4.【推荐】使用批量操作提高效率

​                原生命令：例如mget、mset。 非原生命令：可以使用pipeline提高效率。              

但要注意控制一次批量操作的元素个数(例如500以内，实际也和元素字节数有关)。

注意两者不同：

​                \1. 原生命令是原子操作，pipeline是非原子操作。 2. pipeline可以打包不同的命令，原生命令做不到 3. pipeline需要客户端和服务端同时支持。              

5.【建议】Redis事务功能较弱，不建议过多使用，可以用lua替代

#### 2.3、客户端使用

1.【推荐】

避免多个应用使用一个Redis实例

正例：不相干的业务拆分，公共数据做服务化。

2.【推荐】

使用带有连接池的数据库，可以有效控制连接，同时提高效率，标准使用方式：          

```
JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
jedisPoolConfig.setMaxTotal(5);
        jedisPoolConfig.setMaxIdle(2);
        jedisPoolConfig.setTestOnBorrow(true);

        JedisPool jedisPool = new JedisPool(jedisPoolConfig, "192.168.159.171", 6379, 3000, null);

        Jedis jedis = null;
        try {
        jedis = jedisPool.getResource();
        //具体的命令
        jedis.executeCommand()
        } catch (Exception e) {
        logger.error("op key {} error: " + e.getMessage(), key, e);
        } finally {
        //注意这里不是关闭连接，在JedisPool模式下，Jedis会被归还给资源池。
        if (jedis != null)
        jedis.close();
        }
```

连接池参数含义：

| 序号 | 参数名             | 含义                                                         | 默认值           | 使用建议                                          |
| ---- | ------------------ | ------------------------------------------------------------ | ---------------- | ------------------------------------------------- |
| 1    | maxTotal           | 资源池中最大连接数                                           | 8                | 设置建议见下面                                    |
| 2    | maxIdle            | 资源池允许最大空闲的连接数                                   | 8                | 设置建议见下面                                    |
| 3    | minIdle            | 资源池确保最少空闲的连接数                                   | 0                | 设置建议见下面                                    |
| 4    | blockWhenExhausted | 当资源池用尽后，调用者是否要等待。只有当为true时，下面的maxWaitMillis才会生效 | true             | 建议使用默认值                                    |
| 5    | maxWaitMillis      | 当资源池连接用尽后，调用者的最大等待时间(单位为毫秒)         | -1：表示永不超时 | 不建议使用默认值                                  |
| 6    | testOnBorrow       | 向资源池借用连接时是否做连接有效性检测(ping)，无效连接会被移除 | false            | 业务量很大时候建议设置为false(多一次ping的开销)。 |
| 7    | testOnReturn       | 向资源池归还连接时是否做连接有效性检测(ping)，无效连接会被移除 | false            | 业务量很大时候建议设置为false(多一次ping的开销)。 |
| 8    | jmxEnabled         | 是否开启jmx监控，可用于监控                                  | true             | 建议开启，但应用本身也要开启                      |

**优化建议：**

1）**maxTotal**：最大连接数，早期的版本叫maxActive

实际上这个是一个很难回答的问题，考虑的因素比较多：

- 业务希望Redis并发量
- 客户端执行命令时间
- Redis资源：例如 nodes(例如应用个数) * maxTotal 是不能超过redis的最大连接数maxclients。
- 资源开销：例如虽然希望控制**空闲连接**(连接池此刻可马上使用的连接)，但是不希望因为连接池的频繁释放创建连接造成不必靠开销。

**以一个例子说明**，假设:

- 一次命令时间（borrow|return resource + Jedis执行命令(含网络) ）的平均耗时约为1ms，一个连接的QPS大约是1000
- 业务期望的QPS是50000

那么理论上需要的资源池大小是50000 / 1000 = 50个。但事实上这是个理论值，还要考虑到要比理论值预留一些资源，通常来讲maxTotal可以比理论值大一些。

但这个值不是越大越好，一方面连接太多占用客户端和服务端资源，另一方面对于Redis这种高QPS的服务器，一个大命令的阻塞即使设置再大资源池仍然会无济于事。

2）**maxIdle和minIdle**

maxIdle实际上才是业务需要的最大连接数，maxTotal是为了**给出余量**，所以maxIdle不要设置过小，否则会有new Jedis(新连接)开销。

**连接池的最佳性能是maxTotal = maxIdle**，这样就避免连接池伸缩带来的性能干扰。但是如果并发量不大或者maxTotal设置过高，会导致不必要的连接资源浪费。一般推荐maxIdle可以设置为按上面的业务期望QPS计算出来的理论连接数，maxTotal可以再放大一倍。

minIdle（最小空闲连接数），与其说是最小空闲连接数，不如说是"**至少需要保持的空闲连接数**"，在使用连接的过程中，如果连接数超过了minIdle，那么继续建立连接，如果超过了maxIdle，当超过的连接执行完业务后会慢慢被移出连接池释放掉。

如果系统启动完马上就会有很多的请求过来，那么可以给redis连接池做**预热**，比如快速的创建一些redis连接，执行简单命令，类似ping()，快速的将连接池里的空闲连接提升到minIdle的数量。

**连接池预热**示例代码：

```java
List<Jedis> minIdleJedisList = new ArrayList<Jedis>(jedisPoolConfig.getMinIdle());

for (int i = 0; i < jedisPoolConfig.getMinIdle(); i++) {
        Jedis jedis = null;
        try {
        jedis = pool.getResource();
        minIdleJedisList.add(jedis);
        jedis.ping();
        } catch (Exception e) {
        logger.error(e.getMessage(), e);
        } finally {
        //注意，这里不能马上close将连接还回连接池，否则最后连接池里只会建立1个连接。。
        //jedis.close();
        }
        }
//统一将预热的连接还回连接池
        for (int i = 0; i < jedisPoolConfig.getMinIdle(); i++) {
        Jedis jedis = null;
        try {
        jedis = minIdleJedisList.get(i);
        //将连接归还回连接池
        jedis.close();
        } catch (Exception e) {
        logger.error(e.getMessage(), e);
        } finally {
        }
        } 
```

总之，要根据实际系统的QPS和调用redis客户端的规模整体评估每个节点所使用的连接池大小。

3.【建议】

高并发下建议客户端添加熔断功能(例如sentinel、hystrix)

4.【推荐】

设置合理的密码，如有必要可以使用SSL加密访问

5.【建议】

**Redis对于过期键有三种清除策略：**

1. 被动删除：当读/写一个已经过期的key时，会触发惰性删除策略，直接删除掉这个过期key
2. 主动删除：由于惰性删除策略无法保证冷数据被及时删掉，所以Redis会定期主动淘汰一批**已过期**的key
3. 当前已用内存超过maxmemory限定时，触发**主动清理策略**

**主动清理策略**在Redis 4.0 之前一共实现了 6 种内存淘汰策略，在 4.0 之后，又增加了 2 种策略，总共8种：

**a) 针对设置了过期时间的key做处理：**

1. volatile-ttl：在筛选时，会针对设置了过期时间的键值对，根据过期时间的先后进行删除，越早过期的越先被删除。
2. volatile-random：就像它的名称一样，在设置了过期时间的键值对中，进行随机删除。
3. volatile-lru：会使用 LRU 算法筛选设置了过期时间的键值对删除。
4. volatile-lfu：会使用 LFU 算法筛选设置了过期时间的键值对删除。

**b) 针对所有的key做处理：**

1. allkeys-random：从所有键值对中随机选择并删除数据。
2. allkeys-lru：使用 LRU 算法在所有数据中进行筛选删除。
3. allkeys-lfu：使用 LFU 算法在所有数据中进行筛选删除。

**c) 不处理：**

1. noeviction：不会剔除任何数据，拒绝所有写入操作并返回客户端错误信息"(error) OOM command not allowed when used memory"，此时Redis只响应读操作。

**LRU 算法****（Least Recently Used，最近最少使用）**

淘汰很久没被访问过的数据，以**最近一次访问时间**作为参考。

**LFU 算法****（Least Frequently Used，最不经常使用）**

淘汰最近一段时间被访问次数最少的数据，以**次数**作为参考。

当存在热点数据时，LRU的效率很好，但偶发性的、周期性的批量操作会导致LRU命中率急剧下降，缓存污染情况比较严重。这时使用LFU可能更好点。

根据自身业务类型，配置好maxmemory-policy(默认是noeviction)，推荐使用volatile-lru。如果不设置最大内存，当 Redis 内存超出物理内存限制时，内存的数据会开始和磁盘产生频繁的交换 (swap)，会让 Redis 的性能急剧下降。当Redis运行在主从模式时，只有主结点才会执行过期删除策略，然后把删除操作”del key”同步到从结点删除数据。

## 六、redis架构设计及底层原理

### 1、redis的key的底层设计

redis底层的key采用SDS（simple dynamic string）进行设计

sds:

free:0

len: 6

char buf[] = "haibin"

如果此时将"haibin"修改为"haibin123"，那么长度就会扩容为（len + addlen）* 2 = 18,free就是9

1）二进制安全的数据结构

2）提供了内存预分配机制，避免了频繁的内存分配

3）兼容c语言的函数库
