package com.haibin.activiti.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 出差申请的POJO对象
 */
@Data
public class Evection implements Serializable {

    private long id;

    private String evectionName;


    /**
     * 出差的天数
     */
    private double num;

    private Date beginDate;

    private Date endDate;

    private String destination;

    private String reson;
}
