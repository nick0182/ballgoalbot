package com.nikolay.bot.ballgoal.helper;

import java.time.ZoneId;

public class ZoneGetter {

    // prevent instantiation
    private ZoneGetter() {}

    public static ZoneId getFromResource(String resource) {
        String zone = resource.split("timezone=")[1];
        return ZoneId.of(zone);
    }

}
