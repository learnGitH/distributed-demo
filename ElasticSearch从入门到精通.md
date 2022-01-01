一、ElasticSearch整体介绍

1、概述

Elasticsearch是用Java开发并且是当前最流行的开源的企业级搜索引擎。 能够达到实时搜索，稳定，可靠，快速，安装使用方便。 客户端支持Java、.NET（C#）、PHP、Python、Ruby等多种语言。

官方网站: https://www.elastic.co/ 

下载地址：https://www.elastic.co/cn/start

2、应用场景详解

百度搜索、电商商品搜索等。

3、核心概念

3.1 索引index

一个索引就是一个拥有几分相似特征的文档的集合。比如说，可以有一个客户数据的索引，另一个产品目录的索引，还有一个订单数据的索引。

一个索引由一个名字来标识（必须全部是小写字母的），并且当我们要对对应于这个索引中的文档进行索引、搜索、更新和删除的时候，都要用到这个名字

3.2 映射mapping

ElasticSearch中的映射（Mapping）用来定义一个文档 ,mapping是处理数据的方式和规则方面做一些限制，如某个字段的数据类型、默认 值、分词器、是否被索引等等，这些都是映射里面可以设置的

3.3 字段Field

相当于是数据表的字段|列

3.4 字段类型Type

每一个字段都应该有一个对应的类型，例如：Text、Keyword、Byte等

3.5 文档document

一个文档是一个可被索引的基础信息单元，类似一条记录。文档以JSON（Javascript Object Notation）格式来表示；

3.6 集群cluster

一个集群就是由一个或多个节点组织在一起，它们共同持有整个的数据，并一起提 供索引和搜索功能

3.7 节点node

一个节点是集群中的一个服务器，作为集群的一部分，它存储数据，参与集群的索 引和搜索功能 一个节点可以通过配置集群名称的方式来加入一个指定的集群。默认情况下，每 个节点都会被安排加入到一个叫做“elasticsearch”的集群中 这意味着，如果在网络中启动了若干个节点，并假定它们能够相互发现彼此，它们 将会自动地形成并加入到一个叫做“elasticsearch”的集群中 在一个集群里，可以拥有任意多个节点。而且，如果当前网络中没有运行任何 Elasticsearch节点，这时启动一个节点，会默认创建并加入一个叫 做“elasticsearch”的集群。

3.8 分片和副本 shard&replicas

3.8.1 分片

一个索引可以存储超出单个结点硬件限制的大量数据。比如，一个具有10 亿文档的索引占据1TB的磁盘空间，而任一节点都没有这样大的磁盘空间；或 者单个节点处理搜索请求，响应太慢 为了解决这个问题，Elasticsearch提供了将索引划分成多份的能力，这些 份就叫做分片 当创建一个索引的时候，可以指定你想要的分片的数量 每个分片本身也是一个功能完善并且独立的“索引”，这个“索引”可以 被放置到集群中的任何节点上 分片很重要，主要有两方面的原因 允许水平分割/扩展你的内容容量 允许在分片之上进行分布式的、并行的操作，进而提高性能/吞吐量 至于一个分片怎样分布，它的文档怎样聚合回搜索请求，是完全由 Elasticsearch管理的，对于作为用户来说，这些都是透明的

3.8.2 副本

在一个网络/云的环境里，失败随时都可能发生，在某个分片/节点不知怎 么的就处于离线状态，或者由于任何原因消失了，这种情况下，有一个故障转 移机制是非常有用并且是强烈推荐的。为此目的，Elasticsearch允许你创建分 片的一份或多份拷贝，这些拷贝叫做副本分片，或者直接叫副本 副本之所以重要，有两个主要原因 1) 在分片/节点失败的情况下，提供了高可用性。 注意到复制分片从不与原/主要（original/primary）分片置于同一节点 上是非常重要的 2) 扩展搜索量/吞吐量，因为搜索可以在所有的副本上并行运行 每个索引可以被分成多个分片。一个索引有0个或者多个副本 一旦设置了副本，每个索引就有了主分片和副本分片，分片和副本的数 量可以在索引 创建的时候指定 在索引创建之后，可以在任何时候动态地改变副本的数量，但是不能改变 分片的数量

3、环境搭建

3.1安装Elasticsearch

3.1.1 创建普通用户

