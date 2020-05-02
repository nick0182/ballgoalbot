package com.nikolay.bot.ballgoal.cache.trigger;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.springframework.integration.transformer.GenericTransformer;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
public class TriggerDurationTransformer implements GenericTransformer<LocalDateTime, Duration> {

    @Override
    public Duration transform(LocalDateTime source) {
        log.debug("New timestamp obtained: {}", source);
        Duration newTriggerDelay = Duration.between(
                        LocalDateTime.now(Clock.systemUTC()).withSecond(0),
                        source.withSecond(0));
        String formattedDelay = DurationFormatUtils.formatDurationWords(newTriggerDelay.toMillis(), true, true);
        log.debug("New delay for trigger: {}", formattedDelay);
        return newTriggerDelay;
    }
}
