package com.nikolay.bot.ballgoal.transformer.cache;

import com.nikolay.bot.ballgoal.cache.ZenitTimeCache;
import com.nikolay.bot.ballgoal.constants.Emojis;
import com.nikolay.bot.ballgoal.json.fixture.Fixture;
import org.springframework.integration.transformer.GenericTransformer;

import static com.nikolay.bot.ballgoal.constants.Status.*;

public class ZenitCacheTransformer implements GenericTransformer<Fixture, ZenitTimeCache> {

    @Override
    public ZenitTimeCache transform(Fixture fixture) {
        String status = fixture.getStatus();

        switch (status) {
            case TO_BE_DEFINED:
            case POSTPONED:
            case CANCELLED:
            case ABANDONED:
            case TECHNICAL_LOSS:
            case WALK_OVER:
                return new ZenitTimeCache(null, textWithStatus(fixture));
            case NOT_STARTED:
                return new ZenitTimeCache(fixture.getEventDate(), commonText(fixture));
            case FIRST_HALF:
            case SECOND_HALF:
            case EXTRA_TIME:
            case PENALTIES:
            case BREAK_TIME:
            case HALFTIME:
            case SUSPENDED:
            case INTERRUPTED:
                return new ZenitTimeCache(null, textWithMinuteAndScore(fixture));
            case FINISHED:
            case FINISHED_AFTER_EXTRA_TIME:
            case FINISHED_AFTER_PENALTY:
                return new ZenitTimeCache(null, textWithStatusAndScore(fixture));
            default:
                throw new IllegalArgumentException("Not a valid status");
        }
    }

    private String commonText(Fixture fixture) {
        return Emojis.EMOJI_HOME_TEAM +
                " " +
                fixture.getHomeTeam().getTeam_name() +
                "\n" +
                Emojis.EMOJI_AWAY_TEAM +
                " " +
                fixture.getAwayTeam().getTeam_name() +
                "\n";
    }

    private String textWithStatus(Fixture fixture) {
        return commonText(fixture) +
                Emojis.EMOJI_STATUS +
                " " +
                fixture.getStatus();
    }

    private String textWithMinuteAndScore(Fixture fixture) {
        return commonText(fixture) +
                Emojis.EMOJI_MINUTE +
                " " +
                fixture.getElapsed() +
                "\n" +
                Emojis.EMOJI_BALL +
                " " +
                fixture.getGoalsHomeTeam() +
                ":" +
                fixture.getGoalsAwayTeam();
    }

    private String textWithStatusAndScore(Fixture fixture) {
        return commonText(fixture) +
                Emojis.EMOJI_STATUS +
                " " +
                fixture.getStatus() +
                "\n" +
                Emojis.EMOJI_BALL +
                " " +
                fixture.getGoalsHomeTeam() +
                ":" +
                fixture.getGoalsAwayTeam();
    }

//    private String textWithDateTime(Fixture fixture, ZoneOffset zoneOffset) {
//        OffsetDateTime eventDateTime =
//                fixture.getEventDate().atOffset(ZoneOffset.UTC).withOffsetSameInstant(zoneOffset);
//        String formattedDate = eventDateTime.format(Formatters.DATE_FORMATTER);
//        String formattedTime = eventDateTime.format(Formatters.TIME_FORMATTER);
//
//        return commonText(fixture)
//                .append(Emojis.EMOJI_DATE)
//                .append(" ")
//                .append(formattedDate)
//                .append("\n")
//                .append(Emojis.EMOJI_TIME)
//                .append(" ")
//                .append(formattedTime)
//                .toString();
//    }


}
