package com.nikolay.bot.ballgoal.transformer.api.impl;

import com.nikolay.bot.ballgoal.api.ApiResolver;
import com.nikolay.bot.ballgoal.cache.utils.ZenitPairCache;
import com.nikolay.bot.ballgoal.json.fixture.Fixture;
import com.nikolay.bot.ballgoal.json.fixture.ResultFixture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.transformer.GenericTransformer;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class ZenitFixtureTransformer implements GenericTransformer<ZenitPairCache, Fixture> {

    private static final int TWO_MINUTES = 120;

    private final ApiResolver<ResultFixture> apiResolver;

    private final String apiResourceNextFixture;

    private final String apiResourceFixturesInPlay;

    private final int teamId;

    @Override
    public Fixture transform(ZenitPairCache source) {
        try {
            Fixture zenitFixture = fetchZenitFixtureInPlay(source).orElse(fetchNextZenitFixture());
            log.debug("Fetched next Zenit fixture: {}", zenitFixture);
            return zenitFixture;
        } catch (IOException e) {
            throw new RuntimeException("error fetching result from api", e);
        }
    }

    private Optional<Fixture> fetchZenitFixtureInPlay(ZenitPairCache source) throws IOException {
        if (isNeedFixtureInPlay(source)) {
            return apiResolver
                    .resolve(apiResourceFixturesInPlay)
                    .getApi()
                    .getFixtures()
                    .stream()
                    .filter(this::isTeamPlayingNow)
                    .findAny();
        } else {
            return Optional.empty();
        }
    }

    private Fixture fetchNextZenitFixture() throws IOException {
        return apiResolver.resolve(apiResourceNextFixture).getApi().getFixtures().get(0);
    }

    private boolean isNeedFixtureInPlay(ZenitPairCache source) {
        return source.isInPlay() || source.getNextUpdate().getSeconds() <= TWO_MINUTES;
    }

    private boolean isTeamPlayingNow(Fixture fixture) {
        int homeTeamId = fixture.getHomeTeam().getTeam_id();
        int awayTeamId = fixture.getAwayTeam().getTeam_id();
        return homeTeamId == teamId || awayTeamId == teamId;
    }
}
