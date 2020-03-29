package com.nikolay.bot.ballgoal.cache.timestamp;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

import java.time.LocalDateTime;

public class TimestampHandler implements MessageHandler {

    private final Timestamp timestamp;

    public TimestampHandler(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        timestamp.setTimestamp((LocalDateTime) message.getPayload());
    }
}
