package com.nikolay.bot.ballgoal.transformer.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikolay.bot.ballgoal.api.ApiRequest;
import com.nikolay.bot.ballgoal.json.fixture.Fixture;
import com.nikolay.bot.ballgoal.json.fixture.ResultFixture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.transformer.GenericTransformer;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public abstract class ApiTransformer implements GenericTransformer<Object, Fixture> {

    protected final ApiRequest apiRequest;

    protected final ObjectMapper objectMapper;

    @Override
    public Fixture transform(Object source) {
        try {
            return transform();
        } catch (IOException e) {
            log.error("error fetching result from api", e);
            throw new RuntimeException("Cache was not set");
        }
    }

    protected abstract Fixture transform() throws IOException;

    protected ResultFixture callApi(String resource) throws IOException {
        String json = apiRequest.call(resource);
        return objectMapper.readValue(json, ResultFixture.class);
    }
}
