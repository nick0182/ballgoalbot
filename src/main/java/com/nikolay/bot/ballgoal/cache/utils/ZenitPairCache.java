package com.nikolay.bot.ballgoal.cache.utils;

import lombok.Data;

import java.time.Duration;

@Data
public final class ZenitPairCache {

    private final boolean isInPlay;

    private final Duration nextUpdate;
}
