package com.haibin.redis.jedis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;

/**
 * 单机模式jedis连接配置及其案例
 */
public class JedisSingleTest {

    public static void main(String[] args) throws IOException {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(20);
        jedisPoolConfig.setMaxIdle(10);
        jedisPoolConfig.setMinIdle(5);

       // String masterName = "mymaster";
        //Set<String> sentinels = new HashSet<String>();
        //sentinels.add(new HostAndPort("192.168.159.164",26379).toString());
        //sentinels.add(new HostAndPort("192.168.159.164",26380).toString());
        //sentinels.add(new HostAndPort("192.168.159.164",26381).toString());

        //哨兵模式配置
        //JedisSentinelPool jedisSentinelPool = new JedisSentinelPool(masterName,sentinels,jedisPoolConfig,3000,null);

        //主从配置
       // JedisPool jedisPool = new JedisPool(jedisPoolConfig,"192.168.159.164",6379,3000,null);

        //单机配置
        JedisPool jedisPool =  new JedisPool(jedisPoolConfig,"192.168.159.169",6379,3000,null);

        Jedis jedis = null;

        //高可用配置
        /*Set<HostAndPort> jedisClusterNode = new HashSet<HostAndPort>();
        jedisClusterNode.add(new HostAndPort("192.168.159.165",8001));
        jedisClusterNode.add(new HostAndPort("192.168.159.160",8002));
        jedisClusterNode.add(new HostAndPort("192.168.159.158",8003));
        jedisClusterNode.add(new HostAndPort("192.168.159.165",8004));
        jedisClusterNode.add(new HostAndPort("192.168.159.160",8005));
        jedisClusterNode.add(new HostAndPort("192.168.159.158",8006));
*/
        //JedisCluster jedisCluster = null;

        try{
            //从redis连接池里拿出一个连接执行命令
            //jedis = jedisSentinelPool.getResource();
            //jedisCluster = new JedisCluster(jedisClusterNode,6000,5000,10,"haibin",jedisPoolConfig);
            jedis = jedisPool.getResource();
            //System.out.println(jedisCluster.set("single","haibin"));
            //System.out.println(jedisCluster.get("single"));
            System.out.println(jedis.set("single","haibin"));
            System.out.println(jedis.get("single"));

            //管道示例
            /*Pipeline pl = jedis.pipelined();
            for (int i = 0; i < 10; i++){
                pl.incr("pipelineKey");
                pl.set("haibin" + i,"haibin");
            }
            List<Object> results = pl.syncAndReturnAll();
            System.out.println(results);*/

            //lua脚本模拟一个商品减库存的原子操作
            /*jedis.set("product_count_10016","15");
            String script = " local count = redis.call('get', KEYS[1]) " +
                    " local a = tonumber(count) " +
                    " local b = tonumber(ARGV[1]) " +
                    " if a >= b then " +
                    " redis.call('set', KEYS[1], a-b) " +
                    " return 1 " +
                    " end " +
                    " return 0 ";
            Object obj = jedis.eval(script, Arrays.asList("product_count_10016"),Arrays.asList("10"));
            System.out.println(obj);*/

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

   