ES不能使用root用户来启动，必须使用普通用户来安装启动。这里我们创建一个普通用户以及定义一些常规目录用于存放我们的数据文件以及安装包等。

```
先创建组，再创建用户：
1）创建elasticsearch用户组
   groupadd elasticsearch
2）创建用户haibin 并设置密码
   useradd haibin
   passwd  haibin
3) 创建es文件夹，并修改owner为haibin用户
   mkdir -p /usr/local/es
4) 用户haibin 添加到elasticsearch用户组
   usermod -G elasticsearch haibin
   chown -R haibin /usr/local/es/elasticsearch-7.6.1
5）设置sudo权限
	#为了让普通用户有更大的操作权限，我们一般都会给普通用户设置sudo权限，方便普通用户的操作
	#三台机器使用root用户执行visudo命令然后为es用户添加权限
	visudo

	#在root ALL=(ALL) ALL 一行下面
	#haibin 如下:
	haibin ALL=(ALL) ALL
			 
	#添加成功保存后切换到haibin用户操作
	su haibin

```

3.1.2 上传压缩包并解压

将es的安装包下载并上传到服务器的/user/local/es路径下，然后进行解压

使用haibin用户来执行以下操作，将es安装包上传到指定服务器，并使用es用户执行以下命令解压。

```
# 解压Elasticsearch
  su haibin
  cd /user/local/
  tar -zvxf elasticsearch-7.6.1-linux-x86_64.tar.gz -C /usr/local/es/
```

3.1.3 修改elasticsearch.yml

```
su haibin
mkdir -p /usr/local/es/elasticsearch-7.6.1/log
mkdir -p /usr/local/es/elasticsearch-7.6.1/data
cd /usr/local/es/elasticsearch-7.6.1/config
rm -rf elasticsearch.yml
vim elasticsearch.yml
node.name: node1
path.data: /usr/local/es/es/data
path.logs: /usr/local/es/es/log
network.host: 0.0.0.0
http.port: 9200
#discovery.seed_hosts: ["192.168.159.250"]
cluster.initial_master_nodes: ["node1"]
#bootstrap.system_call_filter: false
#bootstrap.memory_lock: false
#http.cors.enabled: true
#http.cors.allow‐origin: "*"
```

3.1.4 修改jvm.option

修改jvm.option配置文件，调整jvm堆内存大小

```
cd /usr/local/es/elasticsearch-7.6.1/config
vim jvm.options
-Xms2g
-Xmx2g
```

3.1.4 启动报错问题解决

1）max file descriptors [4096] for elasticsearch process likely too low, increase to at least [65536]

ES因为需要大量的创建索引文件，需要大量的打开系统的文件，所以我们需要解除linux系统当中打开文件最大数目的限制，不然ES启动就会抛错

解决:sudo vi /etc/security/limits.conf

添加如下内容: 注意*不要去掉了

```
* soft nofile 65536
* hard nofile 131072
* soft nproc 2048
* hard nproc 4096
```

2)max number of threads [1024] for user [es] likely too low, increase to at least [4096]

解决：sudo vi /etc/security/limits.d/20-nproc.conf          

找到如下内容：

```
* soft nproc 1024#修改为
* soft nproc 4096
```

3）max virtual memory areas vm.max_map_count [65530] likely too low, increase to at least [262144]

解决：编辑 /etc/sysctl.conf，追加以下内容：vm.max_map_count=262144 保存后，执行：sysctl -p     

3.1.5 启动

  nohup /usr/local/es/elasticsearch-7.6.1/bin/elasticsearch 2>&1 &

  后台启动： ./elasticsearch  -d

  启动成功后访问页面：http://192.168.159.252:9200/?pretty                           

3.2 客户端Kibana安装

ES主流客户端Kibana，开放9200端口与图形界面客户端交互

1)下载Kibana放之/usr/local/es目录中

 2)解压文件：tar -zxvf kibana-X.X.X-linux-x86_64.tar.gz

 3)进入/usr/local/es/kibana-X.X.X-linux-x86_64/config目录

使用vi编辑器：vi kibana.yml

```
server.port: 5601
server.host: "服务器IP"
elasticsearch.hosts: ["http://IP:9200"]  #这里是elasticsearch的访问地址
```

启动Kibana: /usr/local/es/kibana-7.6.1-linux-x86_64/bin/kibana              

后台启动kibana: nohup  ./kibana &              

