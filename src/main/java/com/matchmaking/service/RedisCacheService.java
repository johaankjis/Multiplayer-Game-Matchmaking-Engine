package com.matchmaking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Redis caching service for frequently accessed data
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String STATS_KEY_PREFIX = "matchmaking:stats:";
    private static final String LEADERBOARD_KEY = "matchmaking:leaderboard";

    /**
     * Cache player statistics
     */
    public void cachePlayerStats(String playerId, Object stats) {
        String key = STATS_KEY_PREFIX + playerId;
        redisTemplate.opsForValue().set(key, stats, 3600, TimeUnit.SECONDS);
    }

    /**
     * Get cached player statistics
     */
    public Object getPlayerStats(String playerId) {
        String key = STATS_KEY_PREFIX + playerId;
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * Update player ranking in leaderboard
     */
    public void updateLeaderboard(String playerId, int skillRating) {
        redisTemplate.opsForZSet().add(LEADERBOARD_KEY, playerId, skillRating);
    }

    /**
     * Get top players from leaderboard
     */
    public Object getTopPlayers(int limit) {
        return redisTemplate.opsForZSet()
            .reverseRangeWithScores(LEADERBOARD_KEY, 0, limit - 1);
    }

    /**
     * Get player rank
     */
    public Long getPlayerRank(String playerId) {
        Long rank = redisTemplate.opsForZSet().reverseRank(LEADERBOARD_KEY, playerId);
        return rank != null ? rank + 1 : null;
    }

    /**
     * Increment match counter
     */
    public void incrementMatchCounter() {
        redisTemplate.opsForValue().increment("matchmaking:stats:total_matches");
    }

    /**
     * Get total matches created
     */
    public Long getTotalMatches() {
        Object value = redisTemplate.opsForValue().get("matchmaking:stats:total_matches");
        return value != null ? Long.parseLong(value.toString()) : 0L;
    }
}
