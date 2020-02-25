package com.nikolay.bot.ballgoal.cache;

import java.time.LocalTime;
import java.time.ZoneId;

public class ZenitCache {

    private CachedMessage cachedMessage;

    private int thresholdMinutes;

    private LocalTime lastApiTriggerTime;

    public ZenitCache(int thresholdMinutes) {
        this.thresholdMinutes = thresholdMinutes;
        lastApiTriggerTime = LocalTime.now(ZoneId.systemDefault()).minusMinutes(thresholdMinutes);
    }

    public CachedMessage getCachedMessage() {
        return cachedMessage;
    }

    public int getThresholdMinutes() {
        return thresholdMinutes;
    }

    public LocalTime getLastApiTriggerTime() {
        return lastApiTriggerTime;
    }

    public void setCachedMessage(CachedMessage cachedMessage) {
        this.cachedMessage = cachedMessage;
    }

    public void setLastApiTriggerTime(LocalTime lastApiTriggerTime) {
        this.lastApiTriggerTime = lastApiTriggerTime;
    }
}
