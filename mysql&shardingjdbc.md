# mysql&shardingjdbc

## 一、MYSQL生产环境高可用架构详解

### 1、MYSQL主从架构理解与实战

#### 1.1基础环境介绍

​	Linux服务器两台：centos7

​	192.168.159.172 作为mysql主节点部署

​	192.168.159.167 作为mysql从节点部署

​	mysql版本：mysql:8.0.22

#### 1.2 主节点安装配置（192.168.159.172）

​	(1) 拉取镜像：docker pull mysql:8.0.22

​	(2) 查看自己下载好的镜像：docker images

​	(3) 数据卷挂载：为了数据可以不再移除容器的时候丢失，把mysql容器里的目录挂载在服务器的目录上，如果不是root运行docker命令，要先创建目录

```shell
mkdir -p /usr/local/docker/mysql/data
mkdir -p /usr/local/docker/mysql/mysql-files
mkdir -p /usr/local/docker/mysql/cnf
mkdir -p /usr/local/docker/mysql/log
```

​	(4) 在目录/usr/local/docker/mysql/cnf里新建文件：my.cnf

```shell
[mysqld]
server-id=47
#开启binlog
log_bin=master-bin
log_bin-index=master-bin.index
skip-name-resolve
# 设置连接端口
port=3306
# 设置mysql的安装目录
basedir=/usr/local/mysql
# 设置mysql数据库的数据的存放目录
datadir=/usr/local/mysql/mysql-files
# 允许最大连接数
max_connections=200
# 允许连接失败的次数。
max_connect_errors=10
# 服务端使用的字符集默认为UTF8
character-set-server=utf8
# 创建新表时将使用的默认存储引擎
default-storage-engine=INNODB
# 默认使用“mysql_native_password”插件认证
#mysql_native_password
default_authentication_plugin=mysql_native_password
```

​	配置说明：

​	server-id：服务节点的唯一标识。需要给集群中的每个服务分配一个单 独的ID。 

​	log_bin：打开Binlog日志记录，并指定文件名。 

​	log_bin-index：Binlog日志文件	

(5) 启动mysql容器，使用docker run

```shell
docker run  \
    --restart=always \
    --name mysql \
    -v /usr/local/docker/mysql/cnf:/etc/mysql \
    -v /usr/local/docker/mysql/data:/var/lib/mysql \
    -v /usr/local/docker/mysql/log:/var/log \
    -v /usr/local/docker/mysql/mysql-files:/var/lib/mysql-files \
    -p 3306:3306 \
    -e MYSQL_ROOT_PASSWORD=root \
    -d mysql:8.0.22
```

–restart=always #容器在docker启动的时候，也会跟着启动

–name mysql #给容器取名

-v /usr/local/docker/mysql/data:/var/lib/mysql #挂载目录，就是把容器的目录挂载到外面

-p 3306:3306 #端口映射

-e MYSQL_ROOT_PASSWORD=root #mysql初始密码

-d #后台运行

mysql:8.0.22 #后台运行

(6) 查看启动是否成功：docker  ps -a

(7) 进入容器：docker exec -it mysql bash

(8) 登录主数据库

mysql -uroot -proot

GRANT REPLICATION SLAVE ON *.* TO 'root'@'%'; 

flush privileges; #查看主节点同步状态： 

show master status;

在实际生产环境中，通常不会直接使用root用户，而会创建一个拥有全 部权限的用户来负责主从同步。

