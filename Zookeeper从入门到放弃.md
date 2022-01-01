# Zookeeper从入门到放弃

## 一、zookeeper的特性与节点数据类型详解

### 1.什么是zookeeper

官方文档上这么解释zookeeper，它是一个分布式协调框架，是Apache Hadoop 的一个子项目，它主要是用来解决分布式应用中经常遇到的一些数据管理问题，如：统一命名服务、状态同步服务、集群管理、分布式应用配置项的管理等。

<img src="https://img01.sogoucdn.com/app/a/100540022/2021081513414324293008.png" style="zoom:50%;" />

### 2.zookeeper核心概念

zookeeper主要有两个核心概念：文件系统数据结构和监听通知机制。

#### 2.1文件系统数据结构

zookeeper本质就是跟文件系统一样，是一个树形结构：

<img src="https://img01.sogoucdn.com/app/a/100540022/2021081513565052008358.jpg" style="zoom:50%;" />

每个子目录项都被称作为znode(目录节点)，和文件系统类似，我们能够自由的增加、删除znode,在一个znode下增加、删除子znode.

1）PERSISTENT-持久化目录节点

客户端与zookeeper断开连接后，该节点依旧存在，只要不手动删除该节点，他将永远存在 

2）PERSISTENT-SEQUENTIAL-持久化顺序编号目录节点

客户端与zookeeper断开连接后，该节点依旧存在，只是zookeeper给该节点名称进行顺序编号

3）EPHEMERAL-临时目录节点

客户端与zookeeper断开连接后，该节点被删除

4）EPHEMERAL-SEQUENTIAL-临时顺序编号目录节点

客户端与zookeeper断开连接后，该节点被删除，只是zookeeper给该节点进行顺序编号

5）Container节点

这个Container节点是是3.5.3版本新增，如果Container节点下面没有子节点，则Container节点在未来会被Zookeeper自动清除，定时任务默认60s检查一次

6）TTL节点

默认禁用，只能通过系统配置zookeeper.extendedTypesEnabled=true开启，不稳定

<img src="https://img04.sogoucdn.com/app/a/100540022/2021081514231924612423.png" style="zoom:50%;" />

#### 2.2监听通知机制

客户端注册监听它关心的任意节点，或者目录节点及递归子目录节点

1）如果注册的是对某个节点的监听，则当这个节点被删除，或者被修改时，对应的客户端将被通知

2）如果注册的是对某个目录的监听，则当这个目录有子节点被创建，或者有子节点被删除，对应的客户端将被通知

3）如果注册的是对某个目录的递归子节点进行监听，则当这个目录下面的任意子节点有目录结构的变化（有子节点被创建，或被删除）或者根节点有数据变化时，对应的客户端将被通知。

注意：所有的通知都是一次性的，及无论是对节点还是对目录进行监听，一旦触发，对应的监听即被移除。递归子节点，监听是对所有子节点的，每个子节点下面的事件同样只会被触发一次。

#### 2.3zookeeper经典的应用场景

1）分布式配置中心

2）分布式注册中心

3）分布式锁

4）分布式队列

5）集群选举

6）分布式屏障

7）发布/订阅

### 3.zookeeper实操

#### 3.1zookeeper安装

1）安装jdk及配置JAVA环境，因为zookeeper是java开发的，验证是否配置成功：

java -version

2）下载zookeeper并且上传到服务器

tar -zxvf apache-zookeeper-3.5.9-bin.tar.gz

cd apache-zookeeper-3.5.9-bin

3）重命名配置文件zoo_sample.cfg

cp zoo_sample.cfg zoo.cfg

4）启动zookeeper

#可以通过 bin/zkServer.sh 来查看都支持哪些参数

bin/zkServer.sh start conf/zoo.cfg

5）连接服务器

bin/zkCli.sh -server ip:port

#### 3.2使用命令行操作zookeeper

在连接成功服务器上的客户端输入help查看zookeeper所支持的所有命令：

