package com.haibin.boot;

import com.haibin.boot.entity.Person;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BootTest {

    @Autowired
    Person person;

    @Test
    public void test(){
        System.out.println(person);
    }

}
