package com.nikolay.bot.ballgoal.cache.trigger.barrier.impl;

import com.nikolay.bot.ballgoal.cache.trigger.barrier.Barrier;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
public class TriggerBarrier implements Barrier {

    private final CyclicBarrier cyclicBarrier = new CyclicBarrier(2);

    @Override
    public void awaitBarrier() {
        try {
            cyclicBarrier.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException | BrokenBarrierException | TimeoutException e) {
            log.error("Failed to await cyclic barrier due to exception: {0}", e.getCause());
        }
    }

    @Override
    public void resetBarrier() {
        cyclicBarrier.reset();
    }
}
