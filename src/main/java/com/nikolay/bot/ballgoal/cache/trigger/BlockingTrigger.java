package com.nikolay.bot.ballgoal.cache.trigger;

import com.nikolay.bot.ballgoal.cache.Cache;
import com.nikolay.bot.ballgoal.cache.trigger.barrier.Barrier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

@Slf4j
@RequiredArgsConstructor
public class BlockingTrigger implements Trigger {

    private final Barrier barrier;

    private final Cache<Duration> cache;

    @Override
    public Date nextExecutionTime(TriggerContext triggerContext) {
        Date lastScheduled = triggerContext.lastScheduledExecutionTime();
        if (lastScheduled == null) {
            return new Date();
        } else {
            barrier.awaitBarrier();
            Date nextExecutionTime = new Date(lastScheduled.getTime() + cache.getCache().toMillis());
            log.debug("trigger next execution time barrier awaited. Time is: {}",
                    LocalDateTime.ofInstant(nextExecutionTime.toInstant(), ZoneOffset.UTC));
            return nextExecutionTime;
        }
    }
}
