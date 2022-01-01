package com.haibin.redis.jedis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Pipeline;

import java.util.Arrays;
import java.util.List;

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
        JedisPool jedisPool = new JedisPool(jedisPoolConfig,"192.168.159.169",6379,3000,null);
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


