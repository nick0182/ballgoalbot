package com.nikolay.bot.ballgoal.cache.utils;

import com.nikolay.bot.ballgoal.constants.Emojis;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class CacheOffsetAppender {

    public static String appendOffset(LocalDateTime dateTime, ZoneOffset offset) {
        if (dateTime == null) {
            return "";
        } else {
            OffsetDateTime eventDateTime = dateTime.atOffset(ZoneOffset.UTC).withOffsetSameInstant(offset);
            String formattedDate = eventDateTime.format(Formatters.DATE_FORMATTER);
            String formattedTime = eventDateTime.format(Formatters.TIME_FORMATTER);
            return Emojis.EMOJI_DATE +
                    " " +
                    formattedDate +
                    "\n" +
                    Emojis.EMOJI_TIME +
                    " " +
                    formattedTime;
        }
    }

    private static class Formatters {

        private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EEEE d MMMM u");

        private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    }
}
