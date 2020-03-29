package com.nikolay.bot.ballgoal.transformer.timestamp.impl;

import com.nikolay.bot.ballgoal.json.fixture.Fixture;
import com.nikolay.bot.ballgoal.transformer.timestamp.TimestampTransformer;

import java.time.LocalDateTime;

public class LeagueTimestampTransformer implements TimestampTransformer {

    @Override
    public LocalDateTime transform(Fixture fixture) {
        return fixture.getEventDate().plusMinutes(130);
    }
}