访问Kibana:  http://ip:5601/app/kibana              

3.3 安装IK分词器

我们后续也需要使用Elasticsearch来进行中文分词，所以需要单独给Elasticsearch安装IK分词器插件。以下为具体安装步骤：

1)下载Elasticsearch IK分词器

https://github.com/medcl/elasticsearch-analysis-ik/releases

2)切换到haibin用户，并在es的安装目录下/plugins创建ik

mkdir -p /usr/local/es/elasticsearch-7.6.1/plugins/ik         

3)将下载的ik分词器上传并解压到该目录

```
cd /usr/local/es/elasticsearch-7.6.1/plugins/ik
unzip  elasticsearch-analysis-ik-7.6.1.zip 
```

4)重启Elasticsearch

测试分词效果

```
POST _analyze
{
"analyzer":"standard",
"text":"我爱你中国"
}

POST _analyze
{
"analyzer": "ik_smart",
"text": "中华人民共和国"
 }
#ik_smart:会做最粗粒度的拆分

POST _analyze
{
"analyzer":"ik_max_word",
"text":"我爱你中国"
}
#ik_max_word:会将文本做最细粒度的拆分
```

4、常用分词技术介绍与使用

ES的默认分词设置是standard，这个在中文分词时就比较尴尬了，会单字拆分，比如我搜索关键词“清华大学”，这时候会按“清”，“华”，“大”，“学”去分词，然后搜出来的都是些“清清的河水”，“中华儿女”，“地大物博”，“学而不思则罔”之类的莫名其妙的结果，这里我们就想把这个分词方式修改一下，于是呢，就想到了ik分词器，有两种ik_smart和ik_max_word。

ik_smart会将“清华大学”整个分为一个词，而ik_max_word会将“清华大学”分为“清华大学”，“清华”和“大学”，按需选其中之一就可以了。

修改默认分词方法(这里修改school_index索引的默认分词为：ik_max_word)：

```
PUT /school_index
{
"settings" : {
"index" : {
"analysis.analyzer.default.type": "ik_max_word"
}
}
}
```

5、数据管理详解  

5.1 ES数据管理概述

ES是面向文档(document oriented)的，这意味着它可以存储整个对象或文档(document)。然而它不仅仅是存储，还会索引(index)每个文档的内容使之可以被搜索。在ES中，你可以对文档（而非成行成列的数据）进行索引、搜索、排序、过滤。ES使用JSON作为文档序列化格式。JSON现在已经被大多语言所支持，而且已经成为NoSQL领域的标准格式。

ES存储的一个员工文档的格式示例：

```
{
"email": "584614151@qq.com",
"name": "张三",
"age": 30,
"interests": [ "篮球", "健身" ]
}
```

5.2 基本操作

1）创建索引

格式: PUT /索引名称

```
举例: PUT /es_db
```

2）查询索引

格式: GET /索引名称

```
举例: GET /es_db
```

3）删除索引

格式: DELETE /索引名称

```
举例: DELETE /es_db
```

4）添加文档 

格式: PUT /索引名称/类型/id

```
举例:
PUT /es_db/_doc/1
{
"name": "张三",
"sex": 1,
"age": 25,
"address": "广州天河公园",
"remark": "java developer"
}

PUT /es_db/_doc/2
{
"name": "李四",
"sex": 1,
"age": 28,
"address": "广州荔湾大厦",
"remark": "java assistant"
}

PUT /es_db/_doc/3
{
"name": "rod",
"sex": 0,
"age": 26,
"address": "广州白云山公园",
"remark": "php developer"
}

PUT /es_db/_doc/4
{
"name": "admin",
"sex": 0,
"age": 22,
"address": "长沙橘子洲头",
"remark": "python assistant"
}

PUT /es_db/_doc/5
{
"name": "小明",
"sex": 0,
"age": 19,
"address": "长沙岳麓山",
"remark": "java architect assistant"
}
```

5）修改文档

```
格式: PUT /索引名称/类型/id
举例:
PUT /es_db/_doc/1
{
"name": "白起老师",
"sex": 1,
"age": 25,
"address": "张家界森林公园",
"remark": "php developer assistant"
}
```

注意:POST和PUT都能起到创建/更新的作用

