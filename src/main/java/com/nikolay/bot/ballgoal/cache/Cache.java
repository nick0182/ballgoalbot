package com.nikolay.bot.ballgoal.cache;

import java.util.concurrent.atomic.AtomicReference;

public class Cache<T> {

    private final AtomicReference<T> atomicCache = new AtomicReference<>();

    public T getCache() {
        return atomicCache.get();
    }

    public void setCache(T cache) {
        atomicCache.set(cache);
    }
}
