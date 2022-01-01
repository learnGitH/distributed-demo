package com.haibin.productcenter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.discovery.event.HeartbeatEvent;
import org.springframework.cloud.zookeeper.discovery.ZookeeperServiceWatch;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class HeartbeatEventListener implements ApplicationListener<HeartbeatEvent> {

    @Override
    public void onApplicationEvent(HeartbeatEvent event) {

        Object value = event.getValue();
        ZookeeperServiceWatch source = (ZookeeperServiceWatch)event.getSource();

        log.info(" event:source: {} ,event:value{}",source.getCache().getCurrentChildren("/services"),value.toString());
    }

}
