package com.nikolay.bot.ballgoal.transformer.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikolay.bot.ballgoal.api.ApiRequest;
import com.nikolay.bot.ballgoal.properties.ResourceProperties;
import org.springframework.integration.transformer.GenericTransformer;

import java.io.IOException;

public abstract class ApiTransformer<T> implements GenericTransformer<Object, T> {

    protected final ResourceProperties resourceProperties;

    protected final ApiRequest apiRequest;

    protected final ObjectMapper objectMapper;

    public ApiTransformer(ResourceProperties resourceProperties, ApiRequest apiRequest, ObjectMapper objectMapper) {
        this.resourceProperties = resourceProperties;
        this.apiRequest = apiRequest;
        this.objectMapper = objectMapper;
    }

    protected RuntimeException wrapToRuntime(Exception exception) {
        return new RuntimeException("Cannot obtain info from api", exception.getCause());
    }

    protected <S> S callApi(String resource, Class<S> classToMap) throws IOException {
        String json = apiRequest.call(resource);
        return objectMapper.readValue(json, classToMap);
    }
}
