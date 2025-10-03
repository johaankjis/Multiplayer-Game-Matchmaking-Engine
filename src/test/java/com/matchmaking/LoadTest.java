package com.matchmaking;

import com.matchmaking.model.Player;
import com.matchmaking.service.MatchmakingService;
import com.matchmaking.service.QueueService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Load and performance tests
 */
@SpringBootTest
@DisplayName("Load Tests")
class LoadTest {

    @Autowired
    private MatchmakingService matchmakingService;

    @Autowired
    private QueueService queueService;

    @Test
    @DisplayName("Should handle 1000 concurrent join requests")
    void testConcurrentJoinRequests() throws InterruptedException, ExecutionException {
        int numPlayers = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(50);
        List<Future<?>> futures = new ArrayList<>();

        Instant start = Instant.now();

        for (int i = 0; i < numPlayers; i++) {
            final int playerId = i;
            Future<?> future = executor.submit(() -> {
                Player player = Player.builder()
                    .playerId("player" + playerId)
                    .username("User" + playerId)
                    .skillRating(1500 + (playerId % 500))
                    .latency(30 + (playerId % 70))
                    .region("us-east")
                    .build();
                
                matchmakingService.joinQueue(player);
            });
            futures.add(future);
        }

        // Wait for all to complete
        for (Future<?> future : futures) {
            future.get();
        }

        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);

        System.out.println("Processed " + numPlayers + " join requests in " + duration.toMillis() + "ms");
        System.out.println("Throughput: " + (numPlayers * 1000.0 / duration.toMillis()) + " requests/second");

        assertTrue(duration.toSeconds() < 10, "Should process 1000 requests in under 10 seconds");
    }

    @Test
    @DisplayName("Should create matches efficiently")
    void testMatchmakingPerformance() {
        // Add 100 players
        for (int i = 0; i < 100; i++) {
            Player player = Player.builder()
                .playerId("perf-player" + i)
                .username("PerfUser" + i)
                .skillRating(1500 + (i % 100))
                .latency(40 + (i % 60))
                .region("us-east")
                .build();
            
            matchmakingService.joinQueue(player);
        }

        Instant start = Instant.now();
        
        // Process matchmaking
        var matches = matchmakingService.processMatchmaking();
        
        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);

        System.out.println("Created " + matches.size() + " matches in " + duration.toMillis() + "ms");
        
        assertTrue(matches.size() > 0, "Should create at least one match");
        assertTrue(duration.toMillis() < 1000, "Matchmaking should complete in under 1 second");
    }

    @Test
    @DisplayName("Should maintain accuracy under load")
    void testMatchAccuracy() {
        // Add players with varying skills
        for (int i = 0; i < 50; i++) {
            Player player = Player.builder()
                .playerId("accuracy-player" + i)
                .username("AccUser" + i)
                .skillRating(1000 + (i * 50))  // Wide skill range
                .latency(50)
                .region("us-east")
                .build();
            
            matchmakingService.joinQueue(player);
        }

        var matches = matchmakingService.processMatchmaking();

        // Verify match quality
        int accurateMatches = 0;
        for (var match : matches) {
            var players = match.getPlayers();
            if (players.size() == 2) {
                int skillDiff = Math.abs(
                    players.get(0).getSkillRating() - players.get(1).getSkillRating()
                );
                if (skillDiff <= 200) {  // Within acceptable range
                    accurateMatches++;
                }
            }
        }

        double accuracy = (double) accurateMatches / matches.size() * 100;
        System.out.println("Match accuracy: " + accuracy + "%");
        
        assertTrue(accuracy >= 95, "Match accuracy should be >= 95%");
    }
}
