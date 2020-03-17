package com.nikolay.bot.ballgoal.constants;

public class Commands {

    // prevent initialization
    private Commands() {}

    // info about available commands
    public static final String INFO = "/info";

    // info about FC Zenit next match
    public static final String ZENIT = "/zenit";

    // match time in Jerusalem timezone
    public static final String ZENIT_JERUSALEM = "GMT +2.00 (Jerusalem)";

    // match time in Saint-Petersburg timezone
    public static final String ZENIT_SAINT_PETERSBURG = "GMT +3.00 (Saint-Petersburg)";

    // get russian premier league table
    public static final String LEAGUE_STANDING = "/standing";
}
