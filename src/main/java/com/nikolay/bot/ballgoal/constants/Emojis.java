package com.nikolay.bot.ballgoal.constants;

import java.nio.charset.StandardCharsets;

public class Emojis {

    public static final String EMOJI_HOME_TEAM = new String
            (new byte[]{(byte) 0xF0, (byte) 0x9F, (byte) 0x8F, (byte) 0xA0}, StandardCharsets.UTF_8);

    public static final String EMOJI_AWAY_TEAM = new String
            (new byte[]{(byte) 0xE2, (byte) 0x9C, (byte) 0x88}, StandardCharsets.UTF_8);

    public static final String EMOJI_DATE = new String
            (new byte[]{(byte) 0xF0, (byte) 0x9F, (byte) 0x93, (byte) 0x85}, StandardCharsets.UTF_8);

    public static final String EMOJI_TIME = new String
            (new byte[]{(byte) 0xF0, (byte) 0x9F, (byte) 0x95, (byte) 0xA3}, StandardCharsets.UTF_8);

    public static final String EMOJI_STATUS = new String
            (new byte[]{(byte) 0xE2, (byte) 0x8F, (byte) 0xB3}, StandardCharsets.UTF_8);

    public static final String EMOJI_MINUTE = new String
            (new byte[]{(byte) 0xE2, (byte) 0x93, (byte) 0x82}, StandardCharsets.UTF_8);

    public static final String EMOJI_BALL = new String
            (new byte[]{(byte) 0xE2, (byte) 0x9A, (byte) 0xBD}, StandardCharsets.UTF_8);
}
