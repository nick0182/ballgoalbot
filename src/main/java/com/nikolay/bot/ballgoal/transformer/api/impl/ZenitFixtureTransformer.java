package com.nikolay.bot.ballgoal.transformer.api.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikolay.bot.ballgoal.api.ApiRequest;
import com.nikolay.bot.ballgoal.json.fixture.Fixture;
import com.nikolay.bot.ballgoal.json.fixture.ResultFixture;
import com.nikolay.bot.ballgoal.properties.ResourceProperties;
import com.nikolay.bot.ballgoal.transformer.api.ApiTransformer;

import java.io.IOException;
import java.util.Optional;

public class ZenitFixtureTransformer extends ApiTransformer<Fixture> {

    public ZenitFixtureTransformer(ResourceProperties resourceProperties,
                                   ApiRequest apiRequest, ObjectMapper objectMapper) {
        super(resourceProperties, apiRequest, objectMapper);
    }

    @Override
    public Fixture transform() throws IOException {
        return fetchZenitFixtureInPlay().orElse(fetchNextZenitFixture());
    }

    private Optional<Fixture> fetchZenitFixtureInPlay() throws IOException {
        ResultFixture result = callApi(resourceProperties.getApiResourceFixturesInPlay(), ResultFixture.class);
        return result.getApi().getFixtures().stream().filter(this::isTeamPlayingNow).findAny();
    }

    private Fixture fetchNextZenitFixture() throws IOException {
        ResultFixture result = callApi(resourceProperties.getApiResourceNextFixture(), ResultFixture.class);
        return result.getApi().getFixtures().get(0);
    }

    private boolean isTeamPlayingNow(Fixture fixture) {
        int teamId = resourceProperties.getTeamId();
        int homeTeamId = fixture.getHomeTeam().getTeam_id();
        int awayTeamId = fixture.getAwayTeam().getTeam_id();
        return homeTeamId == teamId || awayTeamId == teamId;
    }
}
