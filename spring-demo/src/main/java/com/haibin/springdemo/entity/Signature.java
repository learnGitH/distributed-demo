package com.haibin.springdemo.entity;

import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Signature {

    private static final String apiKey = "bc0000000000f71d";

    public static String getSignature(Map<String, String> data) {
        TreeMap<String, String> sorted = new TreeMap<>();
        sorted.putAll(data);
        String dataBeforeMD5 = "";
        for(Map.Entry<String, String> entry : sorted.entrySet()) {
            if(!"".equals(entry.getValue())) {
                dataBeforeMD5 += entry.getKey() + "=" + entry.getValue() + "&";
            }
        }
        dataBeforeMD5 += "secretKey=" + apiKey;
        String signature = DigestUtils.md5DigestAsHex(dataBeforeMD5.getBytes());
        System.out.println(dataBeforeMD5);
        return signature;
    }

    public static void main(String[] args) {
        Map<String, String> data = new HashMap<>();
        data.put("paramC", "c");
        data.put("paramD", "");
        data.put("paramE", "e");
        data.put("paramA", "a");
        data.put("paramB", "b");
        System.out.println(data);
        System.out.println(getSignature(data));

    }

}
