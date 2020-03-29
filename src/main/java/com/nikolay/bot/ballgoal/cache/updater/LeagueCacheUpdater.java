package com.nikolay.bot.ballgoal.cache.updater;

import com.nikolay.bot.ballgoal.cache.Cache;
import com.nikolay.bot.ballgoal.cache.timestamp.Timestamp;
import com.nikolay.bot.ballgoal.gateway.CacheGateway;
import org.telegram.telegrambots.meta.api.objects.Message;

public class LeagueCacheUpdater extends CacheUpdater {

    private final Cache<String> leagueCache;

    public LeagueCacheUpdater(CacheGateway cacheGateway, Timestamp timestamp, Cache<String> leagueCache) {
        super(cacheGateway, timestamp);
        this.leagueCache = leagueCache;
    }

    public void setTelegramFileCache(Message file) {
        leagueCache.setCache(file.getPhoto().get(0).getFileId());
    }
}
