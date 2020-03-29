package com.nikolay.bot.ballgoal.gateway;

import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.scheduling.annotation.Async;

@Async
public interface CacheGateway {

    @Payload("new java.lang.Object()")
    void refreshCache();

}
