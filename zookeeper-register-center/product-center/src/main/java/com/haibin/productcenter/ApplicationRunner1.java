package com.haibin.productcenter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.cloud.zookeeper.serviceregistry.ServiceInstanceRegistration;
import org.springframework.cloud.zookeeper.serviceregistry.ZookeeperRegistration;
import org.springframework.cloud.zookeeper.serviceregistry.ZookeeperServiceRegistry;
import org.springframework.stereotype.Component;

@Component
public class ApplicationRunner1 implements ApplicationRunner {

    @Autowired
    private ZookeeperServiceRegistry serviceRegistry;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        ZookeeperRegistration registration = ServiceInstanceRegistration.builder()
                .defaultUriSpec()
                .address("anyUrl")
                .port(10)
                .name("/a/b/c/d/anotherservice")
                .build();
        this.serviceRegistry.register(registration);
    }

}
