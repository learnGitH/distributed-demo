package com.haibin.boot;

import com.haibin.boot.entity.Person;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StopWatch;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BootTest {

    @Autowired
    Person person;

    @Test
    public void test(){
        System.out.println(person);
    }

    @Test
    public void stopWatchTest(){
        System.out.println("==========start=========");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        for (int i = 0; i < 100000;i++){}
        stopWatch.stop();
        System.out.println("==========end=========");
    }

}
