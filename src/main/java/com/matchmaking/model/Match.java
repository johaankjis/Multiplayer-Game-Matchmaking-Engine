package com.matchmaking.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Match implements Serializable {
    private String matchId;
    private List<Player> players;
    private int averageSkillRating;
    private int averageLatency;
    private String serverRegion;
    private Instant createdAt;
    private MatchStatus status;
    
    public enum MatchStatus {
        PENDING,
        READY,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED
    }
}
