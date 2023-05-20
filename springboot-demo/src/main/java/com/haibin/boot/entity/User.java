package com.haibin.boot.entity;

import com.haibin.boot.datamask.DataMasking;
import com.haibin.boot.datamask.DataMaskingFunc;
import lombok.Data;

import java.io.Serializable;

@Data
public class User implements Serializable {

    private Long id;

    @DataMasking(maskFunc = DataMaskingFunc.ALL_MASK)
    private String name;

    private Integer age;

    private String email;

}
