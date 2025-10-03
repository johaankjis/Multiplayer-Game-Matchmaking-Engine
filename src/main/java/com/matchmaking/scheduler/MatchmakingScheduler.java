package com.matchmaking.scheduler;

import com.matchmaking.service.MatchmakingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled task to process matchmaking at regular intervals
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MatchmakingScheduler {

    private final MatchmakingService matchmakingService;

    /**
     * Process matchmaking every 2 seconds
     */
    @Scheduled(fixedDelay = 2000)
    public void processMatchmaking() {
        try {
            matchmakingService.processMatchmaking();
        } catch (Exception e) {
            log.error("Error processing matchmaking", e);
        }
    }
}
