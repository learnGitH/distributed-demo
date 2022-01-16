package com.haibin.boot;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class LoggingTest {

    //记录器
    //Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void contextLoads(){
        //日志级别：
        //由低到高 trace<debug<info<warn<error
        log.trace("这是trace日志.....");
        log.debug("这是debug日志.....");
        //springboot默认给我们使用的是info级别的，没有指定级别的就用springboot默认规定的级别：root级别
        log.info("info日志.....");
        log.warn("这是warn日志.....");
        log.error("这是error日志.....");
    }

}
