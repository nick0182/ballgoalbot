package com.nikolay.bot.ballgoal.constants;

import java.time.format.DateTimeFormatter;

public class Formatter {

    // prevent initialization
    private Formatter() {}

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EEEE d MMMM u");

    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
}
