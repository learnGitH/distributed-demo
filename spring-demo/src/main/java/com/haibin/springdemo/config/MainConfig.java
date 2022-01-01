package com.haibin.springdemo.config;

import com.haibin.springdemo.condition.HaibinCondition;
import com.haibin.springdemo.entity.Car;
import com.haibin.springdemo.entity.HaibinLog;
import com.haibin.springdemo.entity.Person;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@ComponentScan(basePackages = "com.haibin.springdemo.entity")
@Configuration
public class MainConfig {

    @Bean
    public Person person(){
        return new Person();
    }

    @Bean
    @Conditional(value= HaibinCondition.class)
    public HaibinLog haibinLog(){
        return new HaibinLog();
    }

    @Bean(initMethod = "init",destroyMethod = "destroy")
    public Car car(){
        return new Car();
    }

}
