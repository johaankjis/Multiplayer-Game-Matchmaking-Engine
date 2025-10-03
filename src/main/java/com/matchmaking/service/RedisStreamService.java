package com.matchmaking.service;

import com.matchmaking.model.Match;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Redis Streams for real-time match notifications
 * Allows game clients to subscribe to match events
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisStreamService {

    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String MATCH_STREAM = "matchmaking:stream:matches";
    private static final String PLAYER_STREAM_PREFIX = "matchmaking:stream:player:";

    /**
     * Publish match creation event to stream
     */
    public void publishMatchCreated(Match match) {
        Map<String, Object> matchData = new HashMap<>();
        matchData.put("matchId", match.getMatchId());
        matchData.put("playerCount", match.getPlayers().size());
        matchData.put("averageSkill", match.getAverageSkillRating());
        matchData.put("averageLatency", match.getAverageLatency());
        matchData.put("region", match.getServerRegion());
        matchData.put("timestamp", match.getCreatedAt().toString());
        
        ObjectRecord<String, Map<String, Object>> record = StreamRecords
            .newRecord()
            .ofObject(matchData)
            .withStreamKey(MATCH_STREAM);
        
        redisTemplate.opsForStream().add(record);
        
        log.debug("Published match {} to stream", match.getMatchId());
        
        // Notify individual players
        match.getPlayers().forEach(player -> 
            publishPlayerMatchFound(player.getPlayerId(), match)
        );
    }

    /**
     * Publish match found notification to player-specific stream
     */
    private void publishPlayerMatchFound(String playerId, Match match) {
        String playerStream = PLAYER_STREAM_PREFIX + playerId;
        
        Map<String, Object> notification = new HashMap<>();
        notification.put("event", "MATCH_FOUND");
        notification.put("matchId", match.getMatchId());
        notification.put("serverRegion", match.getServerRegion());
        
        ObjectRecord<String, Map<String, Object>> record = StreamRecords
            .newRecord()
            .ofObject(notification)
            .withStreamKey(playerStream);
        
        redisTemplate.opsForStream().add(record);
    }
}
