package com.matchmaking.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Player implements Serializable {
    private String playerId;
    private String username;
    private int skillRating; // Elo/MMR score
    private int latency; // Ping in milliseconds
    private String region;
    private Instant queuedAt;
    private PlayerStatus status;
    
    public enum PlayerStatus {
        QUEUED,
        MATCHED,
        IN_GAME,
        OFFLINE
    }
}
