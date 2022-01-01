package com.haibin.productcenter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class TestController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private LoadBalancerClient loadBalancerClient;

    @GetMapping("/test")
    public String test() {
        return this.restTemplate.getForObject("http://product-center/getInfo", String.class);
    }

    @GetMapping("/lb")
    public String getLb(){
        ServiceInstance choose = loadBalancerClient.choose("product-center");
        String serviceId = choose.getServiceId();
        int port = choose.getPort();
        return serviceId + " : "+port;
    }

}
