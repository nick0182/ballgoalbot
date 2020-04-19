package com.nikolay.bot.ballgoal.cache.trigger;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.util.DynamicPeriodicTrigger;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;

public class TriggerHandler implements MessageHandler {

    private static final Logger LOG = LoggerFactory.getLogger(TriggerHandler.class);

    private final DynamicPeriodicTrigger trigger;

    private final String cacheName;

    public TriggerHandler(DynamicPeriodicTrigger trigger, String cacheName) {
        this.trigger = trigger;
        this.cacheName = cacheName;
    }

    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        LocalDateTime timestamp = (LocalDateTime) message.getPayload();
        LOG.info("New timestamp for " + cacheName + " cache obtained: {}", timestamp);
        Duration newTriggerDelay = Duration.between(LocalDateTime.now(Clock.systemUTC()), timestamp);
        String formattedDelay = DurationFormatUtils.formatDurationWords(newTriggerDelay.toMillis(), true, true);
        LOG.info("New delay for " + cacheName + " cache trigger set: {}", formattedDelay);
        trigger.setDuration(newTriggerDelay);
    }
}