![](https://img02.sogoucdn.com/app/a/100540022/2021082314522920964001.png)

这个指令结果中的File和Position记录的是当前日志的binlog文件以及文件中的索引。

开启binlog后，数据库中的所有操作都会被记录到datadir当中，以一组轮询文件的方式循环记录。而指令查到的File和Position就是当前日志的文件和位置。而在后面配置从服务时，就需要通过这个File和Position通知从服务从哪个地方开始记录binLog。

<img src="https://img03.sogoucdn.com/app/a/100540022/2021082314563117968002.png" style="zoom:80%;" />

(9) 开启远程登录

```shell
use mysql;
update user set host='%' where user='root';
flush privileges;
```

#### 1.3 从节点安装配置（192.168.159.167）

(1) 说明：从节点与主节点的安装类似，下面只讲解不同的部分

(2) 在目录/usr/local/docker/mysql/cnf里新建文件：my.cnf

```shell
[mysqld]
#主库和从库需要不一致
server-id=48
#打开MySQL中继日志
relay-log-index=slave-relay-bin.index
relay-log=slave-relay-bin
#打开从服务二进制日志
log-bin=mysql-bin
#使得更新的数据写进二进制日志中
log-slave-updates=1
# 设置3306端口
port=3306
# 设置mysql的安装目录
basedir=/usr/local/mysql
# 设置mysql数据库的数据的存放目录
datadir=/usr/local/mysql/mysql-files
# 允许最大连接数
max_connections=200
# 允许连接失败的次数。
max_connect_errors=10
# 服务端使用的字符集默认为UTF8
character-set-server=utf8
# 创建新表时将使用的默认存储引擎
default-storage-engine=INNODB
# 默认使用“mysql_native_password”插件认证
#mysql_native_password
default_authentication_plugin=mysql_native_password
```

配置说明：主要需要关注的几个属性：

server-id：服务节点的唯一标识

relay-log：打开从服务的relay-log日志。

log-bin：打开从服务的bin-log日志记录。

(3) 登录服务并配置从节点

```shell
#登录从服务
mysql -uroot -proot;
#设置同步主节点：
CHANGE MASTER TO
MASTER_HOST='192.168.159.172',
MASTER_PORT=3306,
MASTER_USER='root',
MASTER_PASSWORD='root',
MASTER_LOG_FILE='master-bin.000003',
MASTER_LOG_POS=156,
GET_MASTER_PUBLIC_KEY=1;
#开启slave
start slave;
#查看主从同步状态
show slave status;
或者用 show slave status \G; 这样查看比较简洁
```

注意，CHANGE MASTER指令中需要指定的MASTER_LOG_FILE和MASTER_LOG_POS必须与主服务中查到的保持一致。并且后续如果要检查主从架构是否成功，也可以通过检查主服务与从服务之间的File和Position这两个属性是否一致来确定。

<img src="https://img01.sogoucdn.com/app/a/100540022/2021082315051724429489.png" style="zoom:80%;" />

#### 1.4 主从集群测试

测试时，我们先用showdatabases，查看下两个MySQL服务中的数据库情况

<img src="https://img02.sogoucdn.com/app/a/100540022/2021082315510166444666.png" style="zoom: 50%;" />

从上面的实验过程看到，我们在主服务中进行的数据操作，就都已经同步到了从服务上。这样，我们一个主从集群就搭建完成了。

另外，这个主从架构是有可能失败的，如果在slave从服务上查看slave状态，发现Slave_SQL_Running=no，就表示主从同步失败了。这有可能是因为在从数据库上进行了写操作，与同步过来的SQL操作冲突了，也有可能是slave从服务重启后有事务回滚了。

如果是因为slave从服务事务回滚的原因，可以按照以下方式重启主从同步：

```shell
mysql> stop slave ;
mysql> set GLOBAL SQL_SLAVE_SKIP_COUNTER=1;
mysql> start slave ;
```

而另一种解决方式就是重新记录主节点的binlog文件消息

```shell
mysql> stop slave ;
mysql> change master to .....
mysql> start slave ;
```

但是这种方式要注意binlog的文件和位置，如果修改后和之前的同步接不上，那就会丢失部分数据。所以不太常用。

#### 1.5 问题记录

参考：http://article.docway.net/details?id=5fccddc7f8d82b656822026e

搭建过程遇到的问题解决：https://www.jianshu.com/p/a68551347d7d

​											https://blog.csdn.net/weixin_37998647/article/details/79950133

#### 1.6 同步原理

 MySQL服务的主从架构一般都是通过binlog日志文件来进行的。即在主服务上打开binlog记录每一步的数据库操作，然后从服务上会有一个IO线程，负责跟主服务建立一个TCP连接，请求主服务将binlog传输过来。这时，主库上会有一个IO dump线程，负责通过这个TCP连接把Binlog日志传输给从库的IO线程。接着从服务的IO线程会把读取到的binlog日志数据写入自己的relay日志文件中。然后从服务上另外一个SQL线程会读取relay日志里的内容，进行操作重演，达到还原数据的目的。我们通常对MySQL做的读写分离配置就必须基于主从架构来搭建。

<img src="https://img02.sogoucdn.com/app/a/100540022/2021082315581369518146.png" style="zoom: 80%;" />

MySQL的binlog不光可以用于主从同步，还可以用于缓存数据同步等场景。

例如Canal，可以模拟一个slave节点，向MySQL发起binlog同步，然后将数据落地到Redis、Kafka等其他组件，实现数据实时流转。

搭建主从集群时，有两个必要的要求：

双方MySQL必须版本一致。至少需要主服务的版本低于从服务
两节点间的时间需要同步。

#### 1.7 集群扩展

（1）全库同步与部分同步

​	之前提到，我们目前配置的主从同步是针对全库配置的，而实际环境中，一般并 不需要针对全库做备份，而只需要对一些特别重要的库或者表来进行同步。那如何 针对库和表做同步配置呢？

​	首先在Master端：在my.cnf中，可以通过以下这些属性指定需要针对哪些库或者哪 些表记录binlog

```shell
#需要同步的二进制数据库名
binlog-do-db=masterdemo
#只保留7天的二进制日志，以防磁盘被日志占满(可选)
expire-logs-days = 7
#不备份的数据库
binlog-ignore-db=information_schema
binlog-ignore-db=performation_schema
binlog-ignore-db=sys
```

然后在Slave端：在my.cnf中，需要配置备份库与主服务的库的对应关系。

```shell
#如果salve库名称与master库名相同，使用本配置
replicate-do-db = masterdemo
#如果master库名[mastdemo]与salve库名[mastdemo01]不同，使用以下配置[需要做映射]
replicate-rewrite-db = masterdemo -> masterdemo01
#如果不是要全部同步[默认全部同步]，则指定需要同步的表
replicate-wild-do-table=masterdemo01.t_dict
replicate-wild-do-table=masterdemo01.t_num
```

配置完成了之后，在show master status指令中，就可以看到Binlog_Do_DB和Binlog_Ignore_DB两个参数的作用了。

(2) 读写分离

我们要注意，目前我们的这个MySQL主从集群是单向的，也就是只能从主服务同 步到从服务，而从服务的数据表更是无法同步到主服务的。所以，在这种架构下，为了保证数据一致，通常会需要保证数据只在主服务上 写，而从服务只进行数据读取。这个功能，就是大名鼎鼎的读写分离。但是这里要 注意下，mysql主从本身是无法提供读写分离的服务的，需要由业务自己来实现。 这也是我们后面要学的ShardingSphere的一个重要功能。到这里可以看到，在MySQL主从架构中，是需要严格限制从服务的数据 写入的，一旦从服务有数据写入，就会造成数据不一致。并且从服务在 执行事务期间还很容易造成数据同步失败。 如果需要限制用户写数据，我们可以在从服务中将read_only参数的值设 为1( set global read_only=1; )。这样就可以限制用户写入数据。 但是这个属性有两个需要注意的地方： 1、read_only=1设置的只读模式，不会影响slave同步复制的功能。 所 以在MySQL slave库中设定了read_only=1后，通过 "show slave status\G" 命令查看salve状态，可以看到salve仍然会读取master上的日 志，并且在slave库中应用日志，保证主从数据库同步一致； 2、read_only=1设置的只读模式， 限定的是普通用户进行数据修改的操 作，但不会限定具有super权限的用户的数据修改操作。 在MySQL中设 置read_only=1后，普通的应用用户进行insert、update、delete等会 产生数据变化的DML操作时，都会报出数据库处于只读模式不能发生数 据变化的错误，但具有super权限的用户，例如在本地或远程通过root用 户登录到数据库，还是可以进行数据变化的DML操作； 如果需要限定 super权限的用户写数据，可以设置super_read_only=0。另外 如果要 想连super权限用户的写操作也禁止，就使用"flush tables with read lock;"，这样设置也会阻止主从同步复制！

#### 1.8 GTID同步集群

上面我们搭建的集群方式，是基于Binlog日志记录点的方式来搭建的，这也是最 为传统的MySQL集群搭建方式。而在这个实验中，可以看到有一个 Executed_Grid_Set列，暂时还没有用上。实际上，这就是另外一种搭建主从同步的 方式，即GTID搭建方式。这种模式是从MySQL5.6版本引入的。 GTID的本质也是基于Binlog来实现主从同步，只是他会基于一个全局的事务ID来 标识同步进度。GTID即全局事务ID，全局唯一并且趋势递增，他可以保证为每一个 在主节点上提交的事务在复制集群中可以生成一个唯一的ID 。 在基于GTID的复制中，首先从服务器会告诉主服务器已经在从服务器执行完了哪 些事务的GTID值，然后主库会有把所有没有在从库上执行的事务，发送到从库上进 行执行，并且使用GTID的复制可以保证同一个事务只在指定的从库上执行一次，这 样可以避免由于偏移量的问题造成数据不一致。 他的搭建方式跟我们上面的主从架构整体搭建方式差不多。只是需要在my.cnf中 修改一些配置。 在主节点上：

```shell
gtid_mode=on
enforce_gtid_consistency=on
log_bin=on
server_id=单独设置一个
binlog_format=row
```

在从节点上：

```shell
gtid_mode=on
enforce_gtid_consistency=on
log_slave_updates=1
server_id=单独设置一个
```

 然后分别重启主服务和从服务，就可以开启GTID同步复制方式。

#### 1.9  集群扩容

我们现在已经搭建成功了一主一从的MySQL集群架构，那要扩展到一主多从的集群架构，其实就比较简单了，只需要增加一个binlog复制就行了。

 但是如果我们的集群是已经运行过一段时间，这时候如果要扩展新的从节点就有一个问题，之前的数据没办法从binlog来恢复了。这时候在扩展新的slave节点时，就需要增加一个数据复制的操作。

 MySQL的数据备份恢复操作相对比较简单，可以通过SQL语句直接来完成。具体操作可以使用mysql的bin目录下的mysqldump工具。

```
mysqldump -u root -p --all-databases > backup.sql
#输入密码
```

 通过这个指令，就可以将整个数据库的所有数据导出成backup.sql，然后把这个backup.sql分发到新的MySQL服务器上，并执行下面的指令将数据全部导入到新的MySQL服务中。

```
mysql -u root -p < backup.sql
#输入密码
```

 这样新的MySQL服务就已经有了所有的历史数据，然后就可以再按照上面的步骤，配置Slave从服务的数据同步了。

#### 1.10 主从架构的数据延迟问题

在我们搭建的这个主从集群中，有一个比较隐藏的问题，就是这样的主从复制之间会有延迟。这在做了读写分离后，会更容易体现出来。即数据往主服务写，而读数据在从服务读。这时候这个主从复制延迟就有可能造成刚插入了数据但是查不到。当然，这在我们目前的这个集群中是很难出现的，但是在大型集群中会很容易出现。

 出现这个问题的根本在于：面向业务的主服务数据都是多线程并发写入的，而从服务是单个线程慢慢拉取binlog，这中间就会有个效率差。所以解决这个问题的关键是要让从服务也用多线程并行复制binlog数据。

 MySQL自5.7版本后就已经支持并行复制了。可以在从服务上设置slave_parallel_workers为一个大于0的数，然后把slave_parallel_type参数设置为LOGICAL_CLOCK，这就可以了。

### 2、MYSQL半同步复制机制

#### 2.1 什么是半同步复制？

到现在为止，我们已经可以搭建MySQL的主从集群，互主集群，但是我们这个集群有一个隐患，就是有可能会丢数据。这是为什么呢？这要从MySQL主从数据复制分析起。

 MySQL主从集群默认采用的是一种异步复制的机制。主服务在执行用户提交的事务后，写入binlog日志，然后就给客户端返回一个成功的响应了。而binlog会由一个dump线程异步发送给Slave从服务。

<img src="https://img04.sogoucdn.com/app/a/100540022/2021082316390828949459.png" style="zoom:80%;" />

 由于这个发送binlog的过程是异步的。主服务在向客户端反馈执行结果时，是不知道binlog是否同步成功了的。这时候如果主服务宕机了，而从服务还没有备份到新执行的binlog，那就有可能会丢数据。

 那怎么解决这个问题呢，这就要靠MySQL的半同步复制机制来保证数据安全。

 半同步复制机制是一种介于异步复制和全同步复制之前的机制。主库在执行完客户端提交的事务后，并不是立即返回客户端响应，而是等待至少一个从库接收并写到relay log中，才会返回给客户端。MySQL在等待确认时，默认会等10秒，如果超过10秒没有收到ack，就会降级成为异步复制。

<img src="https://img01.sogoucdn.com/app/a/100540022/2021082316390813943794.png" style="zoom:80%;" />

这种半同步复制相比异步复制，能够有效的提高数据的安全性。但是这种安全性也不是绝对的，他只保证事务提交后的binlog至少传输到了一个从库，并且并不保证从库应用这个事务的binlog是成功的。另一方面，半同步复制机制也会造成一定程度的延迟，这个延迟时间最少是一个TCP/IP请求往返的时间。整个服务的性能是会有所下降的。而当从服务出现问题时，主服务需要等待的时间就会更长，要等到从服务的服务恢复或者请求超时才能给用户响应。

#### 2.2 搭建半同步复制集群

 半同步复制需要基于特定的扩展模块来实现。而mysql从5.5版本开始，往上的版本都默认自带了这个模块。这个模块包含在mysql安装目录下的lib/plugin目录下的semisync_master.so和semisync_slave.so两个文件中。需要在主服务上安装semisync_master模块，在从服务上安装semisync_slave模块。

首先我们登陆主服务，安装semisync_master模块：

```
mysql> install plugin rpl_semi_sync_master soname 'semisync_master.so';
Query OK, 0 rows affected (0.01 sec)

mysql> show global variables like 'rpl_semi%';
+-------------------------------------------+------------+
| Variable_name                             | Value      |
+-------------------------------------------+------------+
| rpl_semi_sync_master_enabled              | OFF        |
| rpl_semi_sync_master_timeout              | 10000      |
| rpl_semi_sync_master_trace_level          | 32         |
| rpl_semi_sync_master_wait_for_slave_count | 1          |
| rpl_semi_sync_master_wait_no_slave        | ON         |
| rpl_semi_sync_master_wait_point           | AFTER_SYNC |
+-------------------------------------------+------------+
6 rows in set, 1 warning (0.02 sec)

mysql> set global rpl_semi_sync_master_enabled=ON;
Query OK, 0 rows affected (0.00 sec)
```

> 这三行指令中，第一行是通过扩展库来安装半同步复制模块，需要指定扩展库的文件名。
>
> 第二行查看系统全局参数，rpl_semi_sync_master_timeout就是半同步复制时等待应答的最长等待时间，默认是10秒，可以根据情况自行调整。
>
> 第三行则是打开半同步复制的开关。
>
> 在第二行查看系统参数时，最后的一个参数rpl_semi_sync_master_wait_point其实表示一种半同步复制的方式。
>
> 半同步复制有两种方式，一种是我们现在看到的这种默认的AFTER_SYNC方式。这种方式下，主库把日志写入binlog，并且复制给从库，然后开始等待从库的响应。从库返回成功后，主库再提交事务，接着给客户端返回一个成功响应。
>
> 而另一种方式是叫做AFTER_COMMIT方式。他不是默认的。这种方式，在主库写入binlog后，等待binlog复制到从库，主库就提交自己的本地事务，再等待从库返回给自己一个成功响应，然后主库再给客户端返回响应。

然后我们登陆从服务，安装smeisync_slave模块

```
mysql> install plugin rpl_semi_sync_slave soname 'semisync_slave.so';
Query OK, 0 rows affected (0.01 sec)

mysql> show global variables like 'rpl_semi%';
+---------------------------------+-------+
| Variable_name                   | Value |
+---------------------------------+-------+
| rpl_semi_sync_slave_enabled     | OFF   |
| rpl_semi_sync_slave_trace_level | 32    |
+---------------------------------+-------+
2 rows in set, 1 warning (0.01 sec)

mysql> set global rpl_semi_sync_slave_enabled = on;
Query OK, 0 rows affected (0.00 sec)

mysql> show global variables like 'rpl_semi%';
+---------------------------------+-------+
| Variable_name                   | Value |
+---------------------------------+-------+
| rpl_semi_sync_slave_enabled     | ON    |
| rpl_semi_sync_slave_trace_level | 32    |
+---------------------------------+-------+
2 rows in set, 1 warning (0.00 sec)

mysql> stop slave;
Query OK, 0 rows affected (0.01 sec)

mysql> start slave;
Query OK, 0 rows affected (0.01 sec)
```

> slave端的安装过程基本差不多，不过要注意下安装完slave端的半同步插件后，需要重启下slave服务。

### 3、MYSQL集群的高可用架构方案详解

我们之前的MySQL服务集群，都是使用MySQL自身的功能来搭建的集群。但是这样的集群，不具备高可用的功能。即如果是MySQL主服务挂了，从服务是没办法自动切换成主服务的。而如果要实现MySQL的高可用，需要借助一些第三方工具来实现。

 常见的MySQL集群方案有三种: MMM、MHA、MGR。这三种高可用框架都有一些共同点：

对主从复制集群中的Master节点进行监控

自动的对Master进行迁移，通过VIP。

重新配置集群中的其它slave对新的Master进行同步

#### 3.1 MMM

 MMM(Master-Master replication managerfor Mysql，Mysql主主复制管理器)是一套由Perl语言实现的脚本程序，可以对mysql集群进行监控和故障迁移。他需要两个Master，同一时间只有一个Master对外提供服务，可以说是主备模式。

 他是通过一个VIP(虚拟IP)的机制来保证集群的高可用。整个集群中，在主节点上会通过一个VIP地址来提供数据读写服务，而当出现故障时，VIP就会从原来的主节点漂移到其他节点，由其他节点提供服务。

<img src="https://img01.sogoucdn.com/app/a/100540022/2021082317305297821278.png" style="zoom: 80%;" />

优点：

- 提供了读写VIP的配置，使读写请求都可以达到高可用
- 工具包相对比较完善，不需要额外的开发脚本
- 完成故障转移之后可以对MySQL集群进行高可用监控

缺点：

- 故障简单粗暴，容易丢失事务，建议采用半同步复制方式，减少失败的概率
- 目前MMM社区已经缺少维护，不支持基于GTID的复制

适用场景：

- 读写都需要高可用的
- 基于日志点的复制方式

#### 3.2 MHA

Master High Availability Manager and Tools for MySQL。是由日本人开发的一个基于Perl脚本写的工具。这个工具专门用于监控主库的状态，当发现master节点故障时，会提升其中拥有新数据的slave节点成为新的master节点，在此期间，MHA会通过其他从节点获取额外的信息来避免数据一致性方面的问题。MHA还提供了mater节点的在线切换功能，即按需切换master-slave节点。MHA能够在30秒内实现故障切换，并能在故障切换过程中，最大程度的保证数据一致性。在淘宝内部，也有一个相似的TMHA产品。

MHA是需要单独部署的，分为Manager节点和Node节点，两种节点。其中Manager节点一般是单独部署的一台机器。而Node节点一般是部署在每台MySQL机器上的。 Node节点得通过解析各个MySQL的日志来进行一些操作。

Manager节点会通过探测集群里的Node节点去判断各个Node所在机器上的MySQL运行是否正常，如果发现某个Master故障了，就直接把他的一个Slave提升为Master，然后让其他Slave都挂到新的Master上去，完全透明。

<img src="https://img04.sogoucdn.com/app/a/100540022/2021082317305292132468.png" style="zoom:80%;" />

优点：

- MHA除了支持日志点的复制还支持GTID的方式
- 同MMM相比，MHA会尝试从旧的Master中恢复旧的二进制日志，只是未必每次都能成功。如果希望更少的数据丢失场景，建议使用MHA架构。

缺点：

MHA需要自行开发VIP转移脚本。

MHA只监控Master的状态，未监控Slave的状态

#### 3.3 MGR

MGR：MySQL Group Replication。 是MySQL官方在5.7.17版本正式推出的一种组复制机制。主要是解决传统异步复制和半同步复制的数据一致性问题。

由若干个节点共同组成一个复制组，一个事务提交后，必须经过超过半数节点的决议并通过后，才可以提交。引入组复制，主要是为了解决传统异步复制和半同步复制可能产生数据不一致的问题。MGR依靠分布式一致性协议(Paxos协议的一个变体)，实现了分布式下数据的最终一致性，提供了真正的数据高可用方案(方案落地后是否可靠还有待商榷)。

支持多主模式，但官方推荐单主模式：

- 多主模式下，客户端可以随机向MySQL节点写入数据

- 单主模式下，MGR集群会选出primary节点负责写请求，primary节点与其它节点都可以进行读请求处理.

  <img src="https://img03.sogoucdn.com/app/a/100540022/2021082317305146427201.png" style="zoom:80%;" />

优点：

- 基本无延迟，延迟比异步的小很多
- 支持多写模式，但是目前还不是很成熟
- 数据的强一致性，可以保证数据事务不丢失

缺点:

- 仅支持innodb，且每个表必须提供主键。
- 只能用在GTID模式下，且日志格式为row格式。

适用的业务场景：

- 对主从延迟比较敏感
- 希望对对写服务提供高可用，又不想安装第三方软件
- 数据强一致的场景

### 4、分库分表与读写分离详解

前面通过搭建主从架构和理解数据的同步原理等，目的是作为读写分离的支持，也是为后续学习ShardingSphere做铺垫。分库分表就是业务系统将数据写请求分发到master节点，而读请求分发到slave节点的一种方案，可以大大提高整个数据库集群的性能。但是要注意，分库分表的一整套逻辑全部是由客户端自行实现的。而对于MySQL集群，数据主从同步是实现读写分离的一个必要前提条件。

#### 4.1 分库分表有什么作用

分库分表就是为了解决由于数据量过大而导致数据库性能降低的问题，将原来独立的数据库拆分成若干数据库组成 ，将数据大表拆分成若干数据表组成，使得单一数据库、单一数据表的数据量变小，从而达到提升数据库性能的目 的。

例如：微服务架构中，每个服务都分配一个独立的数据库，这就是分库。而对一些业务日志表，按月拆分成不同的表，这就是分表。

#### 4.2 分库分表的方式

分库分表包含分库和分表 两个部分，而这两个部分可以统称为数据分片，其目的都是将数据拆分成不同的存储单元。另外，从分拆的角度上，可以分为垂直分片和水平分片。

- 垂直分片： 按照业务来对数据进行分片，又称为纵向分片。他的核心理念就是转库专用。在拆分之前，一个数据库由多个数据表组成，每个表对应不同的业务。而拆分之后，则是按照业务将表进行归类，分布到不同的数据库或表中，从而将压力分散至不同的数据库或表。例如，下图将用户表和订单表垂直分片到不同的数据库：

  <img src="https://img02.sogoucdn.com/app/a/100540022/2021082317015042896624.png" style="zoom:80%;" />

垂直分片往往需要对架构和设计进行调整。通常来讲，是来不及应对业务需求快速变化的。而且，他也无法真正的解决单点数据库的性能瓶颈。垂直分片可以缓解数据量和访问量带来的问题，但无法根治。如果垂直分片之后，表中的数据量依然超过单节点所能承载的阈值，则需要水平分片来进一步处理。

- 水平分片：又称横向分片。相对于垂直分片，它不再将数据根据业务逻辑分类，而是通过某个字段(或某几个字段)，根据某种规则将数据分散至多个库或表中，每个分片仅包含数据的一部分。例如，像下图根据主键机构分片。

  <img src="https://img01.sogoucdn.com/app/a/100540022/2021082317015048523352.png" style="zoom:80%;" />

常用的分片策略有：

 取余\取模 ： 优点 均匀存放数据，缺点 扩容非常麻烦

 按照范围分片 ： 比较好扩容， 数据分布不够均匀

 按照时间分片 ： 比较容易将热点数据区分出来。

 按照枚举值分片 ： 例如按地区分片

 按照目标字段前缀指定进行分区：自定义业务规则分片

水平分片从理论上突破了单机数据量处理的瓶颈，并且扩展相对自由，是分库分表的标准解决方案。

 一般来说，在系统设计阶段就应该根据业务耦合松紧来确定垂直分库，垂直分表方案，在数据量及访问压力不是特别大的情况，首先考虑缓存、读写分离、索引技术等方案。若数据量极大，且持续增长，再考虑水平分库水平分表方案

#### 4.3 分库分表的缺点

 虽然数据分片解决了性能、可用性以及单点备份恢复等问题，但是分布式的架构在获得收益的同时，也引入了非常多新的问题。

- 事务一致性问题

原本单机数据库有很好的事务机制能够帮我们保证数据一致性。但是分库分表后，由于数据分布在不同库甚至不同服务器，不可避免会带来分布式事务问题。

- 跨节点关联查询问题

在没有分库时，我们可以进行很容易的进行跨表的关联查询。但是在分库后，表被分散到了不同的数据库，就无法进行关联查询了。

这时就需要将关联查询拆分成多次查询，然后将获得的结果进行拼装。

- 跨节点分页、排序函数

跨节点多库进行查询时，limit分页、order by排序等问题，就变得比较复杂了。需要先在不同的分片节点中将数据 进行排序并返回，然后将不同分片返回的结果集进行汇总和再次排序。

这时非常容易出现内存崩溃的问题。

- 主键避重问题

在分库分表环境中，由于表中数据同时存在不同数据库中，主键值平时使用的自增长将无用武之地，某个分区数据 库生成的ID无法保证全局唯一。因此需要单独设计全局主键，以避免跨库主键重复问题。

- 公共表处理

实际的应用场景中，参数表、数据字典表等都是数据量较小，变动少，而且属于高频联合查询的依赖表。这一类表一般就需要在每个数据库中都保存一份，并且所有对公共表的操作都要分发到所有的分库去执行。

- 运维工作量

面对散乱的分库分表之后的数据，应用开发工程师和数据库管理员对数据库的操作都变得非常繁重。对于每一次数据读写操作，他们都需要知道要往哪个具体的数据库的分表去操作，这也是其中重要的挑战之一。

#### 4.4 什么时候需要分库分表

在阿里巴巴公布的开发手册中，建议MySQL单表记录如果达到500W这个级别，或者单表容量达到2GB，一般就建议进行分库分表。而考虑到分库分表需要对数据进行再平衡，所以如果要使用分库分表，就要在系统设计之初就详细考虑好分库分表的方案，这里要分两种情况。

 一般对于用户数据这一类后期增长比较缓慢的数据，一般可以按照三年左右的业务量来预估使用人数，按照标准预设好分库分表的方案。

 而对于业务数据这一类增长快速且稳定的数据，一般则需要按照预估量的两倍左右预设分库分表方案。并且由于分库分表的后期扩容是非常麻烦的，所以在进行分库分表时，尽量根据情况，多分一些表。最好是计算一下数据增量，永远不用增加更多的表。

 另外，在设计分库分表方案时，要尽量兼顾业务场景和数据分布。在支持业务场景的前提下，尽量保证数据能够分得更均匀。

 最后，一旦用到了分库分表，就会表现为对数据查询业务的灵活性有一定的影响，例如如果按userId进行分片，那按age来进行查询，就必然会增加很多麻烦。如果再要进行排序、分页、聚合等操作，很容易就扛不住了。这时候，都要尽量在分库分表的同时，再补充设计一个降级方案，例如将数据转存一份到ES，ES可以实现更灵活的大数据聚合查询。

#### 4.5 常见的分库分表组件

由于分库分表之后，数据被分散在不同的数据库、服务器。因此，对数据的操作也就无法通过常规方式完成，并且 它还带来了一系列的问题。好在，这些问题不是所有都需要我们在应用层面上解决，市面上有很多中间件可供我们 选择，我们来了解一下它。

- shardingsphere 官网地址：https://shardingsphere.apache.org/document/current/cn/overview/

  Sharding-Sphere是当当网研发的开源分布式数据库中间件，他是一套开源的分布式数据库中间件解决方案组成的生态圈，它由Sharding-JDBC、Sharding-Proxy和Sharding-Sidecar（计划中）这3款相互独立的产品组成。 他们均提供标准化的数据分片、分布式事务和 数据库治理功能，可适用于如Java同构、异构语言、容器、云原生等各种多样化的应用场景。

- mycat 官网地址： http://www.mycat.org.cn/

  基于阿里开源的Cobar产品而研发，Cobar的稳定性、可靠性、优秀的架构和性能以及众多成熟的使用案例使得MYCAT一开始就拥有一个很好的起点，站在巨人的肩膀上，我们能看到更远。业界优秀的开源项目和创新思路被广泛融入到MYCAT的基因中，使得MYCAT在很多方面都领先于目前其他一些同类的开源项目，甚至超越某些商业产品。

  MyCAT虽然是从阿里的技术体系中出来的，但是跟阿里其实没什么关系。

- DBLE 官网地址：https://opensource.actionsky.com/

  该网站包含几个重要产品。其中分布式中间件可以认为是MyCAT的一个增强版，专注于MySQL的集群化管理。另外还有数据传输组件和分布式事务框架组件可供选择。

二、ShardingSphere分库分表实战与核心原理

1、官网及其介绍

官网：https://shardingsphere.apache.org/document/current/cn/dev-manual/sql-parser/

Apache ShardingSphere 是一套开源的分布式数据库解决方案组成的生态圈，它由 JDBC、Proxy 和 Sidecar（规划中）这 3 款既能够独立部署，又支持混合部署配合使用的产品组成。 它们均提供标准化的数据水平扩展、分布式事务和分布式治理等功能，可适用于如 Java 同构、异构语言、云原生等各种多样化的应用场景。

Apache ShardingSphere 旨在充分合理地在分布式的场景下利用关系型数据库的计算和存储能力，而并非实现一个全新的关系型数据库。 关系型数据库当今依然占有巨大市场份额，是企业核心系统的基石，未来也难于撼动，我们更加注重在原有基础上提供增量，而非颠覆。

Apache ShardingSphere 5.x 版本开始致力于可插拔架构，项目的功能组件能够灵活的以可插拔的方式进行扩展。 目前，数据分片、读写分离、数据加密、影子库压测等功能，以及对 MySQL、PostgreSQL、SQLServer、Oracle 等 SQL 与协议的支持，均通过插件的方式织入项目。 开发者能够像使用积木一样定制属于自己的独特系统。Apache ShardingSphere 目前已提供数十个 SPI 作为系统的扩展点，而且仍在不断增加中。

ShardingSphere包含三个重要的产品，ShardingJDBC、ShardingProxy和 ShardingSidecar。其中sidecar是针对service mesh定位的一个分库分表插件，目 前在规划中。

其中，ShardingJDBC是用来做客户端分库分表的产品，而ShardingProxy是用 来做服务端分库分表的产品。这两者定位有什么区别呢？我们看下官方资料中给出 的两个重要的图：

ShardingJDBC:

<img src="https://shardingsphere.apache.org/document/current/img/shardingsphere-jdbc-brief.png" alt="ShardingSphere-JDBC Architecture" style="zoom:80%;" />

shardingJDBC定位为轻量级 Java 框架，在 Java 的 JDBC 层提供的额外服务。它 使⽤客户端直连数据库，以 jar 包形式提供服务，⽆需额外部署和依赖，可理解为增 强版的 JDBC 驱动，完全兼容 JDBC 和各种 ORM 框架。

ShardingProxy:

<img src="https://shardingsphere.apache.org/document/current/img/shardingsphere-proxy-brief.png" alt="ShardingSphere-Proxy Architecture" style="zoom:80%;" />

ShardingProxy定位为透明化的数据库代理端，提供封装了数据库⼆进制协议的服 务端版本，⽤于完成对异构语⾔的⽀持。⽬前提供 MySQL 和 PostgreSQL 版本， 它可以使⽤任何兼容 MySQL/PostgreSQL 协议的访问客⼾端。

2、ShardingJDBC分库分表实战

shardingjdbc的核心功能是数据分片和读写分离，通过ShardingJDBC，应用可以 透明的使用JDBC访问已经分库分表、读写分离的多个数据源，而不用关心数据源的 数量以及数据如何分布。

2.1 核心概念：

- 逻辑表：水平拆分的数据库的相同逻辑和数据结构表的总称

- 真实表：在分片的数据库中真实存在的物理表。

- 数据节点：数据分片的最小单元。由数据源名称和数据表组成

- 绑定表：分片规则一致的主表和子表。

- 广播表：也叫公共表，指素有的分片数据源中都存在的表，表结构和表中的数据在每个数据库中都完全一致。例如字典表。

- 分片键：用于分片的数据库字段，是将数据库(表)进行水平拆分的关键字段。SQL中若没有分片字段，将会执行全路由，性能会很差。

- 分片算法：通过分片算法将数据进行分片，支持通过=、BETWEEN和IN分片。分片算法需要由应用开发者自行实现，可实现的灵活度非常高。

- 分片策略：真正用于进行分片操作的是分片键+分片算法，也就是分片策略。在ShardingJDBC中一般采用基于Groovy表达式的inline分片策略，通过一个包含分片键的算法表达式来制定分片策略，如t_user_$->{u_id%8}标识根据u_id模8，分成8张表，表名称为t_user_0到t_user_7。

  

2.2 快速实战

(1) pom.xml

```java
<dependencies>
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>sharding-jdbc-spring-boot-starter</artifactId>
        <version>4.1.1</version>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
    </dependency>
    <dependency>
        <groupId>com.alibaba</groupId>
        <artifactId>druid</artifactId>
        <version>1.1.22</version>
    </dependency>
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
    </dependency>
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-boot-starter</artifactId>
        <version>3.0.5</version>
    </dependency>
</dependencies>
```

(2) application.properties

```java
#给数据源起名m1
spring.shardingsphere.datasource.names=m1

#数据库连接信息
spring.shardingsphere.datasource.m1.type=com.alibaba.druid.pool.DruidDataSource
spring.shardingsphere.datasource.m1.driver-class-name=com.mysql.cj.jdbc.Driver
spring.shardingsphere.datasource.m1.url=jdbc:mysql://192.168.159.172:3306/coursedb?serverTimezone=GMT%2B8
spring.shardingsphere.datasource.m1.username=root
spring.shardingsphere.datasource.m1.password=root

#这里的course是逻辑表，对应于数据库的真实表m1.course_${1..2}，m1.course_${1..2}表示course_1,course_2
spring.shardingsphere.sharding.tables.course.actual-data-nodes=m1.course_${1..2}

#列cid自动生成
spring.shardingsphere.sharding.tables.course.key-generator.column=cid
#生成cid的算法是SNOWFLAKE（雪花算法）
spring.shardingsphere.sharding.tables.course.key-generator.type=SNOWFLAKE
spring.shardingsphere.sharding.tables.course.key-generator.props.worker.id=1

#分片键cid
spring.shardingsphere.sharding.tables.course.table-strategy.inline.sharding-column=cid
#分片算法course_$->{cid%2+1}，表示cid偶数时落在course_1,奇数时落在course_2
spring.shardingsphere.sharding.tables.course.table-strategy.inline.algorithm-expression=course_$->{cid%2+1}

spring.shardingsphere.props.sql.show=true
spring.main.allow-bean-definition-overriding=true
```

(3) 测试

```java
@Test
public void addCourse(){
    for (int i = 0; i < 10; i++){
        Course c = new Course();
        c.setCname("shardingsphere");
        c.setUserId(Long.valueOf(""+(1000+i)));
        c.setCstatus("1");
        courseMapper.insert(c);
    }
}
```

3、ShardingJDBC分片算法详解

ShardingJDBC的整个实战完成后，可以看到，整个分库分表的核心就是在于配置的分片算法。我们的这些实战都是使用的inline分片算法，即提供一个分片键和一个分片表达式来制定分片算法。这种方式配置简单，功能灵活，是分库分表最佳的配置方式，并且对于绝大多数的分库分片场景来说，都已经非常好用了。但是，如果针对一些更为复杂的分片策略，例如多分片键、按范围分片等场景，inline分片算法就有点力不从心了。所以，我们还需要学习下ShardingSphere提供的其他几种分片策略。

 ShardingSphere目前提供了一共五种分片策略：

(1) NoneShardingStrategy

不分片。这种严格来说不算是一种分片策略了。只是ShardingSphere也提供了这么一个配置。

(2) InlineShardingStrategy

最常用的分片方式:

- 配置参数： inline.shardingColumn 分片键；inline.algorithmExpression 分片表达式
- 实现方式： 按照分片表达式来进行分片。

(3) StandardShardingStrategy

只支持单分片键的标准分片策略。

- 配置参数：standard.sharding-column 分片键；standard.precise-algorithm-class-name 精确分片算法类名；standard.range-algorithm-class-name 范围分片算法类名

- 实现方式：

  shardingColumn指定分片算法。

  preciseAlgorithmClassName 指向一个实现了io.shardingsphere.api.algorithm.sharding.standard.PreciseShardingAlgorithm接口的java类名，提供按照 = 或者 IN 逻辑的精确分片 `示例：com.haibin.sharding.algorithm.MyPreciseShardingAlgorithm`

  rangeAlgorithmClassName 指向一个实现了 io.shardingsphere.api.algorithm.sharding.standard.RangeShardingAlgorithm接口的java类名，提供按照Between 条件进行的范围分片。`示例：com.haibin.sharding.algorithm.MyRangeShardingAlgorithm`

- 说明：

  其中精确分片算法是必须提供的，而范围分片算法则是可选的。

  ```java
  #standard
  #spring.shardingsphere.sharding.tables.course.table-strategy.standard.sharding-column=cid
  #分片算法course_$->{cid%2+1}，表示cid偶数时落在course_1,奇数时落在course_2
  #spring.shardingsphere.sharding.tables.course.table-strategy.standard.precise-algorithm-class-name=com.haibin.sharding.algorithem.MyPreciseTableShardingAlgorithm
  #spring.shardingsphere.sharding.tables.course.table-strategy..standard.range-algorithm-class-name=com.haibin.sharding.algorithem.MyRangeTableShardingAlgorithm
  
  #spring.shardingsphere.sharding.tables.course.database-strategy.standard.sharding-column=cid
  #spring.shardingsphere.sharding.tables.course.database-strategy.standard.precise-algorithm-class-name=com.haibin.sharding.algorithem.MyPreciseDSShardingAlgorithm
  #spring.shardingsphere.sharding.tables.course.database-strategy..standard..range-algorithm-class-name=com.haibin.sharding.algorithem.MyRangeDSShardingAlgorithm
  ```

```
public class MyPreciseTableShardingAlgorithm implements PreciseShardingAlgorithm<Long> {

    //select * from course where cid = ? or cid in (?,?)
    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<Long> shardingValue) {
        String logicTableName = shardingValue.getLogicTableName();
        String cid = shardingValue.getColumnName();
        Long cidValue = shardingValue.getValue();
        //实现 course_$->{cid%2+1}
        BigInteger shardingValueB = BigInteger.valueOf(cidValue);
        BigInteger resB = (shardingValueB.mod(new BigInteger("2"))).add(new BigInteger("1"));
        String key = logicTableName+"_"+resB;
        if (availableTargetNames.contains(key)){
            return key;
        }
        //couse_1, course_2
        throw new UnsupportedOperationException("route "+ key +" is not supported ,please check your config");
    }
}
....
```

(4) ComplexShardingStrategy

支持多分片键的复杂分片策略。

- 配置参数：complex.sharding-columns 分片键(多个); complex.algorithm-class-name 分片算法实现类。

- 实现方式：

  shardingColumn指定多个分片列。

  algorithmClassName指向一个实现了org.apache.shardingsphere.api.sharding.complex.ComplexKeysShardingAlgorithm接口的java类名。提供按照多个分片列进行综合分片的算法。`示例：com.haibin.sharding.algorithm.MyComplexKeysShardingAlgorithm`

```java
#complex
#spring.shardingsphere.sharding.tables.course.table-strategy.complex.sharding-columns= cid, user_id
#spring.shardingsphere.sharding.tables.course.table-strategy.complex.algorithm-class-name=com.haibin.sharding.algorithem.MyComplexTableShardingAlgorithm

#spring.shardingsphere.sharding.tables.course.database-strategy.complex.sharding-columns=cid, user_id
#spring.shardingsphere.sharding.tables.course.database-strategy.complex.algorithm-class-name=com.haibin.sharding.algorithem.MyComplexDSShardingAlgorithm
```

(5) HintShardingStrategy

不需要分片键的强制分片策略。这个分片策略，简单来理解就是说，他的分片键不再跟SQL语句相关联，而是用程序另行指定。对于一些复杂的情况，例如select count(*) from (select userid from t_user where userid in (1,3,5,7,9)) 这样的SQL语句，就没法通过SQL语句来指定一个分片键。这个时候就可以通过程序，给他另行执行一个分片键，例如在按userid奇偶分片的策略下，可以指定1作为分片键，然后自行指定他的分片策略。

- 配置参数：hint.algorithm-class-name 分片算法实现类。

- 实现方式：

  algorithmClassName指向一个实现了org.apache.shardingsphere.api.sharding.hint.HintShardingAlgorithm接口的java类名。 `示例：com.haibin.sharding.algorithm.MyHintShardingAlgorithm`

  在这个算法类中，同样是需要分片键的。而分片键的指定是通过HintManager.addDatabaseShardingValue方法(分库)和HintManager.addTableShardingValue(分表)来指定。

  使用时要注意，这个分片键是线程隔离的，只在当前线程有效，所以通常建议使用之后立即关闭，或者用try资源方式打开。

> 而Hint分片策略并没有完全按照SQL解析树来构建分片策略，是绕开了SQL解析的，所有对某些比较复杂的语句，Hint分片策略性能有可能会比较好(情况太多了，无法一一分析)。
>
> 但是要注意，Hint强制路由在使用时有非常多的限制：

```java
#hint
spring.shardingsphere.sharding.tables.course.table-strategy.hint.algorithm-class-name=com.haibin.sharding.algorithem.MyHintTableShardingAlgorithm
```

（6）广播表配置

```java
#广播表，所有的库都会保留完整的数据
spring.shardingsphere.sharding.broadcast-tables=t_dict
spring.shardingsphere.sharding.tables.t_dict.key-generator.column=dict_id
spring.shardingsphere.sharding.tables.t_dict.key-generator.type=SNOWFLAKE
```

(7) 绑定表配置

```java
#给数据源起名m1
spring.shardingsphere.datasource.names=m1

#数据库连接信息
spring.shardingsphere.datasource.m1.type=com.alibaba.druid.pool.DruidDataSource
spring.shardingsphere.datasource.m1.driver-class-name=com.mysql.cj.jdbc.Driver
spring.shardingsphere.datasource.m1.url=jdbc:mysql://192.168.159.172:3306/coursedb?serverTimezone=GMT%2B8
spring.shardingsphere.datasource.m1.username=root
spring.shardingsphere.datasource.m1.password=root

spring.shardingsphere.sharding.tables.t_dict.actual-data-nodes=m1.t_dict_${1..2}
spring.shardingsphere.sharding.tables.t_dict.key-generator.column=dict_id
spring.shardingsphere.sharding.tables.t_dict.key-generator.type=SNOWFLAKE
spring.shardingsphere.sharding.tables.t_dict.key-generator.props.worker.id=1
spring.shardingsphere.sharding.tables.t_dict.table-strategy.inline.sharding-column=ustatus
spring.shardingsphere.sharding.tables.t_dict.table-strategy.inline.algorithm-expression=t_dict_$->{ustatus.toInteger()%2+1}

spring.shardingsphere.sharding.tables.user.actual-data-nodes=m1.t_user_$->{1..2}
#列cid自动生成
spring.shardingsphere.sharding.tables.user.key-generator.column=user_id
#生成cid的算法是SNOWFLAKE（雪花算法）
spring.shardingsphere.sharding.tables.user.key-generator.type=SNOWFLAKE
spring.shardingsphere.sharding.tables.user.key-generator.props.worker.id=1
spring.shardingsphere.sharding.tables.user.table-strategy.inline.sharding-column=ustatus
spring.shardingsphere.sharding.tables.user.table-strategy.inline.algorithm-expression=t_user_$->{ustatus.toInteger()%2+1}

#绑定表
spring.shardingsphere.sharding.binding-tables[0]=user,t_dict

spring.shardingsphere.props.sql.show=true
spring.main.allow-bean-definition-overriding=true
```

(8) 读写分离配置

```java
spring.shardingsphere.datasource.names=m0,s0

spring.shardingsphere.datasource.m0.type=com.alibaba.druid.pool.DruidDataSource
spring.shardingsphere.datasource.m0.driver-class-name=com.mysql.cj.jdbc.Driver
spring.shardingsphere.datasource.m0.url=jdbc:mysql://192.168.159.172:3306/coursedb?serverTimezone=GMT%2B8
spring.shardingsphere.datasource.m0.username=root
spring.shardingsphere.datasource.m0.password=root

spring.shardingsphere.datasource.s0.type=com.alibaba.druid.pool.DruidDataSource
spring.shardingsphere.datasource.s0.driver-class-name=com.mysql.cj.jdbc.Driver
spring.shardingsphere.datasource.s0.url=jdbc:mysql://192.168.159.167:3306/coursedb?serverTimezone=GMT%2B8
spring.shardingsphere.datasource.s0.username=root
spring.shardingsphere.datasource.s0.password=root

spring.shardingsphere.sharding.master-slave-rules.ds0.master-data-source-name=m0
spring.shardingsphere.sharding.master-slave-rules.ds0.slave-data-source-names[0]=s0
spring.shardingsphere.sharding.tables.t_dict.actual-data-nodes=ds0.t_dict

spring.shardingsphere.sharding.tables.t_dict.key-generator.column=dict_id
spring.shardingsphere.sharding.tables.t_dict.key-generator.type=SNOWFLAKE
spring.shardingsphere.sharding.tables.t_dict.key-generator.props.worker.id=1

spring.shardingsphere.props.sql.show = true
spring.main.allow-bean-definition-overriding=true
```

4、ShardingJDBC使用问题分析

1)、分库分表，其实围绕的都是一个核心问题，就是单机数据库容量的问题。我们要了解，在面对这个问题时，解决方案是很多的，并不止分库分表这一种。但是ShardingSphere的这种分库分表，是希望在软件层面对硬件资源进行管理，从而便于对数据库的横向扩展，这无疑是成本很小的一种方式。

 2)、一般情况下，如果单机数据库容量撑不住了，应先从缓存技术着手降低对数据库的访问压力。如果缓存使用过后，数据库访问量还是非常大，可以考虑数据库读写分离策略。如果数据库压力依然非常大，且业务数据持续增长无法估量，最后才考虑分库分表，单表拆分数据应控制在1000万以内。

 当然，随着互联网技术的不断发展，处理海量数据的选择也越来越多。在实际进行系统设计时，最好是用MySQL数据库只用来存储关系性较强的热点数据，而对海量数据采取另外的一些分布式存储产品。例如PostGreSQL、VoltDB甚至HBase、Hive、ES等这些大数据组件来存储。

 3)、从上一部分ShardingJDBC的分片算法中我们可以看到，由于SQL语句的功能实在太多太全面了，所以分库分表后，对SQL语句的支持，其实是步步为艰的，稍不小心，就会造成SQL语句不支持、业务数据混乱等很多很多问题。所以，实际使用时，我们会建议这个分库分表，能不用就尽量不要用。

 如果要使用优先在OLTP场景下使用，优先解决大量数据下的查询速度问题。而在OLAP场景中，通常涉及到非常多复杂的SQL，分库分表的限制就会更加明显。当然，这也是ShardingSphere以后改进的一个方向。

 4)、如果确定要使用分库分表，就应该在系统设计之初开始对业务数据的耦合程度和使用情况进行考量，尽量控制业务SQL语句的使用范围，将数据库往简单的增删改查的数据存储层方向进行弱化。并首先详细规划垂直拆分的策略，使数据层架构清晰明了。而至于水平拆分，会给后期带来非常非常多的数据问题，所以应该谨慎、谨慎再谨慎。一般也就在日志表、操作记录表等很少的一些边缘场景才偶尔用用。

