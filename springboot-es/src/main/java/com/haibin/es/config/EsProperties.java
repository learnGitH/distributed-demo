package com.haibin.es.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@Data
@ConfigurationProperties(prefix = "elasticsearch")
public class EsProperties {

    private Set<EsNode> urls;
    private String userName;
    private String passWord;

    @Data
    static class EsNode{
        private String host;
        private Integer port;
    }

}