1、需要注意的是==PUT==需要对一个具体的资源进行操作也就是要确定id才能进行==更新/创建，而==POST==是可以针对整个资源集合进行操作的，如果不写id就由ES生成一个唯一id进行==创建==新文档，如果填了id那就针对这个id的文档进行创建/更新

2、PUT只会将json数据都进行替换, POST只会更新相同字段的值

3、PUT与DELETE都是幂等性操作, 即不论操作多少次, 结果都一样

6）查询文档

```
格式: GET /索引名称/类型/id
举例: GET /es_db/_doc/1  
```

7）删除文档

```
格式: DELETE /索引名称/类型/id 
举例: DELETE /es_db/_doc/1
```

5.3 查询操作

5.3.1查询当前类型中的所有文档 _search 

```
格式: GET /索引名称/类型/_search
举例: GET /es_db/_doc/_search
SQL:  select * from student
```

5.3.2 条件查询, 如要查询age等于28岁的 _search?q=*:***

```
格式: GET /索引名称/类型/_search?q=*:***
举例: GET /es_db/_doc/_search?q=age:28
SQL:  select * from student where age = 28
```

5.3.3 范围查询, 如要查询age在25至26岁之间的 _search?q=***[** TO **]  注意: TO 必须为大写

```
格式: GET /索引名称/类型/_search?q=***[25 TO 26]
举例: GET /es_db/_doc/_search?q=age[25 TO 26]
SQL:  select * from student where age between 25 and 26
```

5.3.4 根据多个ID进行批量查询 _mget

```
格式: GET /索引名称/类型/_mget
举例: GET /es_db/_doc/_mget 
{
 "ids":["1","2"]  
 }
SQL:  select * from student where id in (1,2)
```

5.3.5 查询年龄小于等于28岁的 :<=

```
格式: GET /索引名称/类型/_search?q=age:<=**
举例: GET /es_db/_doc/_search?q=age:<=28
SQL:  select * from student where age <= 28
```

5.3.6 查询年龄大于28前的 :>

```
格式: GET /索引名称/类型/_search?q=age:>**
举例: GET /es_db/_doc/_search?q=age:>28
SQL:  select * from student where age > 28
```

5.3.7 分页查询 from=*&size=

```
格式: GET /索引名称/类型/_search?q=age[25 TO 26]&from=0&size=1
举例: GET /es_db/_doc/_search?q=age[25 TO 26]&from=0&size=1
SQL:  select * from student where age between 25 and 26 limit 0, 1
```

5.3.8 对查询结果只输出某些字段 _source=字段,字段

```
格式: GET /索引名称/类型/_search?_source=字段,字段
举例: GET /es_db/_doc/_search?_source=name,age
SQL:  select name,age from student
```

5.3.9 对查询结果排序 sort=字段:desc/asc

```
格式: GET /索引名称/类型/_search?sort=字段 desc
举例: GET /es_db/_doc/_search?sort=age:desc
SQL:  select * from student order by age desc 
```

二、文档批量操作

这里多个文档是指，批量操作多个文档

1.批量获取文档数据

批量获取文档数据是通过_mget的API来实现的

1）URL中不指定index和type

- 请求方式：GET
- 请求地址：_mget
- 功能说明 ： 可以通过ID批量获取不同index和type的数据
- 请求参数：

- - docs : 文档数组参数

- - - _index : 指定index
    - _type : 指定type
    - _id : 指定id
    - _source : 指定要查询的字段

```
 GET _mget
{
"docs": [
{
"_index": "es_db",
"_type": "_doc",
"_id": 1
},
{
"_index": "es_db",
"_type": "_doc",
"_id": 2
}
]
}
```

2）在URL中指定index

- 请求方式：GET
- 请求地址：/{{indexName}}/_mget
- 功能说明 ： 可以通过ID批量获取不同index和type的数据
- 请求参数：

- - docs : 文档数组参数

- - - _index : 指定index
    - _type : 指定type
    - _id : 指定id
    - _source : 指定要查询的字段

```
GET /es_db/_mget
{
"docs": [
{
"_type":"_doc",
"_id": 3
},
{
"_type":"_doc",
"_id": 4
}
]
}
```

3)在URL中指定index和type

- 请求方式：GET
- 请求地址：/{{indexName}}/{{typeName}}/_mget
- 功能说明 ： 可以通过ID批量获取不同index和type的数据
- 请求参数：

- - docs : 文档数组参数

