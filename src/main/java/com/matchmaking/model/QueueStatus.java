package com.matchmaking.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueStatus {
    private int totalPlayersInQueue;
    private long averageWaitTime;
    private int matchesCreatedToday;
    private double averageMatchQuality;
}
