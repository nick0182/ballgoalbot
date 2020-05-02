package com.nikolay.bot.ballgoal.transformer.api.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikolay.bot.ballgoal.api.ApiRequest;
import com.nikolay.bot.ballgoal.json.fixture.Fixture;
import com.nikolay.bot.ballgoal.json.fixture.ResultFixture;
import com.nikolay.bot.ballgoal.transformer.api.ApiTransformer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Optional;

@Slf4j
public class ZenitFixtureTransformer extends ApiTransformer {

    private final String apiResourceNextFixture;

    private final String apiResourceFixturesInPlay;

    private final int teamId;

    public ZenitFixtureTransformer(ApiRequest apiRequest, ObjectMapper objectMapper,
                                   String apiResourceNextFixture, String apiResourceFixturesInPlay, int teamId) {
        super(apiRequest, objectMapper);
        this.apiResourceNextFixture = apiResourceNextFixture;
        this.apiResourceFixturesInPlay = apiResourceFixturesInPlay;
        this.teamId = teamId;
    }

    @Override
    public Fixture transform() throws IOException {
        Fixture zenitFixture = fetchZenitFixtureInPlay().orElse(fetchNextZenitFixture());
        log.debug("Fetched next Zenit fixture: {}", zenitFixture);
        return zenitFixture;
    }

    private Optional<Fixture> fetchZenitFixtureInPlay() throws IOException {
        ResultFixture result = callApi(apiResourceFixturesInPlay);
        return result.getApi().getFixtures().stream().filter(this::isTeamPlayingNow).findAny();
    }

    private Fixture fetchNextZenitFixture() throws IOException {
        ResultFixture result = callApi(apiResourceNextFixture);
        return result.getApi().getFixtures().get(0);
    }

    private boolean isTeamPlayingNow(Fixture fixture) {
        int homeTeamId = fixture.getHomeTeam().getTeam_id();
        int awayTeamId = fixture.getAwayTeam().getTeam_id();
        return homeTeamId == teamId || awayTeamId == teamId;
    }
}
