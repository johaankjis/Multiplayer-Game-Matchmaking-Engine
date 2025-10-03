package com.matchmaking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Distributed locking service using Redis
 * Prevents race conditions in concurrent matchmaking
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisLockService {

    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String LOCK_KEY_PREFIX = "matchmaking:lock:";
    private static final long LOCK_TIMEOUT = 5000; // 5 seconds

    /**
     * Acquire a distributed lock
     */
    public boolean acquireLock(String lockName) {
        String lockKey = LOCK_KEY_PREFIX + lockName;
        Boolean acquired = redisTemplate.opsForValue()
            .setIfAbsent(lockKey, "locked", LOCK_TIMEOUT, TimeUnit.MILLISECONDS);
        
        if (Boolean.TRUE.equals(acquired)) {
            log.debug("Acquired lock: {}", lockName);
            return true;
        }
        
        return false;
    }

    /**
     * Release a distributed lock
     */
    public void releaseLock(String lockName) {
        String lockKey = LOCK_KEY_PREFIX + lockName;
        redisTemplate.delete(lockKey);
        log.debug("Released lock: {}", lockName);
    }

    /**
     * Execute code with lock protection
     */
    public <T> T executeWithLock(String lockName, LockCallback<T> callback) {
        if (acquireLock(lockName)) {
            try {
                return callback.execute();
            } finally {
                releaseLock(lockName);
            }
        } else {
            throw new RuntimeException("Failed to acquire lock: " + lockName);
        }
    }

    @FunctionalInterface
    public interface LockCallback<T> {
        T execute();
    }
}