<img src="https://img03.sogoucdn.com/app/a/100540022/2021081521082568868882.png" style="zoom: 50%;" />

1）创建zookeeper节点命令

create [-s] [-e] [-c] [-t ttl] path [data] [acl]

上面[]为可选项，没有则默认创建持久化节点

-s:顺序节点

-e:临时节点

-c:容器节点

-t:可以给节点添加过期时间，默认禁用，需要通过系统参数启用

（-D*zookeeper.extendedTypesEnabled=true*,  *znode.container.checkIntervalMs* : (Java system property only) **New in 3.5.1:** The time interval in milliseconds for each check of candidate container and ttl nodes. Default is "60000".)

创建节点：create /name haibin(这里没有添加任何参数，创建的就是持久化节点)

查看节点：get /name

修改节点数据：set /name haibin-changed

查看节点状态信息：stat /name

<img src="https://img02.sogoucdn.com/app/a/100540022/2021081521213844884462.png"  />

cZxid：创建znode的事务ID(Zxid的值)

ctime：znode创建时间

mZxid：最后修改znode的事务ID

mtime：znode最近修改时间

pZxid：最后添加或删除子节点的事务ID(子节点列表发生变化才会发生改变)

cversion：znode的子节点结果集版本（一个节点的子节点增加、删除都会影响这个版本）

dataVersion：znode的当前数据版本

aclVersion：表示对此znode的acl版本

ephemeralOwner：znode是临时znode时，表示znode所有者的session ID。如果znode不是临时znode,则该字段设置为零。

dataLength：znode数据字段的长度

numChildren：znode的子znode的数量

查看节点状态信息同时查看数据：get -s /name

