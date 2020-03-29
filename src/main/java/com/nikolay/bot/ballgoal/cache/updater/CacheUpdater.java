package com.nikolay.bot.ballgoal.cache.updater;

import com.nikolay.bot.ballgoal.cache.timestamp.Timestamp;
import com.nikolay.bot.ballgoal.gateway.CacheGateway;

public class CacheUpdater {

    private final CacheGateway cacheGateway;

    private final Timestamp timestamp;

    public CacheUpdater(CacheGateway cacheGateway, Timestamp timestamp) {
        this.cacheGateway = cacheGateway;
        this.timestamp = timestamp;
    }

    public void updateCache() {
        if (timestamp.isTimestampPassed()) {
            cacheGateway.refreshCache();
        }
    }
}
