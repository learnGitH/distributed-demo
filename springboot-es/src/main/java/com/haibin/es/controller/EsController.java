package com.haibin.es.controller;

import com.haibin.es.service.HbMallSearchService;
import com.haibin.es.vo.ESRequestParam;
import com.haibin.es.vo.ESResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/es")
public class EsController {

    @Autowired
    private HbMallSearchService hbMallSearchService;

    @PostMapping("/searchList")
    public ESResponseResult listPage(ESRequestParam param, HttpServletRequest request) {
        //1、根据传递来的页面的查询参数，去es中检索商品
        ESResponseResult searchResult = hbMallSearchService.search(param);
        return searchResult;
    }

}
