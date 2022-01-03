# 一、mongodb的特性

1、属于应用型数据库，类似于Mysql,存储海量数据

2、支持副本机制，分片机制，为分布式而生

3、以JSON为数据模型的文档型数据库（{nickName:"admin",age:18}）

4、可以动态建模，没有特定的字段格式（可随时增加或减少字段）

# 二、MongoDB VS 关系型数据库

|              | MongoDB                        | 关系型数据库          |
| ------------ | ------------------------------ | --------------------- |
| 数据模型     | 文档模型/关系模型              | 关系模型              |
| 高可用       | 复制集                         | 集群                  |
| 横向扩展能力 | 原生支持数据分片               | 第三方插件（mycat等） |
| 索引支持     | B+Tree，全文索引，地理位置索引 | B+Tree                |
| 扩展方式     | 垂直扩展+水平扩展              | 垂直扩展              |

# 三、MongoDB安装

1.获取安装包：wget https://fastdl.mongodb.org/linux/mongodb-linux-x86_64-rhel70-4.4.2.tgz              

2.进行解压： tar -xvzf  mongodb-linux-x86_64-rhel70-4.4.2.tgz

3.添加到系统执行路径下面( ~/.bashrc)

vim ~/.bashrc

在if和if之间增加：export PATH=$PATH:<你机器MongoDB bin目录，如：/usr/local/mongodb/mongodb-linux-x86_64-rhel70-4.4.2/bin>      

执行 source ~/.bashrc        

4.创建数据目录：mkdir -p /data/db # 这个路径是MongoDB默认的数据存放路径

5.启动MongoDB服务

前台启动：mongod          \# 如果你不希望使用的默认数据目录可以通过  添加 --dbpath 参数指定路径              

