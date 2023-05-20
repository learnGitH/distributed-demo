package com.haibin.boot.datamask;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration(proxyBeanMethods = false)
public class DataMaskConfiguration {

    static class JacksonObjectMapperConfiguration {
        JacksonObjectMapperConfiguration() {}

        @Bean
        @Primary
        ObjectMapper jacksonObjectMapper(Jackson2ObjectMapperBuilder builder) {
            ObjectMapper objectMapper = builder.createXmlMapper(false).build();
            AnnotationIntrospector ai = objectMapper.getSerializationConfig().getAnnotationIntrospector();
            AnnotationIntrospector newAi = AnnotationIntrospectorPair.pair(ai,new DataMaskingAnnotationIntrospector());
            objectMapper.setAnnotationIntrospector(newAi);
            return objectMapper;
        }
    }

}
