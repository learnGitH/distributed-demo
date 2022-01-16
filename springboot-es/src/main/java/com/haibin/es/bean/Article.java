package com.haibin.es.bean;

import io.searchbox.annotations.JestId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Article {

    @JestId
    private int id;
    private String title;
    private String content;
    private String url;
    private Date pubdate;
    private String source;
    private String author;
}
