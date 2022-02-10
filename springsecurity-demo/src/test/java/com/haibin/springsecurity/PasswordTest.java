package com.haibin.springsecurity;

import org.junit.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordTest {

    @Test
    public void test01(){
        //创建密码解析器
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        //对密码进行加密
        String haibin = bCryptPasswordEncoder.encode("haibin");
        //打印加密之后的数据
        System.out.println("加密之后数据：\t" + haibin);
        //判断原字符串加密后和加密之前是否匹配
        boolean result = bCryptPasswordEncoder.matches("haibin",haibin);
        //打印比较结果
        System.out.println("比较结果：\t" + result);
    }

}