![](http://inews.gtimg.com/newsapp_ls/0/14374481694/0)

后台启动：

需要先创建一个日志文件：

mkdir -p /data/db/logpath/

cd /data/db/logpath

touch output.out

mongod --fork --logpath /data/db/logpath/output.out

![](http://inews.gtimg.com/newsapp_ls/0/14374497217/0)

6.安装文档：https://docs.mongodb.com/guides/server/install/

# 四、客户端使用（mongo shell,用来操作MongoDB的javascript客户端界面）

1.连接服务：mongo --host <HOSTNAME> --port <PORT>   # 如果在本机使用的都是默认参数，也可以直接忽略所有参数              

2.设置密码:

use admin  # 设置密码需要切换到admin库 

db.createUser(  {    user: "gj",    pwd: "gj123",    roles: [ "root" ]  } )

 show users # 查看所有用户信息

<img src="https://note.youdao.com/yws/public/resource/5e038498891617c552667b853742fdc1/xmlnote/9830F2E97BA9410582EBFF7FAA40CB31/29852" alt="https://note.youdao.com/yws/public/resource/5e038498891617c552667b853742fdc1/xmlnote/9830F2E97BA9410582EBFF7FAA40CB31/29852" style="zoom:80%;" />                 

3.停服务

db.shutdownServer()  # 停掉服务

4.退出mongo：exit

5.以授权模式启动：mongod --auth --fork --logpath /data/db/logpath/output.out

6.授权方式连接：mongo -u gj

7.连接后通过help查看能进行怎样的操作

![https://note.youdao.com/yws/public/resource/5e038498891617c552667b853742fdc1/xmlnote/5ADB8928B7E241C5A47D09EA8C954709/29860](https://note.youdao.com/yws/public/resource/5e038498891617c552667b853742fdc1/xmlnote/5ADB8928B7E241C5A47D09EA8C954709/29860)

8.安全说明

MongoDB基于安全性考虑，默认安装后只会绑定本地回环 IP 127.0.0.1, 可以通过启动服务时，指定绑定的IP  如 只允许通过 IP:  192.168.109.200 访问： mongod --bind_ip 192.168.109.200

这时登录需要通过：mongo -host 192.168.109.200 -u  gj

9.文档：https://docs.mongodb.com/manual/tutorial/getting-started/

db：# 显示当前所在的数据库 

use example ：# 切换数据库

show tables: 	#查看有哪些集合

# 五、UI客户端访问

https://docs.mongodb.com/compass/master/install

<img src="https://note.youdao.com/yws/public/resource/5e038498891617c552667b853742fdc1/xmlnote/09472B48D2F14195A8C169DDFF8E4E1F/29858" alt="https://note.youdao.com/yws/public/resource/5e038498891617c552667b853742fdc1/xmlnote/09472B48D2F14195A8C169DDFF8E4E1F/29858" style="zoom:80%;" />              

通过UI客户端去连接需要注意的：

1.需要绑定ip:mongod --bind_ip 192.168.159.138 --auth --fork --logpath /data/db/logpath/output.out

2.UI客户端配置

![](http://inews.gtimg.com/newsapp_ls/0/14374592987/0)

# 六、基本操作

1、新增数据

```MQL
db.集合.insertOne(<JSON对象>)		//添加单个文档
db.集合.insertMany([{<JSON对象1>},{<JSON对象2>}])		//批量添加文档
db.集合.insert()					//添加单个文档
```

开始创建文档：

```MQL
db.collection.insertOne(
doc,
{
	writeConcern: 安全级别			//可选字段
}
)
```

writeConcern 定义了本次文档创建操作的安全写级别简单来说， 安全写级别用来判断一次数据库写入操作是否成功，安全写级别越高，丢失数据的风险就越低，然而写入操作的延迟也可能更高。

writeConcern 决定一个写操作落到多少个节点上才算成功。 writeConcern的取值包括

0： 发起写操作，不关心是否成功

1- 集群中最大数据节点数： 写操作需要被复制到指定节点数才算成功

majority: 写操作需要被复制到大多数节点上才算成功

发起写操作的程序将阻塞到写操作到达指定的节点数为止

```MQL
db.emp.insertOne({name:"haibin",age:20,sex:"m"});

{
	"acknowledged" : true,
	"insertedId" : ObjectId("61d2638d783b7c316fed14fa")
}

db.emp.find();
{ "_id" : ObjectId("61d2638d783b7c316fed14fa"), "name" : "haibin", "age" : 20, "sex" : "m" }
```

插入文档时，如果没有显示指定主键，MongoDB将默认创建一个主键，字段固定为_id,ObjectId() 可以快速生成的12字节id 作为主键，**ObjectId** 前四个字节代表了主键生成的时间，精确到秒。主键ID在客户端驱动生成，一定程度上代表了顺序性，但不保证顺序性， 可以通过ObjectId("id值").getTimestamp() 获取创建时间。

创建多个文档：

```MQL
db.collection.insertMany(
[ {doc},{doc},.....],
{
	writeConcern: doc,
	ordered: true/false
}
)
```

ordered:  觉得是否按顺序进行写入

顺序写入时，一旦遇到错误，便会退出，剩余的文档无论正确与否，都不会写入

乱序写入，则只要文档可以正确写入就会正确写入，不管前面的文档是否是错误的文档

MongoDB以集合（collection）的形式组织数据，collection 相当于关系型数据库中的表，如果collection不存在，当你对不存在的collection进行操作时，将会自动创建一个collection

```MQL
db.inventory.insertMany([
   { item: "journal", qty: 25, status: "A", size: { h: 14, w: 21, uom: "cm" }, tags: [ "blank", "red" ] },
   { item: "notebook", qty: 50, status: "A", size: { h: 8.5, w: 11, uom: "in" }, tags: [ "red", "blank" ] },
   { item: "paper", qty: 10, status: "D", size: { h: 8.5, w: 11, uom: "in" }, tags: [ "red", "blank", "plain" ] },
   { item: "planner", qty: 0, status: "D", size: { h: 22.85, w: 30, uom: "cm" }, tags: [ "blank", "red" ] },
   { item: "postcard", qty: 45, status: "A", size: { h: 10, w: 15.25, uom: "cm" }, tags: [ "blue" ] }
]);
```

上面创建都是自动创建_id，也可以自己指定id的值：

```MQL
db.emp.insertOne({_id:1,name:"haibin",age:20,sex:"m"});
```

可以指定复合主键：

```MQL
db.demeDoc.insert({_id:{product_name:1,product_type:2},supplierId:"001",create_Time:new Date()});

```

注意复合主键，字段顺序换了，会当做不同的对象被创建，即使内容完全一致

2、查询数据

(1)整个文档查询

db.inventory.find();   		//查询所有的文档

db.inventory.find({}).pretty();		//返回格式化后的文档

```MQL
//查询所有的文档
db.inventory.find();
{ "_id" : ObjectId("61d26796c4ca479c37cfc226"), "item" : "journal", "qty" : 25, "status" : "A", "size" : { "h" : 14, "w" : 21, "uom" : "cm" }, "tags" : [ "blank", "red" ] }
{ "_id" : ObjectId("61d26796c4ca479c37cfc227"), "item" : "notebook", "qty" : 50, "status" : "A", "size" : { "h" : 8.5, "w" : 11, "uom" : "in" }, "tags" : [ "red", "blank" ] }
{ "_id" : ObjectId("61d26796c4ca479c37cfc228"), "item" : "paper", "qty" : 10, "status" : "D", "size" : { "h" : 8.5, "w" : 11, "uom" : "in" }, "tags" : [ "red", "blank", "plain" ] }
{ "_id" : ObjectId("61d26796c4ca479c37cfc229"), "item" : "planner", "qty" : 0, "status" : "D", "size" : { "h" : 22.85, "w" : 30, "uom" : "cm" }, "tags" : [ "blank", "red" ] }
{ "_id" : ObjectId("61d26796c4ca479c37cfc22a"), "item" : "postcard", "qty" : 45, "status" : "A", "size" : { "h" : 10, "w" : 15.25, "uom" : "cm" }, "tags" : [ "blue" ] }

//返回格式化后的文档
db.inventory.find({}).pretty();
{
	"_id" : ObjectId("61d26796c4ca479c37cfc226"),
	"item" : "journal",
	"qty" : 25,
	"status" : "A",
	"size" : {
		"h" : 14,
		"w" : 21,
		"uom" : "cm"
	},
	"tags" : [
		"blank",
		"red"
	]
}
{
	"_id" : ObjectId("61d26796c4ca479c37cfc227"),
	"item" : "notebook",
	"qty" : 50,
	"status" : "A",
	"size" : {
		"h" : 8.5,
		"w" : 11,
		"uom" : "in"
	},
	"tags" : [
		"red",
		"blank"
	]
}
{
	"_id" : ObjectId("61d26796c4ca479c37cfc228"),
	"item" : "paper",
	"qty" : 10,
	"status" : "D",
	"size" : {
		"h" : 8.5,
		"w" : 11,
		"uom" : "in"
	},
	"tags" : [
		"red",
		"blank",
		"plain"
	]
}
{
	"_id" : ObjectId("61d26796c4ca479c37cfc229"),
	"item" : "planner",
	"qty" : 0,
	"status" : "D",
	"size" : {
		"h" : 22.85,
		"w" : 30,
		"uom" : "cm"
	},
	"tags" : [
		"blank",
		"red"
	]
}
{
	"_id" : ObjectId("61d26796c4ca479c37cfc22a"),
	"item" : "postcard",
	"qty" : 45,
	"status" : "A",
	"size" : {
		"h" : 10,
		"w" : 15.25,
		"uom" : "cm"
	},
	"tags" : [
		"blue"
	]
}

```

(2)条件查询

精准等值查询

 **db.inventory.find( { status: "D" } );**

 **db.inventory.find( { qty: 0 } );**

多条件查询

 **db.inventory.find( { qty: 0, status: "D" } );**

嵌套对象精准查询

 **db.inventory.find( { "size.uom": "in" } );**

返回指定字段

**db.inventory.find( { },** **{ item: 1, status: 1 }** **);**

默认会返回_id 字段， 同样可以通过指定  _id:0 ,不返回_id 字段

条件查询 and 

**db.inventory.find({$and:[{"qty":0},{"status":"A"}]}).pretty();** 

条件查询 or

**db.inventory.find({$or:[{"qty":0},{"status":"A"}]}).pretty();**

```MQL
//精准等值查询
db.inventory.find( { status: "D" } );
{ "_id" : ObjectId("61d26796c4ca479c37cfc228"), "item" : "paper", "qty" : 10, "status" : "D", "size" : { "h" : 8.5, "w" : 11, "uom" : "in" }, "tags" : [ "red", "blank", "plain" ] }
{ "_id" : ObjectId("61d26796c4ca479c37cfc229"), "item" : "planner", "qty" : 0, "status" : "D", "size" : { "h" : 22.85, "w" : 30, "uom" : "cm" }, "tags" : [ "blank", "red" ] }

//多条件查询
db.inventory.find( { qty: 0, status: "D" } );
{ "_id" : ObjectId("61d26796c4ca479c37cfc229"), "item" : "planner", "qty" : 0, "status" : "D", "size" : { "h" : 22.85, "w" : 30, "uom" : "cm" }, "tags" : [ "blank", "red" ] }

//嵌套对象精准查询
db.inventory.find( { "size.uom": "in" } );
{ "_id" : ObjectId("61d26796c4ca479c37cfc227"), "item" : "notebook", "qty" : 50, "status" : "A", "size" : { "h" : 8.5, "w" : 11, "uom" : "in" }, "tags" : [ "red", "blank" ] }
{ "_id" : ObjectId("61d26796c4ca479c37cfc228"), "item" : "paper", "qty" : 10, "status" : "D", "size" : { "h" : 8.5, "w" : 11, "uom" : "in" }, "tags" : [ "red", "blank", "plain" ] }

//返回指定字段
db.inventory.find( { },{ item: 1, status: 1 });
{ "_id" : ObjectId("61d26796c4ca479c37cfc226"), "item" : "journal", "status" : "A" }
{ "_id" : ObjectId("61d26796c4ca479c37cfc227"), "item" : "notebook", "status" : "A" }
{ "_id" : ObjectId("61d26796c4ca479c37cfc228"), "item" : "paper", "status" : "D" }
{ "_id" : ObjectId("61d26796c4ca479c37cfc229"), "item" : "planner", "status" : "D" }
{ "_id" : ObjectId("61d26796c4ca479c37cfc22a"), "item" : "postcard", "status" : "A" }

//条件查询 and
db.inventory.find({$and:[{"qty":0},{"status":"D"}]}).pretty();
{
	"_id" : ObjectId("61d26796c4ca479c37cfc229"),
	"item" : "planner",
	"qty" : 0,
	"status" : "D",
	"size" : {
		"h" : 22.85,
		"w" : 30,
		"uom" : "cm"
	},
	"tags" : [
		"blank",
		"red"
	]
}

//条件查询 or
db.inventory.find({$or:[{"qty":0},{"status":"A"}]}).pretty();
{
	"_id" : ObjectId("61d26796c4ca479c37cfc226"),
	"item" : "journal",
	"qty" : 25,
	"status" : "A",
	"size" : {
		"h" : 14,
		"w" : 21,
		"uom" : "cm"
	},
	"tags" : [
		"blank",
		"red"
	]
}
{
	"_id" : ObjectId("61d26796c4ca479c37cfc227"),
	"item" : "notebook",
	"qty" : 50,
	"status" : "A",
	"size" : {
		"h" : 8.5,
		"w" : 11,
		"uom" : "in"
	},
	"tags" : [
		"red",
		"blank"
	]
}
{
	"_id" : ObjectId("61d26796c4ca479c37cfc229"),
	"item" : "planner",
	"qty" : 0,
	"status" : "D",
	"size" : {
		"h" : 22.85,
		"w" : 30,
		"uom" : "cm"
	},
	"tags" : [
		"blank",
		"red"
	]
}
{
	"_id" : ObjectId("61d26796c4ca479c37cfc22a"),
	"item" : "postcard",
	"qty" : 45,
	"status" : "A",
	"size" : {
		"h" : 10,
		"w" : 15.25,
		"uom" : "cm"
	},
	"tags" : [
		"blue"
	]
}
```

Mongo查询条件和SQL查询对照表：

| SQL              | MQL                      |
| ---------------- | ------------------------ |
| a<>1   或者 a!=1 | { a : {$ne: 1}}          |
| a>1              | { a: {$gt:1}}            |
| a>=1             | { a: {$gte:1}}           |
| a<1              | { a: {$lt:1}}            |
| a<=1             | { a: { $lte:1}}          |
| in               | { a: { $in:[ x, y, z]}}  |
| not in           | { a: { $nin:[ x, y, z]}} |
| a is null        | { a: { $exists: false }} |

```MQL
// db.inventory.find({status:{$ne:"A"}});
{ "_id" : ObjectId("61d26796c4ca479c37cfc228"), "item" : "paper", "qty" : 10, "status" : "D", "size" : { "h" : 8.5, "w" : 11, "uom" : "in" }, "tags" : [ "red", "blank", "plain" ] }
{ "_id" : ObjectId("61d26796c4ca479c37cfc229"), "item" : "planner", "qty" : 0, "status" : "D", "size" : { "h" : 22.85, "w" : 30, "uom" : "cm" }, "tags" : [ "blank", "red" ] }

//db.inventory.find({qty:{$in:[45,50]}});
{ "_id" : ObjectId("61d26796c4ca479c37cfc227"), "item" : "notebook", "qty" : 50, "status" : "A", "size" : { "h" : 8.5, "w" : 11, "uom" : "in" }, "tags" : [ "red", "blank" ] }
{ "_id" : ObjectId("61d26796c4ca479c37cfc22a"), "item" : "postcard", "qty" : 45, "status" : "A", "size" : { "h" : 10, "w" : 15.25, "uom" : "cm" }, "tags" : [ "blue" ] }
```

（3）逻辑操作符匹配

$not : 匹配筛选条件不成立的文档  

$and : 匹配多个筛选条件同时满足的文档

$or : 匹配至少一个筛选条件成立的文档

$nor :  匹配多个筛选条件全部不满足的文档

```MQL
//构造数据
db.members.insertMany([{nickName:"曹操",points:1000},{nickName:"刘备",points:500}]);

//积分不小于100的
db.members.find({points:{$not:{$lt:100}}});
{ "_id" : ObjectId("61d2704794db62fb785cd8c5"), "nickName" : "曹操", "points" : 1000 }
{ "_id" : ObjectId("61d2704794db62fb785cd8c6"), "nickName" : "刘备", "points" : 500 }

//昵称等于曹操， 积分大于 1000 的文档
db.members.find({$and : [ {nickName:{ $eq : "曹操"}}, {points:{ $gt:999}}]});
{ "_id" : ObjectId("61d2704794db62fb785cd8c5"), "nickName" : "曹操", "points" : 1000 }
当作用在不同的字段上时 可以省略 $and 
db.members.find({nickName:{ $eq : "曹操"}, points:{ $gt:1000}});
当作用在同一个字段上面时可以简化为
db.members.find({points:{ $gte:1000, $lte:2000}});

//$or
db.members.find({$or : [ {nickName:{ $eq : "刘备"}}, {points:{ $gt:1000}}]});
{ "_id" : ObjectId("61d2704794db62fb785cd8c6"), "nickName" : "刘备", "points" : 500 }
```

(4)文档游标

```MQL
> db.members.find().skip(1).count();
2
> db.members.find().skip(1).limit(1).count();
2
> db.members.find().skip(1).limit(1).count(true);
1
```

(5)可以使用 $slice 返回数组中的部分元素

```
db.members.insertOne(
{
 _id: {uid:3,accountType: "qq"},
 nickName:"张飞",
 points:1200,
 address:[
 {address:"xxx",post_no:0000},
 {address:"yyyyy",post_no:0002}
 ]}
);

//返回数组的第一个元素
db.members.find(
{},
{_id:0, 
 nickName:1,
 points:1,
 address: 
    {$slice:1}
});
{ "nickName" : "张飞", "points" : 1200, "address" : [ { "address" : "xxx", "post_no" : 0 } ] }

```

(6)可以使用 elementMatch 进行数组元素进行匹配

```MQL
db.members.insertOne(
{
 _id: {uid:4,accountType: "qq"},
 nickName:"张三",
 points:1200,
 tag:["student","00","IT"]}
);

//查询tag数组中第一个匹配"00" 的元素
db.members.find(
  {},
  {_id:0,
   nickName:1,
   points:1,
   tag: {  $elemMatch:  {$eq:  "00" } }
});
{ "nickName" : "张三", "points" : 1200, "tag" : [ "00" ] }
$elemMatch 和 $ 操作符可以返回数组字段中满足条件的第一个元素
```

3、更新数据

updateOne/updateMany 方法要求更新条件部分必须具有以下之一，否则将报错

$set   给符合条件的文档新增一个字段，有该字段则修改其值

$unset  给符合条件的文档，删除一个字段

$push： 增加一个对象到数组底部 

$pop：从数组底部删除一个对象 

$pull：如果匹配指定的值，从数组中删除相应的对象

$pullAll：如果匹配任意的值，从数据中删除相应的对象

$addToSet：如果不存在则增加一个值到数组

```MQL
db.userInfo.insert([
{ name:"zhansan",
  tag:["90","Programmer","PhotoGrapher"]
},
{  name:"lisi",
   tag:["90","Accountant","PhotoGrapher"] 
}]);

//将tag 中有90 的文档，增加一个字段： flag: 1
db.userInfo.updateMany({tag:"90"},{$set:{flag:1}});
db.userInfo.find();
{ "_id" : ObjectId("61d29bcc94db62fb785cd8c7"), "name" : "zhansan", "tag" : [ "90", "Programmer", "PhotoGrapher" ], "flag" : 1 }
{ "_id" : ObjectId("61d29bcc94db62fb785cd8c8"), "name" : "lisi", "tag" : [ "90", "Accountant", "PhotoGrapher" ], "flag" : 1 }

//只修改一个则用
db.userInfo.updateOne({tag:"90"},{$set:{flag:2}});
db.userInfo.find();
{ "_id" : ObjectId("61d29bcc94db62fb785cd8c7"), "name" : "zhansan", "tag" : [ "90", "Programmer", "PhotoGrapher" ], "flag" : 2 }
{ "_id" : ObjectId("61d29bcc94db62fb785cd8c8"), "name" : "lisi", "tag" : [ "90", "Accountant", "PhotoGrapher" ], "flag" : 1 }

//$unset
db.userInfo.update({name:"zhansan"},{$unset:{flag:2}});

//$push
db.userInfo.update({name:"zhansan"},{$push:{type:1}});
```

更新模板：db.collection.update(<query>,<update>,<options>)

<query>定义了更新时的筛选条件

<update>文档提供了更新内容

<options>声明了一些更新操作的参数

如果只包含更新操作符，db.collection.update() 将会使用update更新集合中符合筛选条件的文档中的特定字段。

默认只会更新第一个匹配的值，可以通过设置  options {multi: true} 设置匹配多个文档并更新

```MQL
db.doc.update(
  {name:"zhangsan"},
  {$set:{ flag: 1 }},
  {multi:true}
);
```

$set 更新或新增字段

$unset删除字段

$rename 重命名字段

$inc 加减字段值

$mul 相乘字段值

$min  采用最小值  

$max  次用最大值

4、删除数据

```MQL
//删除文档
db.collection.remove(<query>,<options>)
默认情况下，会删除所有满足条件的文档， 可以设定参数 { justOne:true},只会删除满足添加的第一条文档
db.members.remove({nickName:"张三"});

//删除集合
db.collection.drop( { writeConcern:<doc>})
<doc> 定义了本次删除集合操作的安全写级别 
这个指令不但删除集合内的所有文档，且删除集合的索引
db.demeDoc.drop();

db.collection.remove 只会删除所有的文档，直接使用remve删除所有文档效率比较低，可以使用 drop 删除集合，才重新创建集合以及索引。
```

# 七、Mongodb聚合操作

MongoDB聚合框架是一个计算框架作用在一个或几个集合对集合中的数据进行一系列运算将这些数据转化为期望的形式

初始化测试数据：

```MQL
db.orders.insertMany(
[
{
 zip:"000001",
 phone:"13101010101",
 name:"LiuBei",
 status:"created",
 shippingFee:10,
 orderLines:[
 	{product:"Huawei Meta30 Pro",sku:"2002",qty:100,price:6000,cost:5599},
 	{product:"Huawei Meta40 Pro",sku:"2003",qty:10,price:7000,cost:6599},
 	{product:"Huawei Meta40 5G",sku:"2004",qty:80,price:4000,cost:3700}
 ]
},

{
 zip:"000001",
 phone:"13101010101",
 name:"LiuBei",
 status:"created",
 shippingFee:10,
 orderLines:[
 	{product:"Huawei Meta30 Pro",sku:"2002",qty:100,price:6000,cost:5599},
 	{product:"Huawei Meta40 Pro",sku:"2003",qty:10,price:7000,cost:6599},
 	{product:"Huawei Meta40 5G",sku:"2004",qty:80,price:4000,cost:3700}
 ]
},

{
 zip:"000001",
 phone:"13101010101",
 name:"LiuBei",
 status:"created",
 shippingFee:10,
 orderLines:[
 	{product:"Huawei Meta30 Pro",sku:"2002",qty:100,price:6000,cost:5599},
 	{product:"Huawei Meta40 Pro",sku:"2003",qty:10,price:7000,cost:6599},
 	{product:"Huawei Meta40 5G",sku:"2004",qty:80,price:4000,cost:3700}
 ]
}]
);
```

新增两个字段，值为每个订单的原件总额和订单总额

```MQL
db.orders.aggregate([{$addFields: {
  totalPrice:{  $sum: "$orderLines.price"},
  totalCost: {  $sum: "$orderLines.cost"},
}}]);
{
	"_id" : ObjectId("61d2b6ad94db62fb785cd8c9"),
	"zip" : "000001",
	"phone" : "13101010101",
	"name" : "LiuBei",
	"status" : "created",
	"shippingFee" : 10,
	"orderLines" : [
		{
			"product" : "Huawei Meta30 Pro",
			"sku" : "2002",
			"qty" : 100,
			"price" : 6000,
			"cost" : 5599
		},
		{
			"product" : "Huawei Meta40 Pro",
			"sku" : "2003",
			"qty" : 10,
			"price" : 7000,
			"cost" : 6599
		},
		{
			"product" : "Huawei Meta40 5G",
			"sku" : "2004",
			"qty" : 80,
			"price" : 4000,
			"cost" : 3700
		}
	],
	"totalPrice" : 17000,
	"totalCost" : 15898
}
{
	"_id" : ObjectId("61d2b6ad94db62fb785cd8ca"),
	"zip" : "000001",
	"phone" : "13101010101",
	"name" : "LiuBei",
	"status" : "created",
	"shippingFee" : 10,
	"orderLines" : [
		{
			"product" : "Huawei Meta30 Pro",
			"sku" : "2002",
			"qty" : 100,
			"price" : 6000,
			"cost" : 5599
		},
		{
			"product" : "Huawei Meta40 Pro",
			"sku" : "2003",
			"qty" : 10,
			"price" : 7000,
			"cost" : 6599
		},
		{
			"product" : "Huawei Meta40 5G",
			"sku" : "2004",
			"qty" : 80,
			"price" : 4000,
			"cost" : 3700
		}
	],
	"totalPrice" : 17000,
	"totalCost" : 15898
}
{
	"_id" : ObjectId("61d2b6ad94db62fb785cd8cb"),
	"zip" : "000001",
	"phone" : "13101010101",
	"name" : "LiuBei",
	"status" : "created",
	"shippingFee" : 10,
	"orderLines" : [
		{
			"product" : "Huawei Meta30 Pro",
			"sku" : "2002",
			"qty" : 100,
			"price" : 6000,
			"cost" : 5599
		},
		{
			"product" : "Huawei Meta40 Pro",
			"sku" : "2003",
			"qty" : 10,
			"price" : 7000,
			"cost" : 6599
		},
		{
			"product" : "Huawei Meta40 5G",
			"sku" : "2004",
			"qty" : 80,
			"price" : 4000,
			"cost" : 3700
		}
	],
	"totalPrice" : 17000,
	"totalCost" : 15898
}

//第一阶段：针对整个集合添加两个字段：totalPrice订单原件总额，totalCost订单实际总额，并将结果传到第二
//阶段。第二阶段：按订单总价进行排序
db.orders.aggregate([{$addFields:{totalPrice:{$sum:"$orderLines.price"},totalCost:{$sum:"$orderLines.cost"}}},{$sort:{totalPrice:-1}}，//..stage2]).pretty();

```

聚合表达式：

获取字段信息:

$<field>:用$指示字段路径

$<field>.<sub field>:使用$和.来指示内嵌文档的路径

常量表达式：

$literal:<value>:指示常量<value>

系统变量表达式

$$  使用 $$ 指示系统变量

$$CURRENT  指示管道中当前操作的文档



聚合管道阶段：

$project  对输入文档进行再次投影

$match 对输入文档进行筛选

$limit 筛选出管道内前 N 篇文档

$skip 跳过管道内前N篇文档

$unwind 展开输入文档中的数组字段

$sort 对输入文档进行排序

$lookup 对输入文档进行查询操作

$group 对输入文档进行分组

$out 对管道中的文档输出

准备测试数据：

```MQL
db.userInfo.insertMany(
 [
  {nickName:"zhangsan",age:18},
  {nickName:"lisi",age:20}]
 );
```

$project ： 投影操作， 将原始字段投影成指定名称， 如将 集合中的 nickName 投影成 name

```MQL
db.userInfo.aggregate({$project:{name:"$nickName"}});
{ "_id" : ObjectId("61d2ba7494db62fb785cd8cc"), "name" : "zhangsan" }
{ "_id" : ObjectId("61d2ba7494db62fb785cd8cd"), "name" : "lisi" }

//$project 可以灵活控制输出文档的格式，也可以剔除不需要的字段
db.userInfo.aggregate({$project:{name:"$nickName",_id:0,age:1}});
{ "age" : 18, "name" : "zhangsan" }
{ "age" : 20, "name" : "lisi" }
```

$match： 进行文档筛选

```MQL
db.userInfo.aggregate({$match:{ nickName:"lisi"}});
{ "_id" : ObjectId("61d2ba7494db62fb785cd8cd"), "nickName" : "lisi", "age" : 20 }

注：筛选管道操作和其他管道操作配合时候时，尽量放到开始阶段，这样可以减少后续管道操作符要操作的文档数，提升效率
```

将筛选 和 投影结合使用

```MQL
db.userInfo.aggregate([{$match:{nickName:"lisi"}},{$project:{_id:0,name:"$nickName",age:1}}]);

db.userInfo.aggregate([{$match:{$and:[{age:{$gte:20}},{nickName:{$eq:"lisi"}}]}},{$project:{_id:0,name:"$nickName",age:1}}]);
{ "age" : 20, "name" : "lisi" }
```

$limit 筛选出管道内前 N 篇文档

```
db.userInfo.aggregate({$limit:1});
```

$skip 跳过管道内前N篇文档

```MQL
db.userInfo.aggregate({$skip:1});
注：一般可以使用$limit和$skip做分页的功能
```

$unwind:将数组打平(展开输入文档中的数组字段)

```
db.userInfo.insertOne(
{ "nickName" : "xixi", 
  "age" : 35, 
  "tags" : ["80","IT","BeiJing"]
}
);

db.userInfo.aggregate({$unwind:{path:"$tags"}});
{ "_id" : ObjectId("61d2bf7c94db62fb785cd8ce"), "nickName" : "xixi", "age" : 35, "tags" : "80" }
{ "_id" : ObjectId("61d2bf7c94db62fb785cd8ce"), "nickName" : "xixi", "age" : 35, "tags" : "IT" }
{ "_id" : ObjectId("61d2bf7c94db62fb785cd8ce"), "nickName" : "xixi", "age" : 35, "tags" : "BeiJing" }

// includeArrayIndex:  加上数组元素的索引值， 赋值给后面指定的字段
db.userInfo.aggregate({$unwind:{path:"$tags",includeArrayIndex:"arrIndex"}});
{ "_id" : ObjectId("61d2bf7c94db62fb785cd8ce"), "nickName" : "xixi", "age" : 35, "tags" : "80", "arrIndex" : NumberLong(0) }
{ "_id" : ObjectId("61d2bf7c94db62fb785cd8ce"), "nickName" : "xixi", "age" : 35, "tags" : "IT", "arrIndex" : NumberLong(1) }
{ "_id" : ObjectId("61d2bf7c94db62fb785cd8ce"), "nickName" : "xixi", "age" : 35, "tags" : "BeiJing", "arrIndex" : NumberLong(2) }

//preserveNullAndEmptyArrays:true展开时保留空数组，或者不存在数组字段的文档
db.userInfo.aggregate({$unwind:{path:"$tags",includeArrayIndex:"arrIndex",preserveNullAndEmptyArrays:true}});
{ "_id" : ObjectId("61d2ba7494db62fb785cd8cc"), "nickName" : "zhangsan", "age" : 18, "arrIndex" : null }
{ "_id" : ObjectId("61d2ba7494db62fb785cd8cd"), "nickName" : "lisi", "age" : 20, "arrIndex" : null }
{ "_id" : ObjectId("61d2bf7c94db62fb785cd8ce"), "nickName" : "xixi", "age" : 35, "tags" : "80", "arrIndex" : NumberLong(0) }
{ "_id" : ObjectId("61d2bf7c94db62fb785cd8ce"), "nickName" : "xixi", "age" : 35, "tags" : "IT", "arrIndex" : NumberLong(1) }
{ "_id" : ObjectId("61d2bf7c94db62fb785cd8ce"), "nickName" : "xixi", "age" : 35, "tags" : "BeiJing", "arrIndex" : NumberLong(2) }

```

$sort  对文档进行排序： 1 正序， -1 倒序 

```
db.userInfo.aggregate({$sort:{age:-1}});
```

$lookup:使用单一字段值进行查询（关联查询）

```MQL
//结构
$lookup:{
from: 需要关联的文档,
localField: 本地字段，
foreignField: 外部文档关联字段，
as  作为新的字段，添加到文档中
}

//准备测试数据
db.account.insertMany(
 [
 {_id:1,name:"zhangsan",age:19},
 {_id:2,name:"lisi",age:20}
]
);
db.accountDetail.insertMany(
[
{aid:1,address:["address1","address2"]}
]
);

db.accountDetail.aggregate({$lookup:{from:"account",localField:"aid",foreignField:"_id",as:"field1"}});
{ "_id" : ObjectId("61d2c1b294db62fb785cd8cf"), "aid" : 1, "address" : [ "address1", "address2" ], "field1" : [ { "_id" : 1, "name" : "zhangsan", "age" : 19 } ] }
```

$group :分组

```MQL
$group:{
 _id:  对哪个字段进行分组，
field1:{  accumulator1: expression1   }
}
```

group 聚合操作默认不会对输出结果进行排序

对于group ，聚合操作主要有以下几种:

$addToSet ：将分组中的元素添加到一个数组中，并且自动去重

$avg   返回分组中的平均值， 非数值直接忽略

$first   返回分组中的第一个元素

$last    返回分组中的最后一个元素

$max  返回分组中的最大元素

$min  回分组中的最小元素

$push   创建新的数组，将值添加进去

$sum    求分组数值元素和

```
//构造数据
db.sales.insertMany(
[
{ "_id" : 1, "item" : "abc", "price" : 10, "quantity" : 2, "date" : ISODate("2014-01-01T08:00:00Z") },
{ "_id" : 2, "item" : "jkl", "price" : 20, "quantity" : 1, "date" : ISODate("2014-02-03T09:00:00Z") },
{ "_id" : 3, "item" : "xyz", "price" : 5, "quantity" : 5, "date" : ISODate("2014-02-03T09:05:00Z") },
{ "_id" : 4, "item" : "abc", "price" : 10, "quantity" : 10, "date" : ISODate("2014-02-15T08:00:00Z") },
{ "_id" : 5, "item" : "xyz", "price" : 5, "quantity" : 10, "date" : ISODate("2014-02-15T09:12:00Z") },
{ "_id" : 6, "item" : "xyz", "price" : 5, "quantity" : 10, "date" : ISODate("2014-02-15T09:12:00Z") }
]
);
```

$addToSet：查看每天，卖出哪几种商品项目，按每天分组， 将商品加入到去重数组中 

```
db.sales.aggregate(
   [
     {
       $group:
         {
           _id: { day: { $dayOfYear: "$date"}, year: { $year: "$date" } },
           itemsSold: { $addToSet: "$item" }
         }
     }
   ]
)
{ "_id" : { "day" : 46, "year" : 2014 }, "itemsSold" : [ "abc", "xyz" ] }
{ "_id" : { "day" : 1, "year" : 2014 }, "itemsSold" : [ "abc" ] }
{ "_id" : { "day" : 34, "year" : 2014 }, "itemsSold" : [ "jkl", "xyz" ] }
```

$avg:  求数值的平均值

```
db.sales.aggregate(
   [
     {
       $group:
         {
           _id: "$item",
           avgAmount: { $avg: { $multiply: [ "$price", "$quantity" ] } },
           avgQuantity: { $avg: "$quantity" }
         }
     }
   ]
)
{ "_id" : "jkl", "avgAmount" : 20, "avgQuantity" : 1 }
{ "_id" : "abc", "avgAmount" : 60, "avgQuantity" : 6 }
{ "_id" : "xyz", "avgAmount" : 41.666666666666664, "avgQuantity" : 8.333333333333334 }
```

$push：创建新的数组，存储，每个分组中元素的信息

```MQL
db.sales.aggregate(
   [
     {
       $group:
         {
           _id: { day: { $dayOfYear: "$date"}, year: { $year: "$date" } },
           itemsSold: { $push:  { item: "$item", quantity: "$quantity" } }
         }
     }
   ]
)
{ "_id" : { "day" : 46, "year" : 2014 }, "itemsSold" : [ { "item" : "abc", "quantity" : 10 }, { "item" : "xyz", "quantity" : 10 }, { "item" : "xyz", "quantity" : 10 } ] }
{ "_id" : { "day" : 1, "year" : 2014 }, "itemsSold" : [ { "item" : "abc", "quantity" : 2 } ] }
{ "_id" : { "day" : 34, "year" : 2014 }, "itemsSold" : [ { "item" : "jkl", "quantity" : 1 }, { "item" : "xyz", "quantity" : 5 } ] }
```

group 阶段有 100m内存的使用限制， 默认情况下，如果超过这个限制会直接返回 error，

可以通过设置  allowDiskUse 为 true 来避免异常， allowDiskUse 为 true 将利用临时文件来辅助实现group操作。

$out：将聚合结果写入另一个文档

```
db.sales.aggregate(
   [
     {
       $group:
         {
           _id: { day: { $dayOfYear: "$date"}, year: { $year: "$date" } },
           itemsSold: { $push:  { item: "$item", quantity: "$quantity" } }
         }
     },
    {  $out:"output"}
   ]
)
```

 更多聚合操作参考： https://docs.mongodb.com/manual/reference/operator/aggregation-pipeline/

# 八、Mongodb聚合优化

1. 投影优化

聚合管道可以确定它是否仅需要文档中的字段的子集来获得结果。如果是这样，管道将只使用那些必需的字段，减少通过管道的数据量。 

2. 管道符号执行顺序优化

对于包含投影阶段($project或$unset或$addFields或$set)后跟$match阶段的聚合管道，MongoDB 将$match阶段中不需要在投影阶段计算的值的任何过滤器移动到投影前的新$match阶段

3. $sort +  $match 

如果序列中带有$sort后跟$match，则$match会移动到$sort之前，以最大程度的减少要排序的对象的数量

4. $project/ $unset + $skip序列优化

当有一个$project或$unset之后跟有$skip序列时，$skip 会移至$project之前。 

5.$limit+ $limit合并

当$limit紧接着另一个时 $limit，两个阶段可以合并为一个阶段 $limit，其中限制量为两个初始限制量中的较小者。

6. skip+ $skip 合并 

当$skip紧跟另一个$skip，这两个阶段可合并成一个单一的$skip，其中跳过量为总和的两个初始跳过量。

7. $match+ $match合并

当一个$match紧随另一个紧随其后时 $match，这两个阶段可以合并为一个单独 $match的条件 $and

```
{ $match: { year: 2014 } },
{ $match: { status: "A" } }
```

优化后

```
{ $match: { $and: [ { "year" : 2014 }, { "status" : "A" } ] } }
```

# 九、Mongodb索引原理及实战

默认id索引：在创建集合期间，MongoDB 在_id字段上创建唯一索引。该索引可防止客户端插入两个具有相同值的文档。你不能将_id字段上的index删除。

查询索引：db.collection.getIndexes();

创建索引：db.collection.createIndex(<keys>,<options>)

 <keys>指定了创建索引的字段

准备测试数据：

```MQL
db.members.insertMany(
[
  {name:"zhangsan",age:19,tags:["00","It","SH"]},
  {name:"lisi",age:35,tags:["80","It","BJ"]},     
  {name:"wangwu",age:31,tags:["90","It","SZ"]}
]
);
```

创建一个单键索引：

```
db.members.createIndex({name:1});
```

索引的默认名称是索引键和索引中每个键的方向(即1或-1)的连接，使用下划线作为分隔符， 也可以通过指定 name 来自定义索引名称；

```
db.members.createIndex({name:1}，{ name: "whatever u like."});
```

对于单字段索引和排序操作，索引键的排序顺序(升序或降序)并不重要，因为MongoDB可以从任何方向遍历索引。

查询集合中已经存在的索引:

```
db.members.getIndexes();
```

创建一个复合索引：

MongoDB支持在多个字段上创建用户定义索引，即 复合索引。

复合索引中列出的字段的顺序具有重要意义。如果一个复合索引由 {name: 1, age: -1} 组成，索引首先按name 升序排序，然后在每个name值内按 age 降序 排序。

```
db.members.createIndex({ name:1,age:-1});
```

对于复合索引和排序操作，索引键的排序顺序(升序或降序)可以决定索引是否支持排序操作。

创建多键索引：

MongoDB使用多键索引来索引存储在数组中的内容。如果索引包含数组值的字段，MongoDB为数组的每个元素创建单独的索引项。数组字段中的每一个元素，都会在多键索引中创建一个键

```
db.members.createIndex( { tags:1});
```

索引效果解析：

```
db.collection.explain().method(?)
```

可以使用 explain 进行分析的操作包含 aggregate, count, distinct, find ,group, remove, update

winningPlan： stage 的值含义

COLLSCAN:  整个集合扫描

IXScan: 索引扫描

FETCH:  根据索引指向的文档的地址进行查询

SORT: 需要再内存中排序，效率不高

覆盖索引：

当查询条件和查询的<投影>只包含索引字段时，MongoDB直接从索引返回结果，而不扫描任何文档或将文档带入内存。这些覆盖的查询可能非常高效。

```
db.members.explain().find({ name:"zhangsan"},{_id:0, name:1});
```

这时不需要 fetch, 可以直接从索引中获取数据。

```
db.members.explain().find().sort( {name:1 ,age:-1}) ;
```

使用已创建索引的字段进行排序，能利用索引的顺序，不需要重新排序，效率高

```
 db.members.explain().find().sort( {name:1 ,age: 1}) ;
```

使用未创建索引的字段进行排序， 因为和创建索引时的顺序不一致，所以需要重新排序，效率低

如果需要更改某些字段上已经创建的索引，必须首先删除原有索引，再重新创建新索引，否则，新索引不会包含原有文档

```
db.collection.dropIndex()
//使用索引名称删除索引
db.collection.dropIndex("name_1");
//使用索引定义删除索引
db.collection.dropIndex({name:1,age:-1});
```

```
 db.collection.createIndex(<keys>, <options>)
```

 定义了创建索引时可以使用的一些参数，也可以指定索引的特性

索引的唯一性：

索引的unique属性使MongoDB拒绝索引字段的重复值。除了唯一性约束，唯一索引和MongoDB其他索引功能上是一致的

```
db.members.createIndex({age:1},{unique:true});
```

如果文档中的字段已经出现了重复值，则不可以创建该字段的唯一性索引

如果新增的文档不具备加了唯一索引的字段，则只有第一个缺失该字段的文档可以被添加，索引中该键值被置为null。

复合键索引也可以具有唯一性，这种情况下，不同的文档之间，其所包含的复合键字段值的组合不可以重复。

索引的稀疏性：

索引的稀疏属性可确保索引仅包含具有索引字段的文档的条目。索引会跳过没有索引字段的文档。

可以将稀疏索引与唯一索引结合使用，**以防止插入索引字段值重复的文档**，并跳过索引缺少索引字段的文档。 

```
//准备数据
db.sparsedemo.insertMany([{name:"xxx",age:19},{name:"zs",age:20}]);

//创建 唯一键，稀疏索引
db.sparsedemo.createIndex({name:1},{unique:true ,sparse:true});

//如果同一个索引既具有唯一性，又具有稀疏性，就可以保存多篇缺失索引键值的文档了
db.sparsedemo.insertOne({name:"zs2w",age:20});
db.sparsedemo.insertOne({name:"zs2w2",age:20});
说明：如果只单纯的 唯一键索引，则 缺失索引键的字段，只能有一个，复合键索引也可以具有稀疏性，在这种情况下，只有在缺失复合键所包含的所有字段的情况下，文档才不会被加入到索引中。
```

索引的生存时间：

针对日期字段，或者包含了日期元素的数组字段，可以使用设定了生存时间的索引，来自动删除字段值超过生存时间的文档。

```
//构造数据
db.members.insertMany( [ 

    {
     name:"zhangsanss",
     age:19,tags:["00","It","SH"],
     create_time:new Date()}
    ] );
    
db.members.createIndex({ create_time: 1},{expireAfterSeconds:30 });
在create_time字段上面创建了一个生存时间是30s的索引
```

复合键索引不具备生存时间的特性：

当索引键是包含日期元素的数组字段时，数组中最小的日期将被用来计算文档是否已经过期

数据库使用一个后台线程来监测和删除过期的文档，删除操作可能会有一定的延迟 

索引相关文档：

https://docs.mongodb.com/manual/core/index-compound/#index-ascending-and-descending

https://docs.mongodb.com/manual/reference/operator/aggregation/addFields/#pipe._S_addFields

# 十、mongodb高可用复制集架构

# 十一、mongodb集群分片机制原理

# 十二、mongodb应用与开发实战

最新驱动的地址

https://mongodb.github.io/mongo-java-driver/4.1/driver/

1、java原生客户端

引入maven

```
<dependency>
 	<groupId>org.mongodb</groupId>
    <artifactId>mongodb-driver-sync</artifactId>
    <version>4.2.2</version>
</dependency>
```

2、Spring Boot整合

https://docs.spring.io/spring-data/mongodb/docs/3.1.2/reference/html/#preface

引入maven

```
<dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-mongodb</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.12</version>
        </dependency>
    </dependencies>

```

配置

```
@Configuration
public class AppConfig {

    public @Bean
    MongoClient mongoClient() {
        return MongoClients.create("mongodb://192.168.159.133:27017");
    }

    public @Bean
    MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoClient(), "productdb");
    }
}
```

测试

```
package com.haibin.mongdb.entity;

public class Person {

    private String id;
    private String name;
    private int age;

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public int getAge() {
        return age;
    }

    @Override
    public String toString() {
        return "Person [id=" + id + ", name=" + name + ", age=" + age + "]";
    }

}

package com.haibin.mongdb;

import com.haibin.mongdb.entity.Person;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.core.query.Update.update;

@Component
@Slf4j
public class ApplicationRunnerTest implements ApplicationRunner {

    @Autowired
    private MongoTemplate mongoOps;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Person p = new Person("Joe", 34);

        // 插入文档
        mongoOps.insert(p);
        log.info("Insert: " + p);

        // 查询文档
        p = mongoOps.findById(p.getId(), Person.class);
        log.info("Found: " + p);

        // 更新文档
        mongoOps.updateFirst(query(where("name").is("Joe")), update("age", 35), Person.class);
        p = mongoOps.findOne(query(where("name").is("Joe")), Person.class);
        log.info("Updated: " + p);

        // 删除文档
        mongoOps.remove(p);

        // Check that deletion worked
        List<Person> people =  mongoOps.findAll(Person.class);
        log.info("Number of people = : " + people.size());
        mongoOps.dropCollection(Person.class);
    }
}

```

