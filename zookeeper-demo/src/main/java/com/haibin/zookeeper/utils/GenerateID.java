package com.haibin.zookeeper.utils;

import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;

import java.security.NoSuchAlgorithmException;

public class GenerateID {

    public static void generateSuperDigest() throws NoSuchAlgorithmException {
        String sId = DigestAuthenticationProvider.generateDigest("gj:test");
        System.out.println(sId);//  gj:X/NSthOB0fD/OT6iilJ55WJVado=
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {
        generateSuperDigest();
    }

}
