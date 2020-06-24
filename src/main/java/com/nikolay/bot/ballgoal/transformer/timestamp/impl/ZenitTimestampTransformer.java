package com.nikolay.bot.ballgoal.transformer.timestamp.impl;

import com.nikolay.bot.ballgoal.json.fixture.Fixture;
import com.nikolay.bot.ballgoal.transformer.timestamp.TimestampTransformer;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;

import static com.nikolay.bot.ballgoal.constants.Status.*;

public class ZenitTimestampTransformer implements TimestampTransformer {

    @Override
    public LocalDateTime transform(Fixture fixture) {
        LocalDateTime eventDate = fixture.getEvent_date();
        LocalDateTime now = LocalDateTime.now(Clock.systemUTC());
        String status = fixture.getStatus();
        switch (status) {
            case NOT_STARTED:
                return calculateTimestamp(eventDate, now);
            case FIRST_HALF:
            case SECOND_HALF:
            case EXTRA_TIME:
            case PENALTIES:
            case BREAK_TIME:
            case HALFTIME:
            case SUSPENDED:
            case INTERRUPTED:
                return now.plusMinutes(2);
            case TO_BE_DEFINED:
            case FINISHED:
            case FINISHED_AFTER_EXTRA_TIME:
            case FINISHED_AFTER_PENALTY:
            case POSTPONED:
            case CANCELLED:
            case ABANDONED:
            case TECHNICAL_LOSS:
            case WALK_OVER:
                return now.plusMinutes(30);
            default:
                throw new RuntimeException("Status not supported: " + status);
        }
    }

    private LocalDateTime calculateTimestamp(LocalDateTime eventDate, LocalDateTime now) {
        Period period = Period.between(now.toLocalDate(), eventDate.toLocalDate());
        if (period.getDays() > 7) {
            return now.plusDays(1);
        } else if (period.getDays() > 3) {
            return now.plusHours(12);
        } else if (period.getDays() > 1) {
            return now.plusHours(4);
        } else {
            Duration duration = Duration.between(now, eventDate);
            if (duration.getSeconds() > 7200) {
                return now.plusHours(1);
            } else if (duration.getSeconds() > 3600) {
                return eventDate.plusMinutes(30);
            } else if (duration.getSeconds() > 1800) {
                return eventDate.plusMinutes(15);
            } else if (duration.getSeconds() > 900) {
                return eventDate.plusMinutes(8);
            } else {
                return eventDate.plusMinutes(2);
            }
        }
    }
}
