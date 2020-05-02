package com.nikolay.bot.ballgoal.transformer.timestamp.impl;

import com.nikolay.bot.ballgoal.constants.Status;
import com.nikolay.bot.ballgoal.json.fixture.Fixture;
import com.nikolay.bot.ballgoal.transformer.timestamp.TimestampTransformer;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;

public class ZenitTimestampTransformer implements TimestampTransformer {

    @Override
    public LocalDateTime transform(Fixture fixture) {
        LocalDateTime eventDate = fixture.getEvent_date();
        LocalDateTime now = LocalDateTime.now(Clock.systemUTC());
        String status = fixture.getStatus();
        switch (status) {
            case Status.NOT_STARTED:
                return calculateTimestamp(eventDate, now);
            case Status.FIRST_HALF:
            case Status.SECOND_HALF:
            case Status.EXTRA_TIME:
            case Status.PENALTIES:
            case Status.BREAK_TIME:
            case Status.HALFTIME:
            case Status.SUSPENDED:
            case Status.INTERRUPTED:
                return now.plusMinutes(2);
            case Status.TO_BE_DEFINED:
            case Status.FINISHED:
            case Status.FINISHED_AFTER_EXTRA_TIME:
            case Status.FINISHED_AFTER_PENALTY:
            case Status.POSTPONED:
            case Status.CANCELLED:
            case Status.ABANDONED:
            case Status.TECHNICAL_LOSS:
            case Status.WALK_OVER:
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
            return now.plusHours(3);
        } else {
            Duration duration = Duration.between(now, eventDate);
            if (duration.getSeconds() > 7200) {
                return now.plusMinutes(30);
            } else {
                return eventDate.plusMinutes(2);
            }
        }
    }
}
