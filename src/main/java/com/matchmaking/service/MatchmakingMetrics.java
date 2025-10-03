package com.matchmaking.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class MatchmakingMetrics {
    
    private final MeterRegistry meterRegistry;
    
    public void incrementPlayersQueued() {
        Counter.builder("matchmaking.players.queued")
            .description("Total players added to queue")
            .register(meterRegistry)
            .increment();
    }
    
    public void incrementPlayersDequeued() {
        Counter.builder("matchmaking.players.dequeued")
            .description("Total players removed from queue")
            .register(meterRegistry)
            .increment();
    }
    
    public void incrementMatchesCreated() {
        Counter.builder("matchmaking.matches.created")
            .description("Total matches created")
            .register(meterRegistry)
            .increment();
    }
    
    public void recordMatchQuality(double quality) {
        meterRegistry.gauge("matchmaking.match.quality", quality);
    }
    
    public void recordQueueWaitTime(long waitTimeMs) {
        Timer.builder("matchmaking.queue.wait.time")
            .description("Time players spend in queue")
            .register(meterRegistry)
            .record(waitTimeMs, TimeUnit.MILLISECONDS);
    }
}
