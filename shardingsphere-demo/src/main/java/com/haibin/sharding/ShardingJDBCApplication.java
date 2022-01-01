package com.haibin.sharding;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.swing.*;

/**
 * Hello world!
 *
 */
@MapperScan("com.haibin.sharding.mapper")
@SpringBootApplication
public class ShardingJDBCApplication
{
    public static void main( String[] args )
    {
        SpringApplication.run(ShardingJDBCApplication.class,args);
    }
}
