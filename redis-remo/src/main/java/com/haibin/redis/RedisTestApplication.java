package com.haibin.redis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Hello world!
 *
 */
@SpringBootApplication
public class RedisTestApplication
{
    public static void main( String[] args )
    {
        SpringApplication.run(RedisTestApplication.class,args);
        System.out.println("started");
    }
}
