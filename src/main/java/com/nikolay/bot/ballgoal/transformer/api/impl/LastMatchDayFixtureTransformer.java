package com.nikolay.bot.ballgoal.transformer.api.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikolay.bot.ballgoal.api.ApiRequest;
import com.nikolay.bot.ballgoal.json.fixture.Fixture;
import com.nikolay.bot.ballgoal.json.fixture.ResultFixture;
import com.nikolay.bot.ballgoal.transformer.api.ApiTransformer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Slf4j
public class LastMatchDayFixtureTransformer extends ApiTransformer {

    private final String apiResourceNextLeagueFixture;

    private final String apiResourceLeagueRoundDates;

    private final String apiResourceLeagueFixturesInPlay;

    public LastMatchDayFixtureTransformer(ApiRequest apiRequest, ObjectMapper objectMapper,
                                          String apiResourceNextLeagueFixture,
                                          String apiResourceLeagueRoundDates,
                                          String apiResourceLeagueFixturesInPlay) {
        super(apiRequest, objectMapper);
        this.apiResourceNextLeagueFixture = apiResourceNextLeagueFixture;
        this.apiResourceLeagueRoundDates = apiResourceLeagueRoundDates;
        this.apiResourceLeagueFixturesInPlay = apiResourceLeagueFixturesInPlay;
    }

    @Override
    protected Fixture transform() throws IOException {
        Fixture matchDayFixture = fetchLeagueFixtureInPlay().orElse(fetchNextLeagueFixture());
        log.debug("Fetched league match day fixture: {}", matchDayFixture);
        Fixture lastMatchDayFixture = fetchLastMatchDayFixture(matchDayFixture.getEvent_date());
        log.debug("Fetched league last match day fixture: {}", lastMatchDayFixture);
        return lastMatchDayFixture;
    }

    private Optional<Fixture> fetchLeagueFixtureInPlay() throws IOException {
        ResultFixture result = callApi(apiResourceLeagueFixturesInPlay);
        List<Fixture> allLeagueFixturesInPlay = result.getApi().getFixtures();
        return allLeagueFixturesInPlay.isEmpty()
                ? Optional.empty()
                : Optional.of(allLeagueFixturesInPlay.get(0));
    }

    private Fixture fetchNextLeagueFixture() throws IOException {
        ResultFixture result = callApi(apiResourceNextLeagueFixture);
        return result.getApi().getFixtures().get(0);
    }

    private Fixture fetchLastMatchDayFixture(LocalDateTime eventDate) throws IOException {
        String resource = appendDate(apiResourceLeagueRoundDates, eventDate);
        ResultFixture resultFixture = callApi(resource);
        List<Fixture> matchDayFixtures = resultFixture.getApi().getFixtures();
        // get last fixture from the list as it's already sorted by date
        return matchDayFixtures.get(matchDayFixtures.size() - 1);
    }

    private String appendDate(String resource, LocalDateTime dateTime) {
        return resource + dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
