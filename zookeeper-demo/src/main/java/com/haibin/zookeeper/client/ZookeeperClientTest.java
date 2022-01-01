package com.haibin.zookeeper.client;

import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class ZookeeperClientTest {

    private static final String ZK_ADDRESS="192.168.159.173:2181";

    private static final int SESSION_TIMEOUT = 5000;

    private static ZooKeeper zooKeeper;

    private static final String ZK_NODE="/zk-node";

    public static void main(String[] args) throws IOException, InterruptedException{
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        zooKeeper = new ZooKeeper(ZK_ADDRESS, SESSION_TIMEOUT,event -> {
            if (event.getState() == Watcher.Event.KeeperState.SyncConnected &&
                    event.getType() == Watcher.Event.EventType.None){
                countDownLatch.countDown();
                System.out.println("连接成功！");
            }
        });
        System.out.println("连接中.....");
        countDownLatch.await();
    }

}
