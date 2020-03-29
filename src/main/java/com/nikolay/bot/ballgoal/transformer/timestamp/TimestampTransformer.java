package com.nikolay.bot.ballgoal.transformer.timestamp;

import com.nikolay.bot.ballgoal.json.fixture.Fixture;
import org.springframework.integration.transformer.GenericTransformer;

import java.time.LocalDateTime;

public interface TimestampTransformer extends GenericTransformer<Fixture, LocalDateTime> {
}
