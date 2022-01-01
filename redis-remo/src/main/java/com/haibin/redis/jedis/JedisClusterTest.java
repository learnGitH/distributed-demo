package com.haibin.redis.jedis;

import redis.clients.jedis.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

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
