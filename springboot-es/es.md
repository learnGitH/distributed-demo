docker启动es：

docker run -e ES_JAVA_OPTS="-Xms256m -Xmx256m" -d -p 9200:9200 -p 9300:9300 --name ES01 镜像id

参考文档：
https://www.elastic.co/cn/
https://www.elastic.co/cn/start
https://www.baeldung.com/elasticsearch-jest
https://www.cnblogs.com/lori/p/13255199.html
https://juejin.cn/post/6844903873384038414#heading-3
https://github.com/ameizi/elasticsearch-jest-example/blob/master/src/main/java/net/aimeizi/client/jest/JestExample.java
https://blog.csdn.net/laoyang360/article/details/77146063