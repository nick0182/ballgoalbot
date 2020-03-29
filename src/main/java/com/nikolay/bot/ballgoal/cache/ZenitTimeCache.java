package com.nikolay.bot.ballgoal.cache;

import java.time.LocalDateTime;

public final class ZenitTimeCache {

    private final LocalDateTime dateTime;

    private final String cache;

    public ZenitTimeCache(LocalDateTime dateTime, String cache) {
        this.dateTime = dateTime;
        this.cache = cache;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public String getCache() {
        return cache;
    }
}