5、ShardingSphere的SQL使用限制

https://shardingsphere.apache.org/document/current/cn/features/sharding/use-norms/sql/

三、ShardingSphere内核原理及核心源码剖析

1、ShardingSphere分库分表内核原理详解

ShardingSphere 的 3 个产品的数据分片主要流程是完全一致的。 核心由 `SQL 解析 => 执行器优化 => SQL 路由 => SQL 改写 => SQL 执行 => 结果归并`的流程组成。

<img src="https://shardingsphere.apache.org/document/current/img/sharding/sharding_architecture_cn.png" alt="分片架构图" style="zoom:50%;" />

（1）解析引擎

解析过程分为词法解析和语法解析。 词法解析器用于将 SQL 拆解为不可再分的原子符号，称为 Token。并根据不同数据库方言所提供的字典，将其归类为关键字，表达式，字面量和操作符。 再使用语法解析器将词法解析器的输出转换为抽象语法树。

例如， SQL：SELECT id, name FROM t_user WHERE status = 'ACTIVE' AND age > 18

解析之后的为抽象语法树见下图。

<img src="https://shardingsphere.apache.org/document/current/img/sharding/sql_ast.png" alt="SQL抽象语法树" style="zoom:50%;" />

为了便于理解，抽象语法树中的关键字的 Token 用绿色表示，变量的 Token 用红色表示，灰色表示需要进一步拆分。

