package com.nikolay.bot.ballgoal.cache;

import com.nikolay.bot.ballgoal.cache.trigger.barrier.Barrier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;

import java.time.Duration;

@Slf4j
public class TriggerCacheHandler extends CacheHandler<Duration> {

    private final Barrier zenitBarrier;

    public TriggerCacheHandler(Cache<Duration> cache, Barrier zenitBarrier) {
        super(cache);
        this.zenitBarrier = zenitBarrier;
    }

    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        super.handleMessage(message);
        log.debug("duration set barrier awaited. Duration is: {}", message.getPayload());
        zenitBarrier.awaitBarrier();
        zenitBarrier.resetBarrier();
    }
}