- - - _index : 指定index
    - _type : 指定type
    - _id : 指定id
    - _source : 指定要查询的字段

```
GET /es_db/_doc/_mget
{
"docs": [
{
"_id": 1
},
{
"_id": 2
}
]
}
```

2.批量操作文档数据

批量对文档进行写操作是通过_bulk的API来实现的

- 请求方式：POST
- 请求地址：_bulk
- 请求参数：通过_bulk操作文档，一般至少有两行参数(或偶数行参数)

- - 第一行参数为指定操作的类型及操作的对象(index,type和id)
  - 第二行参数才是操作的数据

参数类似于：

```
{"actionName":{"_index":"indexName", "_type":"typeName","_id":"id"}}
{"field1":"value1", "field2":"value2"}
```

- actionName：表示操作类型，主要有create,index,delete和update

1)批量创建文档create

```
POST _bulk
{"create":{"_index":"article", "_type":"_doc", "_id":3}}
{"id":3,"title":"haibin","content":"haibin666","tags":["java", "面向对象"],"create_time":1554015482530}
{"create":{"_index":"article", "_type":"_doc", "_id":4}}
{"id":4,"title":"haibin2","content":"haibin1122","tags":["java", "面向对象"],"create_time":1554015482530}                       
```

2)普通创建或全量替换index

```
POST _bulk
{"index":{"_index":"article", "_type":"_doc", "_id":3}}
{"id":3,"title":"lalal","content":"laljfas","tags":["java", "面向对象"],"create_time":1554015482530}
{"index":{"_index":"article", "_type":"_doc", "_id":4}}
{"id":4,"title":"bb,"content":"bbmm","tags":["java", "面向对象"],"create_time":1554015482530}
```

- 如果原文档不存在，则是创建
- 如果原文档存在，则是替换(全量修改原文档)

3)批量删除delete

```
POST _bulk
{"delete":{"_index":"article", "_type":"_doc", "_id":3}}
{"delete":{"_index":"article", "_type":"_doc", "_id":4}}
```

4)批量修改update

```
POST _bulk
{"update":{"_index":"article", "_type":"_doc", "_id":3}}
{"doc":{"title":"ES大法必修内功"}}
{"update":{"_index":"article", "_type":"_doc", "_id":4}}
{"doc":{"create_time":1554018421008}}
```

三、DSL语言高级查询

1.Query DSL概述

**Domain Specific Language**

**领域专用语言**

**Elasticsearch provides a ful1 Query DSL based on JSON to define queries**

**Elasticsearch提供了基于JSON的DSL来定义查询。**

DSL由叶子查询子句和复合查询子句两种子句组成。

