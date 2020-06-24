package com.nikolay.bot.ballgoal.api.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikolay.bot.ballgoal.api.ApiRequest;
import com.nikolay.bot.ballgoal.api.ApiResolver;
import com.nikolay.bot.ballgoal.json.fixture.ResultFixture;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

@RequiredArgsConstructor
public class FixtureApiResolver implements ApiResolver<ResultFixture> {

    private final ApiRequest apiRequest;

    private final ObjectMapper objectMapper;

    @Override
    public ResultFixture resolve(String resource) throws IOException {
        String json = apiRequest.call(resource);
        return objectMapper.readValue(json, ResultFixture.class);
    }
}
