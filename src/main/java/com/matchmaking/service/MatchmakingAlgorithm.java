package com.matchmaking.service;

import com.matchmaking.model.Player;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Core matchmaking algorithm implementing skill-based and latency-aware matching
 */
@Slf4j
@Component
public class MatchmakingAlgorithm {

    @Value("${matchmaking.skill.max-gap:200}")
    private int maxSkillGap;

    @Value("${matchmaking.latency.max-threshold:100}")
    private int maxLatencyThreshold;

    /**
     * Check if two players are compatible for matchmaking
     * Based on skill rating (Elo/MMR) and network latency
     */
    public boolean arePlayersCompatible(Player player1, Player player2, List<Player> existingPlayers) {
        // Check skill compatibility
        if (!isSkillCompatible(player1, player2)) {
            log.debug("Players {} and {} not skill compatible (ratings: {} vs {})", 
                player1.getPlayerId(), player2.getPlayerId(), 
                player1.getSkillRating(), player2.getSkillRating());
            return false;
        }

        // Check latency compatibility
        if (!isLatencyCompatible(player1, player2)) {
            log.debug("Players {} and {} not latency compatible (latencies: {}ms vs {}ms)", 
                player1.getPlayerId(), player2.getPlayerId(), 
                player1.getLatency(), player2.getLatency());
            return false;
        }

        // Check region compatibility
        if (!isRegionCompatible(player1, player2)) {
            log.debug("Players {} and {} not region compatible (regions: {} vs {})", 
                player1.getPlayerId(), player2.getPlayerId(), 
                player1.getRegion(), player2.getRegion());
            return false;
        }

        // Check compatibility with existing players in the match
        for (Player existing : existingPlayers) {
            if (!isSkillCompatible(existing, player2) || !isLatencyCompatible(existing, player2)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Check if players have compatible skill ratings
     * Uses Elo/MMR-style scoring with configurable max gap
     */
    private boolean isSkillCompatible(Player player1, Player player2) {
        int skillDifference = Math.abs(player1.getSkillRating() - player2.getSkillRating());
        return skillDifference <= maxSkillGap;
    }

    /**
     * Check if players have compatible network latency
     * Ensures both players can connect to the same server with acceptable ping
     */
    private boolean isLatencyCompatible(Player player1, Player player2) {
        // Both players should have latency below threshold
        return player1.getLatency() <= maxLatencyThreshold 
            && player2.getLatency() <= maxLatencyThreshold;
    }

    /**
     * Check if players are in compatible regions
     * Players in the same region get priority for better connection quality
     */
    private boolean isRegionCompatible(Player player1, Player player2) {
        // For now, require same region for optimal latency
        // Could be expanded to allow nearby regions
        return player1.getRegion().equals(player2.getRegion());
    }

    /**
     * Calculate match quality score (0-100)
     * Higher score means better match quality
     */
    public double calculateMatchQuality(List<Player> players) {
        if (players.size() < 2) {
            return 0.0;
        }

        double skillScore = calculateSkillScore(players);
        double latencyScore = calculateLatencyScore(players);
        
        // Weighted average: 60% skill, 40% latency
        return (skillScore * 0.6) + (latencyScore * 0.4);
    }

    /**
     * Calculate skill balance score
     */
    private double calculateSkillScore(List<Player> players) {
        int minSkill = players.stream().mapToInt(Player::getSkillRating).min().orElse(0);
        int maxSkill = players.stream().mapToInt(Player::getSkillRating).max().orElse(0);
        int skillRange = maxSkill - minSkill;
        
        // Score decreases as skill range increases
        double score = 100.0 - ((double) skillRange / maxSkillGap * 100.0);
        return Math.max(0, Math.min(100, score));
    }

    /**
     * Calculate latency quality score
     */
    private double calculateLatencyScore(List<Player> players) {
        double avgLatency = players.stream()
            .mapToInt(Player::getLatency)
            .average()
            .orElse(0);
        
        // Score decreases as average latency increases
        double score = 100.0 - (avgLatency / maxLatencyThreshold * 100.0);
        return Math.max(0, Math.min(100, score));
    }

    /**
     * Estimate wait time for a player based on their criteria
     */
    public long estimateWaitTime(Player player, int queueSize) {
        // Base wait time on queue size and player's criteria strictness
        long baseWaitTime = 5000; // 5 seconds base
        
        // Adjust based on skill rating (extreme ratings wait longer)
        int skillDeviation = Math.abs(player.getSkillRating() - 1500); // 1500 is average
        long skillPenalty = (skillDeviation / 100) * 1000; // +1s per 100 rating deviation
        
        // Adjust based on latency requirements
        long latencyPenalty = player.getLatency() > 50 ? 2000 : 0;
        
        // Adjust based on queue size
        long queueBonus = queueSize > 10 ? -2000 : 0; // Faster with more players
        
        return Math.max(1000, baseWaitTime + skillPenalty + latencyPenalty + queueBonus);
    }
}
