package com.nikolay.bot.ballgoal.cache;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public final class ZenitTimeCache {

    private final LocalDateTime dateTime;

    private final String text;
}
