package com.matchmaking.service;

import com.matchmaking.model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MatchmakingAlgorithm
 */
@DisplayName("Matchmaking Algorithm Tests")
class MatchmakingAlgorithmTest {

    private MatchmakingAlgorithm algorithm;

    @BeforeEach
    void setUp() {
        algorithm = new MatchmakingAlgorithm();
        // Set test values using reflection or create test configuration
    }

    @Test
    @DisplayName("Should match players with similar skill ratings")
    void testSkillBasedMatching() {
        Player player1 = createPlayer("player1", 1500, 50, "us-east");
        Player player2 = createPlayer("player2", 1550, 45, "us-east");
        
        boolean compatible = algorithm.arePlayersCompatible(
            player1, player2, List.of(player1)
        );
        
        assertTrue(compatible, "Players with similar skills should be compatible");
    }

    @Test
    @DisplayName("Should reject players with large skill gap")
    void testSkillGapRejection() {
        Player player1 = createPlayer("player1", 1500, 50, "us-east");
        Player player2 = createPlayer("player2", 2000, 45, "us-east");
        
        boolean compatible = algorithm.arePlayersCompatible(
            player1, player2, List.of(player1)
        );
        
        assertFalse(compatible, "Players with large skill gap should not be compatible");
    }

    @Test
    @DisplayName("Should match players with low latency")
    void testLatencyBasedMatching() {
        Player player1 = createPlayer("player1", 1500, 50, "us-east");
        Player player2 = createPlayer("player2", 1520, 60, "us-east");
        
        boolean compatible = algorithm.arePlayersCompatible(
            player1, player2, List.of(player1)
        );
        
        assertTrue(compatible, "Players with acceptable latency should be compatible");
    }

    @Test
    @DisplayName("Should reject players with high latency")
    void testHighLatencyRejection() {
        Player player1 = createPlayer("player1", 1500, 50, "us-east");
        Player player2 = createPlayer("player2", 1520, 150, "us-east");
        
        boolean compatible = algorithm.arePlayersCompatible(
            player1, player2, List.of(player1)
        );
        
        assertFalse(compatible, "Players with high latency should not be compatible");
    }

    @Test
    @DisplayName("Should match players in same region")
    void testRegionMatching() {
        Player player1 = createPlayer("player1", 1500, 50, "us-east");
        Player player2 = createPlayer("player2", 1520, 45, "us-east");
        
        boolean compatible = algorithm.arePlayersCompatible(
            player1, player2, List.of(player1)
        );
        
        assertTrue(compatible, "Players in same region should be compatible");
    }

    @Test
    @DisplayName("Should reject players in different regions")
    void testDifferentRegionRejection() {
        Player player1 = createPlayer("player1", 1500, 50, "us-east");
        Player player2 = createPlayer("player2", 1520, 45, "eu-west");
        
        boolean compatible = algorithm.arePlayersCompatible(
            player1, player2, List.of(player1)
        );
        
        assertFalse(compatible, "Players in different regions should not be compatible");
    }

    @Test
    @DisplayName("Should calculate match quality correctly")
    void testMatchQualityCalculation() {
        List<Player> players = List.of(
            createPlayer("player1", 1500, 50, "us-east"),
            createPlayer("player2", 1520, 55, "us-east")
        );
        
        double quality = algorithm.calculateMatchQuality(players);
        
        assertTrue(quality >= 0 && quality <= 100, "Match quality should be between 0 and 100");
        assertTrue(quality > 80, "High quality match should have score > 80");
    }

    private Player createPlayer(String id, int skill, int latency, String region) {
        return Player.builder()
            .playerId(id)
            .username("User" + id)
            .skillRating(skill)
            .latency(latency)
            .region(region)
            .status(Player.PlayerStatus.QUEUED)
            .build();
    }
}
