package com.nikolay.bot.ballgoal.cache;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

public class CacheHandler<T> implements MessageHandler {

    private final Cache<T> cache;

    public CacheHandler(Cache<T> cache) {
        this.cache = cache;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handleMessage(Message<?> message) throws MessagingException {
        cache.setCache((T) message.getPayload());
    }
}
