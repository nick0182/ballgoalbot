package com.nikolay.bot.ballgoal.transformer.api.mock;

import com.nikolay.bot.ballgoal.constants.Status;
import com.nikolay.bot.ballgoal.json.fixture.Fixture;
import com.nikolay.bot.ballgoal.json.fixture.Team;
import org.springframework.integration.transformer.GenericTransformer;

import java.time.Clock;
import java.time.LocalDateTime;

public class MockApiTransformer implements GenericTransformer<Object, Fixture> {

    @Override
    public Fixture transform(Object source) {
        Team homeTeam = new Team();
        homeTeam.setTeam_name("Barcelona");
        Team awayTeam = new Team();
        awayTeam.setTeam_name("Real Madrid");
        Fixture fixture = new Fixture();
        fixture.setEvent_date(LocalDateTime.now(Clock.systemUTC()).plusMinutes(1));
        fixture.setStatus(Status.NOT_STARTED);
        fixture.setHomeTeam(homeTeam);
        fixture.setAwayTeam(awayTeam);
        return fixture;
    }
}
