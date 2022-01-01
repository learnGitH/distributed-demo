package com.haibin.springdemo.config;

import com.haibin.springdemo.entity.SpringBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "com.haibin.springdemo.entity")
public class AppConfig {

    @Bean(initMethod = "initMethod",destroyMethod = "destroyMethod")
    public SpringBean springBean(){
        return new SpringBean();
    }

}
