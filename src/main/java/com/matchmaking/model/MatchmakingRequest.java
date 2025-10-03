package com.matchmaking.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchmakingRequest {
    @NotBlank(message = "Player ID is required")
    private String playerId;
    
    @NotBlank(message = "Username is required")
    private String username;
    
    @Min(value = 0, message = "Skill rating must be non-negative")
    @Max(value = 5000, message = "Skill rating must not exceed 5000")
    private int skillRating;
    
    @Min(value = 0, message = "Latency must be non-negative")
    @Max(value = 1000, message = "Latency must not exceed 1000ms")
    private int latency;
    
    @NotBlank(message = "Region is required")
    private String region;
}
