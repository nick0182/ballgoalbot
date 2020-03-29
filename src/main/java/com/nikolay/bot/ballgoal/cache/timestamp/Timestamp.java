package com.nikolay.bot.ballgoal.cache.timestamp;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

public class Timestamp {

    private final AtomicReference<LocalDateTime> timestamp = new AtomicReference<>(LocalDateTime.now());

    public boolean isTimestampPassed() {
        return LocalDateTime.now().compareTo(timestamp.get()) > 0;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp.set(timestamp);
    }
}
