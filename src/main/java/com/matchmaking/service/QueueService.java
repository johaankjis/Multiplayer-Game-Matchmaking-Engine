package com.matchmaking.service;

import com.matchmaking.model.Match;
import com.matchmaking.model.Player;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis-backed queue service for high-performance concurrent matchmaking
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QueueService {

    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String QUEUE_KEY = "matchmaking:queue";
    private static final String PLAYER_KEY_PREFIX = "matchmaking:player:";
    private static final String MATCH_KEY_PREFIX = "matchmaking:match:";
    private static final long QUEUE_TTL = 300; // 5 minutes
    private static final long MATCH_TTL = 600; // 10 minutes

    /**
     * Add a player to the matchmaking queue
     */
    public void enqueue(Player player) {
        String playerKey = PLAYER_KEY_PREFIX + player.getPlayerId();
        
        // Store player data
        redisTemplate.opsForValue().set(playerKey, player, QUEUE_TTL, TimeUnit.SECONDS);
        
        // Add to sorted set with timestamp as score for FIFO ordering
        double score = System.currentTimeMillis();
        redisTemplate.opsForZSet().add(QUEUE_KEY, player.getPlayerId(), score);
        
        log.debug("Enqueued player {} with score {}", player.getPlayerId(), score);
    }

    /**
     * Remove a player from the matchmaking queue
     */
    public boolean dequeue(String playerId) {
        String playerKey = PLAYER_KEY_PREFIX + playerId;
        
        // Remove from sorted set
        Long removed = redisTemplate.opsForZSet().remove(QUEUE_KEY, playerId);
        
        // Delete player data
        redisTemplate.delete(playerKey);
        
        return removed != null && removed > 0;
    }

    /**
     * Get all players currently in the queue
     */
    public List<Player> getQueuedPlayers() {
        Set<Object> playerIds = redisTemplate.opsForZSet().range(QUEUE_KEY, 0, -1);
        List<Player> players = new ArrayList<>();
        
        if (playerIds != null) {
            for (Object playerId : playerIds) {
                String playerKey = PLAYER_KEY_PREFIX + playerId;
                Player player = (Player) redisTemplate.opsForValue().get(playerKey);
                
                if (player != null) {
                    players.add(player);
                } else {
                    // Clean up stale entry
                    redisTemplate.opsForZSet().remove(QUEUE_KEY, playerId);
                }
            }
        }
        
        return players;
    }

    /**
     * Get current queue size
     */
    public long getQueueSize() {
        Long size = redisTemplate.opsForZSet().size(QUEUE_KEY);
        return size != null ? size : 0;
    }

    /**
     * Store a match result for a player
     */
    public void storeMatch(String playerId, Match match) {
        String matchKey = MATCH_KEY_PREFIX + playerId;
        redisTemplate.opsForValue().set(matchKey, match, MATCH_TTL, TimeUnit.SECONDS);
        log.debug("Stored match {} for player {}", match.getMatchId(), playerId);
    }

    /**
     * Retrieve a match result for a player
     */
    public Match getMatch(String playerId) {
        String matchKey = MATCH_KEY_PREFIX + playerId;
        return (Match) redisTemplate.opsForValue().get(matchKey);
    }

    /**
     * Check if a player is in the queue
     */
    public boolean isPlayerInQueue(String playerId) {
        Double score = redisTemplate.opsForZSet().score(QUEUE_KEY, playerId);
        return score != null;
    }

    /**
     * Get player's position in queue
     */
    public long getPlayerPosition(String playerId) {
        Long rank = redisTemplate.opsForZSet().rank(QUEUE_KEY, playerId);
        return rank != null ? rank + 1 : -1;
    }

    /**
     * Clear all queue data (for testing/maintenance)
     */
    public void clearQueue() {
        redisTemplate.delete(QUEUE_KEY);
        log.info("Cleared matchmaking queue");
    }
}