![](https://img03.sogoucdn.com/app/a/100540022/2021081521400084984600.png)

根据上面的dataVersion数据版本号来实现乐观锁的功能（有无并发情况）

/name当前的数据版本是1，这时客户端用set命令修改数据的时候可以带上版本号：set -v 1 /name changed

如果在执行上面set命令前，有人修改了数据，zookeeper会递增版本号，这个时候，如果再用以前的版本号去修改，将会导致修改失败，报一下错误：

![](https://img04.sogoucdn.com/app/a/100540022/2021081521462706282652.png)

创建子节点：create /name/name-sub-node test

递归查看节点：ls -R /name

创建临时节点：create -e /ephemeral test(create后面跟一个-e创建临时节点，临时节点不能创建子节点)

![](https://img02.sogoucdn.com/app/a/100540022/2021081521542612762197.png)

创建序号节点，加参数-s：create /se-parent data	//创建父目录，单纯为了分类，非必须

​									create -s /seq-parent data //创建序号节点。顺序节点再将seq-parent目录下面，顺序递增

![](https://img03.sogoucdn.com/app/a/100540022/2021081521595366488860.png)

创建临时顺序节点： create -s -e  /ephemeral-node/前缀-          

创建容器节点：create -c /container(容器节点主要用来容纳字节点，如果没有给其创建子节点，容器节点表现和持久化节点一样，如果给容器节点创建了子节点，后续又把子节点清空，容器节点也会被zookeeper删除。)    

2）事件监听机制

针对节点的监听：一旦事件触发，对应的注册立刻被移除，所以事件监听是一次性的

get -w /path		//注册监听的同时获取数据

stat -w /path		//对节点进行监听，且获取元数据信息

![](https://img01.sogoucdn.com/app/a/100540022/2021081608495028818223.png)

针对目录的监听，目录的变化，会触发事件，且一旦触发，对应的监听也会被移除，后续对节点的创建没有触发监听事件：ls -w /path

<img src="https://img04.sogoucdn.com/app/a/100540022/2021081608495048180049.png" style="zoom:80%;" />

针对递归子目录的监听：ls -R -w /path(-R区分大小写，一定用大写)

如下对/name 节点进行递归监听，但是每个目录下的目录监听也是一次性的，如第一次在/name 目录下创建节点时，触发监听事件，第二次则没有，同样，因为是递归的目录监听，所以在/name/sub0下进行节点创建时，触发事件，但是再次创建/name/sub0/subsub1节点时，没有触发事件。

![](https://img03.sogoucdn.com/app/a/100540022/2021081608495070212442.png)

Zookeeper事件类型：

​	None:连接建立事件

​	NodeCreated:节点创建

​	NodeDeleted:节点删除

​	NodeDataChanged:节点数据变化

​	NodeChildrenChanged:子节点列表变化

​	DataWatchRemoved:节点监听被移除

​	ChildWatchRemoved:子节点监听被移除

### 4.zookeeper ACLs权限控制

Zookeeper的ACL权限控制，可以控制节点的读写操作，保证数据的安全性，Zookeeper权限设置分为3部分组成，分别是：权限模式（Schema）、授权对象（ID）、权限信息（Permission）。最终组成一条例如“scheme:id:permission”格式的 ACL 请求信息。下面我们具体看一下这 3 部分代表什么意思：

1）权限模式（Schema）:用来设置Zookeeper服务器进行权限验证的方式。ZooKeeper 的权限验证方式大体分为两种类型：

一种是范围验证：所谓的范围验证就是说 ZooKeeper 可以针对一个 IP 或者一段 IP 地址授予某种权限。比如我们可以让一个 IP 地址为“ip：192.168.0.110”的机器对服务器上的某个数据节点具有写入的权限。或者也可以通过“ip:192.168.0.1/24”给一段 IP 地址的机器赋权。

另一种是口令验证：也可以理解为用户名密码的方式。在 ZooKeeper 中这种验证方式是 Digest 认证，而 Digest 这种认证方式首先在客户端传送“username:password”这种形式的权限表示符后，ZooKeeper 服务端会对密码 部分使用 SHA-1 和 BASE64 算法进行加密，以保证安全性。

还有一种Super权限模式,  Super可以认为是一种特殊的 Digest 认证。具有 Super 权限的客户端可以对 ZooKeeper 上的任意数据节点进行任意操作。

2）授权对象（ID）

授权对象就是说我们要把权限赋予谁，而对应于 4 种不同的权限模式来说，如果我们选择采用 IP 方式，使用的授权对象可以是一个 IP 地址或 IP 地址段；而如果使用 Digest 或 Super 方式，则对应于一个用户名。如果是 World 模式，是授权系统中所有的用户。

3）权限信息（Permission）

权限就是指我们可以在数据节点上执行的操作种类，如下所示：在 ZooKeeper 中已经定义好的权限有 5 种：

数据节点（c: create）创建权限，授予权限的对象可以在数据节点下创建子节点；

数据节点（w: wirte）更新权限，授予权限的对象可以更新该数据节点；

数据节点（r: read）读取权限，授予权限的对象可以读取该节点的内容以及子节点的列表信息；

数据节点（d: delete）删除权限，授予权限的对象可以删除该数据节点的子节点；

数据节点（a: admin）管理者权限，授予权限的对象可以对该数据节点体进行 ACL 权限设置。

**命令**：

getAcl：获取某个节点的acl权限信息

setAcl：设置某个节点的acl权限信息

addauth: 输入认证授权信息，相当于注册用户信息，注册时输入明文密码，zk将以密文的形式存储 

可以通过系统参数zookeeper.skipACL=yes进行配置，默认是no,可以配置为true, 则配置过的ACL将不再进行权限检测

4）生成授权ID的两种方式：

a.代码生成ID:

```java
public static void generateSuperDigest() throws NoSuchAlgorithmException {
    String sId = DigestAuthenticationProvider.generateDigest("gj:test");
    System.out.println(sId);//  gj:X/NSthOB0fD/OT6iilJ55WJVado=
}
```

b.在xshell中生成

```xshell
echo -n <user>:<password> | openssl dgst -binary -sha1 | openssl base64
```

5）设置acl

设置ACL有两种方式：节点创建得时候设置ACL或者用setAcl设置

```zookeeper
//节点创建得时候设置ACL
create /zk-node datatest digest:gj:X/NSthOB0fD/OT6iilJ55WJVado=:cdrwa
//先用创建节点，再用setAcl设置
setAcl /zk-node  digest:gj:X/NSthOB0fD/OT6iilJ55WJVado=:cdrwa
```

![](https://img02.sogoucdn.com/app/a/100540022/2021081617272563262086.png)

明文授权：使用之前需要先addauth digest username:password注册用户信息，后续可以直接用明文授权

![](https://img01.sogoucdn.com/app/a/100540022/2021081617312737877679.png)

ip授权模式：多个指定IP可以通过逗号分隔， 如 setAcl /node-ip  ip:IP1:rw,ip:IP2:a

```zookeeper
setAcl /node-ip ip:192.168.159.173:cdwra
create /node-ip  data  ip:192.168.159.173:cdwra
```

Super 超级管理员模式:这是一种特殊的Digest模式， 在Super模式下超级管理员用户可以对Zookeeper上的节点进行任何的操作。需要在启动了上通过JVM 系统参数开启：

```zookeeper
DigestAuthenticationProvider中定义
-Dzookeeper.DigestAuthenticationProvider.superDigest=super:<base64encoded(SHA1(password))
```

### 5.zookeeper 内存数据和持久化

Zookeeper数据的组织形式为一个类似文件系统的数据结构，而这些数据都是存储在内存中的，所以我们可以认为，Zookeeper是一个基于内存的小型数据库 

内存结构：

```java
public class DataTree {
    private static final Logger LOG = LoggerFactory.getLogger(DataTree.class);
    private final ConcurrentHashMap<String, DataNode> nodes = new ConcurrentHashMap();
    private final WatchManager dataWatches = new WatchManager();
    private final WatchManager childWatches = new WatchManager();
	...    
}
public class DataNode implements Record {
    byte[] data;
    Long acl;
    public StatPersisted stat;
    private Set<String> children = null;
    private static final Set<String> EMPTY_SET = Collections.emptySet();
}
public class StatPersisted implements Record {
    private long czxid;
    private long mzxid;
    private long ctime;
    private long mtime;
    private int version;
    private int cversion;
    private int aversion;
    private long ephemeralOwner;
    private long pzxid;
}
```

事务日志：

针对每一次客户端的事务操作，Zookeeper都会将他们记录到事务日志中，当然，Zookeeper也会将数据变更应用到内存数据库中。我们可以在zookeeper的主配置文件zoo.cfg 中配置内存中的数据持久化目录，也就是事务日志的存储路径 dataLogDir. 如果没有配置dataLogDir（非必填）, 事务日志将存储到dataDir （必填项）目录，

zookeeper提供了格式化工具可以进行数据查看事务日志数据  

org.apache.zookeeper.server.LogFormatter

进入lib目录下执行：

```zookeeper
java -classpath .:slf4j-api-1.7.25.jar:zookeeper-3.5.9.jar:zookeeper-jute-3.5.9.jar org.apache.zookeeper.server.LogFormatter /tmp/zookeeper/version-2/log.1
```

从左到右分别记录了操作时间，客户端会话ID，CXID,ZXID,操作类型，节点路径，节点数据（用#+ascii 码表示），节点版本。

Zookeeper进行事务日志文件操作的时候会频繁进行磁盘IO操作，事务日志的不断追加写操作会触发底层磁盘IO为文件开辟新的磁盘块，即磁盘Seek。因此，为了提升磁盘IO的效率，Zookeeper在创建事务日志文件的时候就进行文件空间的预分配- 即在创建文件的时候，就向操作系统申请一块大一点的磁盘块。这个预分配的磁盘大小可以通过系统参数 zookeeper.preAllocSize 进行配置。

事务日志文件名为： log.<当时最大事务ID>，应为日志文件时顺序写入的，所以这个最大事务ID也将是整个事务日志文件中，最小的事务ID，日志满了即进行下一次事务日志文件的创建

数据快照：

数据快照用于记录Zookeeper服务器上某一时刻的全量数据，并将其写入到指定的磁盘文件中。

可以通过配置snapCount配置每间隔事务请求个数，生成快照，数据存储在dataDir 指定的目录中，

可以通过如下方式进行查看快照数据（ 为了避免集群中所有机器在同一时间进行快照，实际的快照生成时机为事务数达到 [snapCount/2   + 随机数(随机数范围为1 ~ snapCount/2 )] 个数时开始快照）

进入lib目录下执行：

java -classpath .:slf4j-api-1.7.25.jar:zookeeper-3.5.9.jar:zookeeper-jute-3.5.9.jar                 org.apache.zookeeper.server.SnapshotFormatter /tmp/zookeeper/version-2/snapshot.0

快照事务日志文件名为： snapshot.<当时最大事务ID>，日志满了即进行下一次事务日志文件的创建

有了事务日志，为啥还要快照数据。

快照数据主要时为了快速恢复，事务日志文件是每次事务请求都会进行追加的操作，而快照是达到某种设定条件下的内存全量数据。所以通常快照数据是反应当时内存数据的状态。事务日志是更全面的数据，所以恢复数据的时候，可以先恢复快照数据，再通过增量恢复事务日志中的数据即可。

## 二、zookeeper客户端使用和集群特性

### 1.zookeeper java客户端

1）引入maven依赖

zookeeper 官方的客户端没有和服务端代码分离，他们为同一个jar 文件，所以我们直接引入zookeeper的maven即可， 这里版本请保持与服务端版本一致，不然会有很多兼容性的问题

```java
<dependency>
  <groupId>org.apache.zookeeper</groupId>
  <artifactId>zookeeper</artifactId>
  <version>3.5.9</version>
</dependency>
```

2）操作案例

```java
@Slf4j
public class ZookeeperClientTest {

    private static final String ZK_ADDRESS="192.168.159.173:2181";

    private static final int SESSION_TIMEOUT = 5000;

    private static ZooKeeper zooKeeper;

    private static final String ZK_NODE="/zk-node";

    //连接
    @Before
    public void init() throws IOException, InterruptedException {
        final CountDownLatch countDownLatch=new CountDownLatch(1);
        zooKeeper=new ZooKeeper(ZK_ADDRESS, SESSION_TIMEOUT, event -> {
            if (event.getState()== Watcher.Event.KeeperState.SyncConnected &&
                    event.getType()== Watcher.Event.EventType.None){
                countDownLatch.countDown();
                log.info("连接成功！");
            }
        });
        log.info("连接中....");
        countDownLatch.await();
    }

    //创建节点
    @Test
    public void createTest() throws KeeperException, InterruptedException{
        String path = zooKeeper.create(ZK_NODE,"data".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        log.info("created path: {}",path);
    }

    //异步创建接节点
    @Test
    public void createAsycTest() throws InterruptedException {
        zooKeeper.create(ZK_NODE, "data".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT,
                (rc, path, ctx, name) -> log.info("rc  {},path {},ctx {},name {}",rc,path,ctx,name),"context");
        TimeUnit.SECONDS.sleep(Integer.MAX_VALUE);
    }

    //更新节点
    @Test
    public void setTest() throws KeeperException, InterruptedException {
        Stat stat = new Stat();
        byte[] data = zooKeeper.getData(ZK_NODE, false, stat);
        log.info("修改前: {}",new String(data));
        zooKeeper.setData(ZK_NODE, "changed!".getBytes(), stat.getVersion());
        byte[] dataAfter = zooKeeper.getData(ZK_NODE, false, stat);
        log.info("修改后: {}",new String(dataAfter));
    }

}
```

创建Zookeeper实例的方法：

```java
ZooKeeper(String connectString, int sessionTimeout, Watcher watcher)
ZooKeeper(String connectString, int sessionTimeout, Watcher watcher, ZKClientConfig)
ZooKeeper(String connectString, int sessionTimeout, Watcher watcher, boolean canBeReadOnly, HostProvider)
ZooKeeper(String connectString, int sessionTimeout, Watcher watcher, boolean canBeReadOnly, HostProvider, ZKClientConfig)
ZooKeeper(String connectString, int sessionTimeout, Watcher watcher, boolean canBeReadOnly)
ZooKeeper(String connectString, int sessionTimeout, Watcher watcher, boolean canBeReadOnly, ZKClientConfig)
ZooKeeper(String connectString, int sessionTimeout, Watcher watcher, long, byte[])
ZooKeeper(String connectString, int sessionTimeout, Watcher watcher, long, byte[], boolean, HostProvider)
ZooKeeper(String connectString, int sessionTimeout, Watcher watcher, long, byte[], boolean, HostProvider, ZKClientConfig)
ZooKeeper(String connectString, int  sessionTimeout, Watcher watcher, long, byte[], boolean)
```

| 参数名称                  | 含义                                                         |
| ------------------------- | ------------------------------------------------------------ |
| connectString             | ZooKeeper服务器列表，由英文逗号分开的host:port字符串组成，每一个都代表一台ZooKeeper机器，如，host1:port1,host2:port2,host3:port3。另外，也可以在connectString中设置客户端连接上ZooKeeper后的根目录，方法是在host:port字符串之后添加上这个根目录，例如,host1:port1,host2:port2,host3:port3/zk-base,这样就指定了该客户端连接上ZooKeeper服务器之后,所有对ZooKeeper的操作，都会基于这个根目录。例如，客户端对/sub-node 的操作，最终创建 /zk-node/sub-node, 这个目录也叫Chroot，即客户端隔离命名空间。 |
| sessionTimeout            | 会话的超时时间，是一个以“毫秒”为单位的整型值。在ZooKeeper中有会话的概念，在一个会话周期内，ZooKeeper客户端和服务器之间会通过心跳检测机制来维持会话的有效性，一旦在sessionTimeout时间内没有进行有效的心跳检测，会话就会失效。 |
| watcher                   | ZooKeeper允许客户端在构造方法中传入一个接口 watcher (org.apache. zookeeper.Watcher)的实现类对象来作为默认的 Watcher事件通知处理器。当然，该参数可以设置为null 以表明不需要设置默认的 Watcher处理器。 |
| canBeReadOnly             | 这是一个boolean类型的参数，用于标识当前会话是否支持“read-only(只读)”模式。默认情况下，在ZooKeeper集群中，一个机器如果和集群中过半及以上机器失去了网络连接，那么这个机器将不再处理客户端请求（包括读写请求)。但是在某些使用场景下，当ZooKeeper服务器发生此类故障的时候，我们还是希望ZooKeeper服务器能够提供读服务（当然写服务肯定无法提供）——这就是 ZooKeeper的“read-only”模式。 |
| sessionId和 sessionPasswd | 分别代表会话ID和会话秘钥。这两个参数能够唯一确定一个会话，同时客户端使用这两个参数可以实现客户端会话复用，从而达到恢复会话的效果。具体使用方法是，第一次连接上ZooKeeper服务器时，通过调用ZooKeeper对象实例的以下两个接口，即可获得当前会话的ID和秘钥:long getSessionId();byte[]getSessionPasswd( );荻取到这两个参数值之后，就可以在下次创建ZooKeeper对象实例的时候传入构造方法了 |

### 2.Apache Curator开源客户端

### 3.zookeeper集群&不停机动态扩容/缩容

Zookeeper 集群模式一共有三种类型的角色:

**Leader**:   处理所有的事务请求（写请求），可以处理读请求，集群中只能有一个Leader

**Follower**：只能处理读请求，同时作为 Leader的候选节点，即如果Leader宕机，Follower节点要参与到新的Leader选举中，有可能成为新的Leader节点。

**Observer**：只能处理读请求。不能参与选举 

<img src="https://img04.sogoucdn.com/app/a/100540022/2021081713552245197790.png" style="zoom:80%;" />

#### 3.1 集群搭建

1）创建myid

```shell
mkdir /shenhaibin/zookeeper/apache-zookeeper-3.5.9-bin/data/zookeeper-1
vim myid 
1
mkdir /shenhaibin/zookeeper/apache-zookeeper-3.5.9-bin/data/zookeeper-2
vim myid
2
mkdir /shenhaibin/zookeeper/apache-zookeeper-3.5.9-bin/data/zookeeper-3
vim myid
3
mkdir /shenhaibin/zookeeper/apache-zookeeper-3.5.9-bin/data/zookeeper-4
vim myid
4
```

2）配置文件

```shell
cp zoo_sample.cfg zoo-1.cfg
tickTime=2000
initLimit=10
syncLimit=5
dataDir=/shenhaibin/zookeeper/apache-zookeeper-3.5.9-bin/data/zookeeper-1
clientPort=2181
server.1=192.168.159.173:2001:3001
server.2=192.168.159.173:2002:3002
server.3=192.168.159.173:2003:3003
server.4=192.168.159.173:2004:3004:observer
```

```shell
zoo-2.cfg
    tickTime=2000
    initLimit=10
    syncLimit=5
    dataDir=/shenhaibin/zookeeper/apache-zookeeper-3.5.9-bin/data/zookeeper-2
    clientPort=2181
    server.1=192.168.159.173:2001:3001
	server.2=192.168.159.173:2002:3002
	server.3=192.168.159.173:2003:3003
	server.4=192.168.159.173:2004:3004:observer
```

```shell
zoo-3.cfg
    tickTime=2000
    initLimit=10
    syncLimit=5
    dataDir=/shenhaibin/zookeeper/apache-zookeeper-3.5.9-bin/data/zookeeper-3
    clientPort=2181
    server.1=192.168.159.173:2001:3001
	server.2=192.168.159.173:2002:3002
	server.3=192.168.159.173:2003:3003
	server.4=192.168.159.173:2004:3004:observer
```

```shell
zoo-4.cfg
    tickTime=2000
    initLimit=10
    syncLimit=5
    dataDir=/shenhaibin/zookeeper/apache-zookeeper-3.5.9-bin/data/zookeeper-4
    clientPort=2181
	server.1=192.168.159.173:2001:3001
	server.2=192.168.159.173:2002:3002
	server.3=192.168.159.173:2003:3003
	server.4=192.168.159.173:2004:3004:observer
```

3）配置说明

- tickTime：用于配置Zookeeper中最小时间单位的长度，很多运行时的时间间隔都是使用tickTime的倍数来表示的。
- initLimit：该参数用于配置Leader服务器等待Follower启动，并完成数据同步的时间。Follower服务器再启动过程中，会与Leader建立连接并完成数据的同步，从而确定自己对外提供服务的起始状态。Leader服务器允许Follower再initLimit 时间内完成这个工作。
- syncLimit：Leader 与Follower心跳检测的最大延时时间
- dataDir：顾名思义就是 Zookeeper 保存数据的目录，默认情况下，Zookeeper 将写数据的日志文件也保存在这个目录里。
- clientPort：这个端口就是客户端连接 Zookeeper 服务器的端口，Zookeeper 会监听这个端口，接受客户端的访问请求。
- server.A=B：C：D：E 其中 A 是一个数字，表示这个是第几号服务器；B 是这个服务器的 ip 地址；C 表示的是这个服务器与集群中的 Leader 服务器交换信息的端口；D 表示的是万一集群中的 Leader 服务器挂了，需要一个端口来重新进行选举，选出一个新的 Leader，而这个端口就是用来执行选举时服务器相互通信的端口。如果是伪集群的配置方式，由于 B 都是一样，所以不同的 Zookeeper 实例通信端口号不能一样，所以要给它们分配不同的端口号。如果需要通过添加不参与集群选举以及事务请求的过半机制的 Observer节点，可以在E的位置，添加observer标识。

4）启动

```shell
bin/zkServer.sh start conf/zoo-1.cfg
bin/zkServer.sh start conf/zoo-2.cfg
bin/zkServer.sh start conf/zoo-3.cfg
bin/zkServer.sh start conf/zoo-4.cfg
```

5）验证

```shell
bin/zkServer.sh status conf/zoo-1.cfg

bin/zkServer.sh status conf/zoo-2.cfg

bin/zkServer.sh status conf/zoo-3.cfg

bin/zkServer.sh status conf/zoo-4.cfg
```

6）连接集群

```shell
bin/zkCli.sh -server ip1:port1,ip2:port2,ip3:port3 
```

可以通过 查看/zookeeper/config  节点数据来查看集群配置

#### 3.2 Zookeeper 3.5.0 新特性： 集群动态配置

Zookeeper 3.5.0 以前，Zookeeper集群角色要发生改变的话，只能通过停掉所有的Zookeeper服务，修改集群配置，重启服务来完成，这样集群服务将有一段不可用的状态，为了应对高可用需求，Zookeeper 3.5.0 提供了支持动态扩容/缩容的 新特性。但是通过客户端API可以变更服务端集群状态是件很危险的事情，所以在zookeeper **3.5.3** 版本要用动态配置，需要开启超级管理员身份验证模式 **ACLs**。如果是在一个安全的环境也可以通过配置 系统参数 **-Dzookeeper.skipACL=yes** 来避免配置维护acl 权限配置。

## 三、zookeeper典型使用场景实战

## 四、zookeeper集群Leader选举源码分析

### 1、源码下载安装

1）下载地址：https://github.com/apache/zookeeper/tree/branch-3.5.9

2）在根路径下进行编译：mvn clean install -DskipTests

3）将zookeeper-server下的pom.xml文件下（除jline)的scope为provided这一行全部注释掉

4）将conf文件夹里的log4j.properties文件复制一份到zookeeper-server项目的 \target\classes 目录下，这样项目启动时才会打印日志

### 2、单机启动

1）将conf目录下的zoo_sample.cfg复制一份取名为zoo.cfg,然后修改里面的配置

```java
tickTime=2000
initLimit=10
syncLimit=5
dataDir=D:/projectPath/git/zookeeper/data/zk
clientPort=2181
```

2）启动配置

<img src="https://img03.sogoucdn.com/app/a/100540022/2021081811072624488617.png" style="zoom: 50%;" />

### 3、集群启动

1）在根目录下创建文件夹data，然后在data下分别创建zk1、zk2、zk3文件夹。然后在zk1、zk2、zk3下分别创建myid，里面的值分别为1、2、3

2）将conf目录下的zoo_sample.cfg复制取名为zoo1.cfg、zoo2.cfg、zoo3.cfg,然后修改里面的配置

```java
tickTime=2000
initLimit=10
syncLimit=5
dataDir=D:/projectPath/git/zookeeper/data/zk1(zk2/zk3)
clientPort=2181
server.1=127.0.0.1:2888:3888
server.2=127.0.0.1:2889:3889
server.3=127.0.0.1:2890:3890
```

3)启动配置并分别启动：

<img src="https://img04.sogoucdn.com/app/a/100540022/2021081811173474220723.png" style="zoom:50%;" />

4、集群启动Leader选举流程

<img src="https://img03.sogoucdn.com/app/a/100540022/2021081811202841943080.png" style="zoom:50%;" />

分析:启动时各自都会发送自己的信息给其它机器，然后各个机器都拥有所有机器的信息，各个机器进行比对，各自都按照统一规则选择符合的Leader，然后再统一发送出去进行第二轮投票。