最后，通过 `visitor` 对抽象语法树遍历构造域模型，通过域模型（`SQLStatement`）去提炼分片所需的上下文，并标记有可能需要改写的位置。 供分片使用的解析上下文包含查询选择项（Select Items）、表信息（Table）、分片条件（Sharding Condition）、自增主键信息（Auto increment Primary Key）、排序信息（Order By）、分组信息（Group By）以及分页信息（Limit、Rownum、Top）。 SQL 的一次解析过程是不可逆的，一个个 Token 按 SQL 原本的顺序依次进行解析，性能很高。 考虑到各种数据库 SQL 方言的异同，在解析模块提供了各类数据库的 SQL 方言字典。

（2）路由引擎

<img src="https://shardingsphere.apache.org/document/current/img/sharding/route_architecture.png" alt="路由引擎结构" style="zoom:50%;" />

（3）改写引擎

![](https://shardingsphere.apache.org/document/current/img/sharding/rewrite_architecture_cn.png)

（4）执行引擎

<img src="https://shardingsphere.apache.org/document/current/img/sharding/execute_architecture_cn.png" alt="执行引擎流程图" style="zoom:50%;" />

（5）归并引擎

<img src="https://shardingsphere.apache.org/document/current/img/sharding/merge_architecture_cn.png" alt="归并引擎结构" style="zoom:50%;" />

3、ShardingSphere的SPI扩展点总结

ShardingSphere为了兼容更多的应用场景，在源码中保留了大量的SPI扩展点。所以在看源码之前，需要对JAVA的SPI机制有足够的了解。

SPI的全名为：Service Provider Interface。在java.util.ServiceLoader的文档里有比较详细的介绍。

简单的总结下 Java SPI 机制的思想。我们系统里抽象的各个模块，往往有很多不同的实现方案，比如日志模块的方案，xml解析模块、jdbc模块的方案等。面向的对象的设计里，我们一般推荐模块之间基于接口编程，模块之间不对实现类进行硬编码。

一旦代码里涉及具体的实现类，就违反了可拔插的原则，如果需要替换一种实现，就需要修改代码。为了实现在模块装配的时候能不在程序里动态指明，这就需要一种服务发现机制。

Java SPI 就是提供这样的一个机制：为某个接口寻找服务实现的机制。有点类似IOC的思想，就是将装配的控制权移到程序之外，在模块化设计中这个机制尤其重要

Java SPI 的具体约定为:当服务的提供者，提供了服务接口的一种实现之后，在jar包的META-INF/services/目录里同时创建一个以服务接口命名的文件。该文件里就是实现该服务接口的具体实现类。

而当外部程序装配这个模块的时候，就能通过该jar包META-INF/services/里的配置文件找到具体的实现类名，并装载实例化，完成模块的注入。

基于这样一个约定就能很好的找到服务接口的实现类，而不需要再代码里制定。jdk提供服务实现查找的一个工具类：java.util.ServiceLoader。

https://shardingsphere.apache.org/document/current/cn/dev-manual/sharding/
