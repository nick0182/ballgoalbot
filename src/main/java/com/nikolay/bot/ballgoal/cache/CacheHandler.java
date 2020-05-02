package com.nikolay.bot.ballgoal.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

@RequiredArgsConstructor
public class CacheHandler<T> implements MessageHandler {

    private final Cache<T> cache;

    @Override
    @SuppressWarnings("unchecked")
    public void handleMessage(Message<?> message) throws MessagingException {
        cache.setCache((T) message.getPayload());
    }
}
