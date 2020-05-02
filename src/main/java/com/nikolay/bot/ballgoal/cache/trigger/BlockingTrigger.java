package com.nikolay.bot.ballgoal.cache.trigger;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
public class BlockingTrigger implements Trigger, MessageHandler {

    private final CyclicBarrier barrier = new CyclicBarrier(2);

    private volatile Duration duration = Duration.ofMillis(0);

    @Override
    public void handleMessage(Message<?> message) {
        this.duration = (Duration) message.getPayload();
        log.debug("duration set barrier awaited. Duration is: {}", duration);
        awaitBarrier();
        resetBarrier();
    }

    @Override
    public Date nextExecutionTime(TriggerContext triggerContext) {
        Date lastScheduled = triggerContext.lastScheduledExecutionTime();
        if (lastScheduled == null) {
            return new Date();
        } else {
            awaitBarrier();
            Date nextExecutionTime = new Date(lastScheduled.getTime() + duration.toMillis());
            log.debug("trigger next execution time barrier awaited. Time is: {}",
                    LocalDateTime.ofInstant(nextExecutionTime.toInstant(), ZoneOffset.UTC));
            return nextExecutionTime;
        }
    }

    private void awaitBarrier() {
        try {
            barrier.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException | BrokenBarrierException | TimeoutException e) {
            log.error("Failed to await cyclic barrier due to exception: {0}", e.getCause());
        }
    }

    private void resetBarrier() {
        barrier.reset();
    }
}
