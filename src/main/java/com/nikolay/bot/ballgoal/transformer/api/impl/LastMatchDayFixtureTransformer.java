package com.nikolay.bot.ballgoal.transformer.api.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikolay.bot.ballgoal.api.ApiRequest;
import com.nikolay.bot.ballgoal.json.fixture.Fixture;
import com.nikolay.bot.ballgoal.json.fixture.ResultFixture;
import com.nikolay.bot.ballgoal.properties.ResourceProperties;
import com.nikolay.bot.ballgoal.transformer.api.ApiTransformer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class LastMatchDayFixtureTransformer extends ApiTransformer<Fixture> {

    public LastMatchDayFixtureTransformer(ResourceProperties resourceProperties,
                                          ApiRequest apiRequest, ObjectMapper objectMapper) {
        super(resourceProperties, apiRequest, objectMapper);
    }

    @Override
    protected Fixture transform() throws IOException {
        Fixture matchDayFixture = fetchLeagueFixtureInPlay().orElse(fetchNextLeagueFixture());
        return fetchLastMatchDayFixture(matchDayFixture.getEventDate());
    }

    private Optional<Fixture> fetchLeagueFixtureInPlay() throws IOException {
        ResultFixture result = callApi(resourceProperties.getApiResourceLeagueFixturesInPlay(), ResultFixture.class);
        List<Fixture> allLeagueFixturesInPlay = result.getApi().getFixtures();
        return allLeagueFixturesInPlay.isEmpty()
                ? Optional.empty()
                : Optional.of(allLeagueFixturesInPlay.get(0));
    }

    private Fixture fetchNextLeagueFixture() throws IOException {
        ResultFixture result = callApi(resourceProperties.getApiResourceNextLeagueFixture(), ResultFixture.class);
        return result.getApi().getFixtures().get(0);
    }

    private Fixture fetchLastMatchDayFixture(LocalDateTime eventDate) throws IOException {
        String resource = appendDate(resourceProperties.getApiResourceLeagueRoundDates(), eventDate);
        ResultFixture resultFixture = callApi(resource, ResultFixture.class);
        List<Fixture> matchDayFixtures = resultFixture.getApi().getFixtures();
        // get last fixture from the list as it's already sorted by date
        return matchDayFixtures.get(matchDayFixtures.size() - 1);
    }

    private String appendDate(String resource, LocalDateTime dateTime) {
        return resource + dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
