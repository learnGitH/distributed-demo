package com.haibin.productcenter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Hello world!
 *
 */
@SpringBootApplication
@RestController
public class ProductCenterApplication
{
    @Value("${server.port}")
    private String port;

    @Value( "${spring.application.name}" )
    private String name;

    @GetMapping("/getInfo")
    public String getServerPortAndName(){
        return  this.name +" : "+ this.port;
    }

    public static void main( String[] args)
    {
        SpringApplication.run(ProductCenterApplication.class, args);
    }
}
