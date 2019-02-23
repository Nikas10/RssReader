package com.company.nikas.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class ObjectMapperPreparer {

    private ObjectMapper objectMapper;

    public ObjectMapperPreparer() {};

    public ObjectMapper produceInstance() {
        objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        return objectMapper;
    }
}
