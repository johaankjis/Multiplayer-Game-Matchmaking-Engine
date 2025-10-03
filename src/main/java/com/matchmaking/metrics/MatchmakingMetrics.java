package com.matchmaking.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Metrics tracking for matchmaking performance
 */
@Component
@RequiredArgsConstructor
public class MatchmakingMetrics {

    private final MeterRegistry meterRegistry;

    public void recordMatchCreated() {
        Counter.builder("matchmaking.matches.created")
            .description("Total number of matches created")
            .register(meterRegistry)
            .increment();
    }

    public void recordPlayerJoined() {
        Counter.builder("matchmaking.players.joined")
            .description("Total number of players joined queue")
            .register(meterRegistry)
            .increment();
    }

    public void recordPlayerLeft() {
        Counter.builder("matchmaking.players.left")
            .description("Total number of players left queue")
            .register(meterRegistry)
            .increment();
    }

    public void recordMatchmakingTime(Duration duration) {
        Timer.builder("matchmaking.time")
            .description("Time taken to find a match")
            .register(meterRegistry)
            .record(duration);
    }

    public void recordQueueSize(long size) {
        meterRegistry.gauge("matchmaking.queue.size", size);
    }
}