![](https://p.pstatp.com/origin/pgc-image/19f263470ae04ed4a9f0d950253a4425)

2.无条件查询

无查询条件是查询所有，默认是查询所有的，或者使用match_all表示所有

```
GET /es_db/_doc/_search
{
"query":{
"match_all":{}
}
}
```

3.有查询条件

3.1叶子条件查询(单字段查询条件)

3.1.1模糊匹配

模糊匹配主要是针对文本类型的字段，文本类型的字段会对内容进行分词，对查询时，也会对搜索条件进行分词，然后通过倒排索引查找到匹配的数据，模糊匹配主要通过match等参数来实现

- match : 通过match关键词模糊匹配条件内容
- prefix : 前缀匹配
- regexp : 通过正则表达式来匹配数据

match的复杂用法

match条件还支持以下参数：

- query : 指定匹配的值
- operator : 匹配条件类型

- - and : 条件分词后都要匹配
  - or : 条件分词后有一个匹配即可(默认)

- minmum_should_match : 指定最小匹配的数量

3.1.2 精确匹配

- term : 单个条件相等
- terms : 单个字段属于某个值数组内的值
- range : 字段属于某个范围内的值
- exists : 某个字段的值是否存在
- ids : 通过ID批量查询

3.2组合条件查询(多条件查询)

组合条件查询是将叶子条件查询语句进行组合而形成的一个完整的查询条件

- bool : 各条件之间有and,or或not的关系

- - must : 各个条件都必须满足，即各条件是and的关系
  - should : 各个条件有一个满足即可，即各条件是or的关系
  - must_not : 不满足所有条件，即各条件是not的关系
  - filter : 不计算相关度评分，它不计算_score即相关度评分，效率更高

- constant_score : 不计算相关度评分

**must/filter/shoud/must_not** 等的子条件是通过 **term/terms/range/ids/exists/match** 等叶子条件为参数的

注：以上参数，当只有一个搜索条件时，must等对应的是一个对象，当是多个条件时，对应的是一个数组

3.3连接查询(多文档合并查询)

- 父子文档查询：parent/child
- 嵌套文档查询: nested

3.4DSL查询语言中存在两种：查询DSL（query DSL）和过滤DSL（filter DSL）

它们两个的区别如下图：

![](https://p.pstatp.com/origin/pgc-image/f0bbf0f4bd634505a4e5197691201045)

**query DSL**

在查询上下文中，查询会回答这个问题——“这个文档匹不匹配这个查询，它的相关度高么？”

如何验证匹配很好理解，如何计算相关度呢？ES中索引的数据都会存储一个_score分值，分值越高就代表越匹配。另外关于某个搜索的分值计算还是很复杂的，因此也需要一定的时间。

**filter DSL**

在过滤器上下文中，查询会回答这个问题——“这个文档匹不匹配？”

答案很简单，是或者不是。它不会去计算任何分值，也不会关心返回的排序问题，因此效率会高一点。

过滤上下文 是在使用filter参数时候的执行环境，比如在bool查询中使用must_not或者filter

另外，经常使用过滤器，ES会自动的缓存过滤器的内容，这对于查询来说，会提高很多性能。

3.5Query方式查询:案例

- 根据名称精确查询姓名 term, term查询不会对字段进行分词查询，会采用精确匹配 

  注意: 采用term精确查询, 查询字段映射类型属于为keyword.

```
POST /es_db/_doc/_search
 {
 "query": {
 "term": {
 "name": "admin"
 }
 }
 } 
 SQL: select * from student where name = 'admin'
```

- 根据备注信息模糊查询 match, match会根据该字段的分词器，进行分词查询 

```
POST /es_db/_doc/_search
{
"from": 0,
"size": 2,
"query": {
"match": {
"address": "广州"
}
}
} 
SQL: select * from user where address like '%广州%' limit 0, 2
```

- 多字段模糊匹配查询与精准查询 multi_match

```
POST /es_db/_doc/_search
 {
 "query":{
 "multi_match":{
 "query":"张三",
 "fields":["address","name"]
 }
 }
 } 
 SQL: select * from student  where name like '%张三%' or address like '%张三%' 
```

- 未指定字段条件查询 query_string , 含 AND 与 OR 条件

```
POST /es_db/_doc/_search
{
"query":{
"query_string":{
"query":"广州 OR 长沙"
}
}
}  
```

- 指定字段条件查询 query_string , 含 AND 与 OR 条件

```
POST /es_db/_doc/_search
{
"query":{
"query_string":{
"query":"admin OR 长沙",
"fields":["name","address"]
}
}
} 
```

- 范围查询

注：json请求字符串中部分字段的含义

​	range：范围关键字

​	gte 大于等于

​	lte  小于等于

​	gt 大于

​	lt 小于

​	now 当前时间	

```
POST /es_db/_doc/_search
{
"query" : {
"range" : {
"age" : {
"gte":25,
"lte":28
}
}
}
}  
SQL: select * from user where age between 25 and 28
```

-  分页、输出字段、排序综合查询

```
POST /es_db/_doc/_search
{
"query" : {
"range" : {
"age" : {
"gte":25,
"lte":28
}
}
},
"from": 0,
"size": 2,
"_source": ["name", "age", "book"],
"sort": {"age":"desc"}
} 
```

3.6Filter过滤器方式查询，它的查询不会计算相关性分值，也不会对结果进行排序, 因此效率会高一点，查询的结果可以被缓存

Filter Context 对数据进行过滤 

```
POST /es_db/_doc/_search
{
"query" : {
"bool" : {
"filter" : {
"term":{
"age":25
}
}
}
}
}
```

**总结:**

**1. match**

match：模糊匹配，需要指定字段名，但是输入会进行分词，比如"hello world"会进行拆分为hello和world，然后匹配，如果字段中包含hello或者world，或者都包含的结果都会被查询出来，也就是说match是一个部分匹配的模糊查询。查询条件相对来说比较宽松。

**2. term**

term:  这种查询和match在有些时候是等价的，比如我们查询单个的词hello，那么会和match查询结果一样，但是如果查询"hello world"，结果就相差很大，因为这个输入不会进行分词，就是说查询的时候，是查询字段分词结果中是否有"hello world"的字样，而不是查询字段中包含"hello world"的字样。当保存数据"hello world"时，elasticsearch会对字段内容进行分词，"hello world"会被分成hello和world，不存在"hello world"，因此这里的查询结果会为空。这也是term查询和match的区别。

**3. match_phase**

match_phase：会对输入做分词，但是需要结果中也包含所有的分词，而且顺序要求一样。以"hello world"为例，要求结果中必须包含hello和world，而且还要求他们是连着的，顺序也是固定的，hello that world不满足，world hello也不满足条件。

**4. query_string**

query_string：和match类似，但是match需要指定字段名，query_string是在所有字段中搜索，范围更广泛。

四、文档映射

1.ES中映射可以分为动态映射和静态映射

动态映射： 

在关系数据库中，需要事先创建数据库，然后在该数据库下创建数据表，并创建表字段、类型、长度、主键等，最后才能基于表插入数据。而Elasticsearch中不需要定义Mapping映射（即关系型数据库的表、字段等），在文档写入Elasticsearch时，会根据文档字段自动识别类型，这种机制称之为动态映射。

动态映射规则如下：

![](https://p.pstatp.com/origin/pgc-image/32971bde99a34a159478d1e8f04f7191)

静态映射： 

 静态映射是在Elasticsearch中也可以事先定义好映射，包含文档的各字段类型、分词器等，这种方式称之为静态映射。

2.动态映射(ES根据数据类型, 会自动创建映射)

```
PUT /es_db/_doc/1
{
"name": "Jack",
"sex": 1,
"age": 25,
"book": "java入门至精通",
"address": "广州小蛮腰"
} 
```

3.静态映射

```
PUT /es_db
 {
 "mappings":{
 "properties":{
 "name":{"type":"keyword","index":true,"store":true},
 "sex":{"type":"integer","index":true,"store":true},
 "age":{"type":"integer","index":true,"store":true},
 "book":{"type":"text","index":true,"store":true},
 "address":{"type":"text","index":true,"store":true}
 }
 }
 }
 
 PUT /es_db/_doc/1
{
"name": "Jack",
"sex": 1,
"age": 25,
"book": "elasticSearch入门至精通",
"address": "广州车陂"
}
//获取文档映射
GET /es_db/_mapping
```

五.核心类型（Core datatype）

字符串：string，string类型包含 text 和 keyword。

text：该类型被用来索引长文本，在创建索引前会将这些文本进行分词，转化为词的组合，建立索引；允许es来检索这些词，text类型不能用来排序和聚合。

keyword：该类型不能分词，可以被用来检索过滤、排序和聚合，keyword类型不可用text进行分词模糊检索。

数值型：long、integer、short、byte、double、float

日期型：date

布尔型：boolean

1.keyword 与 text 映射类型的区别

将 book 字段设置为 keyword 映射 （只能精准查询, 不能分词查询，能聚合、排序）

```
POST /es_db/_doc/_search
{
"query": {
"term": {
"book": "elasticSearch入门至精通"
}
}
} 
```

将 book 字段设置为 text 映射能模糊查询, 能分词查询，不能聚合、排序）

```
POST /es_db/_doc/_search
{
"query": {
"match": {
"book": "elasticSearch入门至精通"
}
}
}
```

六.Elasticsearch架构原理

1.Elasticsearch的节点类型

在Elasticsearch主要分成两类节点，一类是Master，一类是DataNode。

1.1  Master节点

在Elasticsearch启动时，会选举出来一个Master节点。当某个节点启动后，然后使用Zen Discovery机制找到集群中的其他节点，并建立连接。

discovery.seed_hosts: ["192.168.159.130", "192.168.159.131", "192.168.159.132"]

并从候选主节点中选举出一个主节点。

cluster.initial_master_nodes: ["node1", "node2","node3"]

Master节点主要负责：

 管理索引（创建索引、删除索引）、分配分片

 维护元数据

 管理集群节点状态

 不负责数据写入和查询，比较轻量级

一个Elasticsearch集群中，只有一个Master节点。在生产环境中，内存可以相对小一点，但机器要稳定。

1.2  DataNode节点

在Elasticsearch集群中，会有N个DataNode节点。DataNode节点主要负责：

数据写入、数据检索，大部分Elasticsearch的压力都在DataNode节点上

在生产环境中，内存最好配置大一些

2.分片和副本机制

2.1  分片（Shard）

 Elasticsearch是一个分布式的搜索引擎，索引的数据也是分成若干部分，分布在不同的服务器节点中

分布在不同服务器节点中的索引数据，就是分片（Shard）。Elasticsearch会自动管理分片，如果发现分片分布不均衡，就会自动迁移

一个索引（index）由多个shard（分片）组成，而分片是分布在不同的服务器上的

2.2  副本

为了对Elasticsearch的分片进行容错，假设某个节点不可用，会导致整个索引库都将不可用。所以，需要对分片进行副本容错。每一个分片都会有对应的副本。

在Elasticsearch中，默认创建的索引为1个分片、每个分片有1个主分片和1个副本分片。

每个分片都会有一个Primary Shard（主分片），也会有若干个Replica Shard（副本分片）

Primary Shard和Replica Shard不在同一个节点上

2.3指定分片、副本数量

```
// 创建指定分片数量、副本数量的索引
PUT /job_idx_shard_temp
{
"mappings":{
"properties":{
"id":{"type":"long","store":true},
"area":{"type":"keyword","store":true},
"exp":{"type":"keyword","store":true},
"edu":{"type":"keyword","store":true},
"salary":{"type":"keyword","store":true},
"job_type":{"type":"keyword","store":true},
"cmp":{"type":"keyword","store":true},
"pv":{"type":"keyword","store":true},
"title":{"type":"text","store":true},
"jd":{"type":"text"}

}
},
"settings":{
"number_of_shards":3,
"number_of_replicas":2
}
}

// 查看分片、主分片、副本分片
GET /_cat/indices?v  
```

3.Elasticsearch重要工作流程

3.1  Elasticsearch文档写入原理

![](https://p.pstatp.com/origin/pgc-image/b2af2ea0a1854c55aad67241c3b86867)

1.选择任意一个DataNode发送请求，例如：node2。此时，node2就成为一个coordinating node（协调节点）

2.计算得到文档要写入的分片

 `shard = hash(routing) % number_of_primary_shards`

 routing 是一个可变值，默认是文档的 _id

3.coordinating node会进行路由，将请求转发给对应的primary shard所在的DataNode（假设primary shard在node1、replica shard在node2）

4.node1节点上的Primary Shard处理请求，写入数据到索引库中，并将数据同步到Replica shard

5.Primary Shard和Replica Shard都保存好了文档，返回client

![](https://p.pstatp.com/origin/pgc-image/7015ae73c8064d5f8c102a8037759e65)

 client发起查询请求，某个DataNode接收到请求，该DataNode就会成为协调节点（Coordinating Node）

 协调节点（Coordinating Node）将查询请求广播到每一个数据节点，这些数据节点的分片会处理该查询请求

 每个分片进行数据查询，将符合条件的数据放在一个优先队列中，并将这些数据的文档ID、节点信息、分片信息返回给协调节点

 协调节点将所有的结果进行汇总，并进行全局排序

 协调节点向包含这些文档ID的分片发送get请求，对应的分片将文档数据返回给协调节点，最后协调节点将数据返回给客户端

4.Elasticsearch准实时索引实现

4.1  溢写到文件系统缓存

当数据写入到ES分片时，会首先写入到内存中，然后通过内存的buffer生成一个segment，并刷到**文件系统缓存**中，数据可以被检索（注意不是直接刷到磁盘）

ES中默认1秒，refresh一次

4.2  写translog保障容错

 在写入到内存中的同时，也会记录translog日志，在refresh期间出现异常，会根据translog来进行数据恢复

等到文件系统缓存中的segment数据都刷到磁盘中，清空translog文件

4.3  flush到磁盘

ES默认每隔30分钟会将文件系统缓存的数据刷入到磁盘

4.4  segment合

 Segment太多时，ES定期会将多个segment合并成为大的segment，减少索引查询时IO开销，此阶段ES会真正的物理删除（之前执行过的delete的数据）

<img src="https://p.pstatp.com/origin/pgc-image/eadc4bf80ef44f1d9a7eb27d4fc145bb" style="zoom: 33%;" />

七、聚合搜索技术深入

