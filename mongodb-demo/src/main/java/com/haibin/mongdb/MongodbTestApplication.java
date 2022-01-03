package com.haibin.mongdb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Hello world!
 *
 */
@SpringBootApplication
public class MongodbTestApplication
{
    public static void main( String[] args )
    {
        SpringApplication.run(MongodbTestApplication.class,args);
        System.out.println( "Hello World!" );
    }
}
