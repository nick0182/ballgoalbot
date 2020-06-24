package com.nikolay.bot.ballgoal.transformer.cache;

import com.nikolay.bot.ballgoal.json.fixture.Fixture;
import org.springframework.integration.transformer.GenericTransformer;

import static com.nikolay.bot.ballgoal.constants.Status.*;

public class ZenitInPlayTransformer implements GenericTransformer<Fixture, Boolean> {

    @Override
    public Boolean transform(Fixture fixture) {
        String status = fixture.getStatus();

        switch (status) {
            case TO_BE_DEFINED:
            case POSTPONED:
            case CANCELLED:
            case ABANDONED:
            case TECHNICAL_LOSS:
            case WALK_OVER:
            case NOT_STARTED:
                return false;
            case FIRST_HALF:
            case SECOND_HALF:
            case EXTRA_TIME:
            case PENALTIES:
            case BREAK_TIME:
            case HALFTIME:
            case SUSPENDED:
            case INTERRUPTED:
            case FINISHED:
            case FINISHED_AFTER_EXTRA_TIME:
            case FINISHED_AFTER_PENALTY:
                return true;
            default:
                throw new IllegalArgumentException("Not a valid status");
        }
    }
}
