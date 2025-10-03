package com.matchmaking.service;

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
 * Advanced Redis queue with priority support
 * Prioritizes players based on wait time and skill rating
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisPriorityQueueService {

    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String PRIORITY_QUEUE_KEY = "matchmaking:priority:queue";
    private static final String PLAYER_WAIT_TIME_KEY = "matchmaking:wait:";

    /**
     * Enqueue with priority calculation
     * Priority = wait_time_seconds + (skill_deviation / 10)
     */
    public void enqueueWithPriority(Player player) {
        long waitTimeSeconds = calculateWaitTime(player.getPlayerId());
        int skillDeviation = Math.abs(player.getSkillRating() - 1500);
        
        // Higher score = higher priority
        double priority = waitTimeSeconds + (skillDeviation / 10.0);
        
        redisTemplate.opsForZSet().add(PRIORITY_QUEUE_KEY, player.getPlayerId(), priority);
        
        // Track when player joined
        String waitKey = PLAYER_WAIT_TIME_KEY + player.getPlayerId();
        redisTemplate.opsForValue().set(waitKey, System.currentTimeMillis(), 600, TimeUnit.SECONDS);
        
        log.debug("Enqueued player {} with priority {}", player.getPlayerId(), priority);
    }

    /**
     * Get players ordered by priority (highest first)
     */
    public List<String> getPlayersByPriority(int limit) {
        Set<Object> playerIds = redisTemplate.opsForZSet()
            .reverseRange(PRIORITY_QUEUE_KEY, 0, limit - 1);
        
        return playerIds != null ? 
            playerIds.stream().map(Object::toString).toList() : 
            new ArrayList<>();
    }

    /**
     * Calculate how long a player has been waiting
     */
    private long calculateWaitTime(String playerId) {
        String waitKey = PLAYER_WAIT_TIME_KEY + playerId;
        Object joinTime = redisTemplate.opsForValue().get(waitKey);
        
        if (joinTime != null) {
            long joinedAt = (Long) joinTime;
            return (System.currentTimeMillis() - joinedAt) / 1000; // seconds
        }
        
        return 0;
    }

    /**
     * Update priority for a player (increases with wait time)
     */
    public void updatePriority(String playerId, int skillRating) {
        long waitTimeSeconds = calculateWaitTime(playerId);
        int skillDeviation = Math.abs(skillRating - 1500);
        double newPriority = waitTimeSeconds + (skillDeviation / 10.0);
        
        redisTemplate.opsForZSet().add(PRIORITY_QUEUE_KEY, playerId, newPriority);
    }
}
