package com.nikolay.bot.ballgoal.transformer.api.impl;

import com.nikolay.bot.ballgoal.api.ApiResolver;
import com.nikolay.bot.ballgoal.json.fixture.Fixture;
import com.nikolay.bot.ballgoal.json.fixture.ResultFixture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.transformer.GenericTransformer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class LastMatchDayFixtureTransformer implements GenericTransformer<Object, Fixture> {

    private final ApiResolver<ResultFixture> apiResolver;

    private final String apiResourceNextLeagueFixture;

    private final String apiResourceLeagueRoundDates;

    private final String apiResourceLeagueFixturesInPlay;

    @Override
    public Fixture transform(Object source) {
        try {
            Fixture matchDayFixture = fetchLeagueFixtureInPlay().orElse(fetchNextLeagueFixture());
            log.debug("Fetched league match day fixture: {}", matchDayFixture);
            Fixture lastMatchDayFixture = fetchLastMatchDayFixture(matchDayFixture.getEvent_date());
            log.debug("Fetched league last match day fixture: {}", lastMatchDayFixture);
            return lastMatchDayFixture;
        } catch (IOException e) {
            throw new RuntimeException("error fetching result from api", e);
        }
    }

    private Optional<Fixture> fetchLeagueFixtureInPlay() throws IOException {
        ResultFixture result = apiResolver.resolve(apiResourceLeagueFixturesInPlay);
        List<Fixture> allLeagueFixturesInPlay = result.getApi().getFixtures();
        return allLeagueFixturesInPlay.isEmpty()
                ? Optional.empty()
                : Optional.of(allLeagueFixturesInPlay.get(0));
    }

    private Fixture fetchNextLeagueFixture() throws IOException {
        ResultFixture result = apiResolver.resolve(apiResourceNextLeagueFixture);
        return result.getApi().getFixtures().get(0);
    }

    private Fixture fetchLastMatchDayFixture(LocalDateTime eventDate) throws IOException {
        String resource = appendDate(apiResourceLeagueRoundDates, eventDate);
        ResultFixture resultFixture = apiResolver.resolve(resource);
        List<Fixture> matchDayFixtures = resultFixture.getApi().getFixtures();
        // get last fixture from the list as it's already sorted by date
        return matchDayFixtures.get(matchDayFixtures.size() - 1);
    }

    private String appendDate(String resource, LocalDateTime dateTime) {
        return resource + dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
