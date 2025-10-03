package com.matchmaking.service;

import com.matchmaking.model.Match;
import com.matchmaking.model.Player;
import com.matchmaking.model.MatchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchmakingService {

    private final MatchmakingAlgorithm matchmakingAlgorithm;
    private final QueueService queueService;
    private final RedisStreamService redisStreamService;
    private final RedisCacheService redisCacheService;
    private final RedisLockService redisLockService;
    
    @Value("${matchmaking.match.size:2}")
    private int matchSize;

    /**
     * Add a player to the matchmaking queue
     */
    public void joinQueue(Player player) {
        player.setQueuedAt(Instant.now());
        player.setStatus(Player.PlayerStatus.QUEUED);
        queueService.enqueue(player);
        log.info("Player {} joined queue with skill rating {} and latency {}ms", 
            player.getPlayerId(), player.getSkillRating(), player.getLatency());
    }

    /**
     * Remove a player from the matchmaking queue
     */
    public boolean leaveQueue(String playerId) {
        boolean removed = queueService.dequeue(playerId);
        if (removed) {
            log.info("Player {} left the queue", playerId);
        }
        return removed;
    }

    /**
     * Attempt to find a match for players in the queue
     */
    public List<Match> processMatchmaking() {
        return redisLockService.executeWithLock("matchmaking-process", () -> {
            List<Match> matches = new ArrayList<>();
            List<Player> queuedPlayers = queueService.getQueuedPlayers();
            
            if (queuedPlayers.size() < matchSize) {
                log.debug("Not enough players in queue: {} (need {})", queuedPlayers.size(), matchSize);
                return matches;
            }

            log.info("Processing matchmaking for {} players", queuedPlayers.size());
            
            // Group players into potential matches
            while (queuedPlayers.size() >= matchSize) {
                Player anchor = queuedPlayers.get(0);
                List<Player> matchedPlayers = new ArrayList<>();
                matchedPlayers.add(anchor);
                
                // Find compatible players for this match
                for (int i = 1; i < queuedPlayers.size() && matchedPlayers.size() < matchSize; i++) {
                    Player candidate = queuedPlayers.get(i);
                    
                    if (matchmakingAlgorithm.arePlayersCompatible(anchor, candidate, matchedPlayers)) {
                        matchedPlayers.add(candidate);
                    }
                }
                
                // If we found enough players, create a match
                if (matchedPlayers.size() == matchSize) {
                    Match match = createMatch(matchedPlayers);
                    matches.add(match);
                    
                    // Remove matched players from queue
                    matchedPlayers.forEach(p -> {
                        queuedPlayers.remove(p);
                        queueService.dequeue(p.getPlayerId());
                        queueService.storeMatch(p.getPlayerId(), match);
                    });
                    
                    redisStreamService.publishMatchCreated(match);
                    
                    redisCacheService.incrementMatchCounter();
                    
                    log.info("Created match {} with {} players (avg skill: {}, avg latency: {}ms)", 
                        match.getMatchId(), matchedPlayers.size(), 
                        match.getAverageSkillRating(), match.getAverageLatency());
                } else {
                    // Can't find a full match with this anchor, try next player
                    queuedPlayers.remove(0);
                }
            }
            
            return matches;
        });
    }

    /**
     * Get match result for a player
     */
    public MatchResult getMatchResult(String playerId) {
        Match match = queueService.getMatch(playerId);
        
        if (match != null) {
            return MatchResult.builder()
                .matchId(match.getMatchId())
                .match(match)
                .success(true)
                .message("Match found")
                .build();
        }
        
        return MatchResult.builder()
            .success(false)
            .message("No match found yet")
            .build();
    }

    /**
     * Create a match from a list of players
     */
    private Match createMatch(List<Player> players) {
        int totalSkill = players.stream().mapToInt(Player::getSkillRating).sum();
        int totalLatency = players.stream().mapToInt(Player::getLatency).sum();
        
        return Match.builder()
            .matchId(UUID.randomUUID().toString())
            .players(players)
            .averageSkillRating(totalSkill / players.size())
            .averageLatency(totalLatency / players.size())
            .serverRegion(determineServerRegion(players))
            .createdAt(Instant.now())
            .status(Match.MatchStatus.READY)
            .build();
    }

    /**
     * Determine the best server region based on player locations
     */
    private String determineServerRegion(List<Player> players) {
        // Simple implementation: use the most common region
        return players.stream()
            .map(Player::getRegion)
            .findFirst()
            .orElse("us-east");
    }
}
