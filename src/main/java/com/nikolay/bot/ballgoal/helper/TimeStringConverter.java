package com.nikolay.bot.ballgoal.helper;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TimeStringConverter {

    // prevent instantiation
    private TimeStringConverter() {}

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EEEE d MMMM u");

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public static String getDateString(ZonedDateTime date) {
        return date.format(DATE_FORMATTER);
    }

    public static String getTimeString(ZonedDateTime date) {
        return date.toLocalTime().format(TIME_FORMATTER);
    }

    public static String getZonedTimeString(ZonedDateTime dateTime, ZoneId zone) {
        return dateTime.withZoneSameInstant(zone).toLocalTime().format(TIME_FORMATTER);
    }

}
