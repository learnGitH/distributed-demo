package com.haibin.boot.datamask;

public interface DataMaskingOperation {

    String MASK_CHAR = "*";

    String mask(String content, String maskChar);

}
