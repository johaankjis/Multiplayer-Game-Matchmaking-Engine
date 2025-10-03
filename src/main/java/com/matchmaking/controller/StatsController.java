package com.matchmaking.controller;

import com.matchmaking.dto.ApiResponse;
import com.matchmaking.service.RedisCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API endpoints for statistics and leaderboards
 */
@Slf4j
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final RedisCacheService redisCacheService;

    /**
     * Get total matches created
     * GET /api/stats/totalMatches
     */
    @GetMapping("/totalMatches")
    public ResponseEntity<ApiResponse<Long>> getTotalMatches() {
        Long totalMatches = redisCacheService.getTotalMatches();
        return ResponseEntity.ok(ApiResponse.success(
            totalMatches,
            "Total matches retrieved"
        ));
    }

    /**
     * Get top players from leaderboard
     * GET /api/stats/leaderboard
     */
    @GetMapping("/leaderboard")
    public ResponseEntity<ApiResponse<Object>> getLeaderboard(
            @RequestParam(defaultValue = "10") int limit) {
        
        Object topPlayers = redisCacheService.getTopPlayers(limit);
        return ResponseEntity.ok(ApiResponse.success(
            topPlayers,
            "Leaderboard retrieved"
        ));
    }

    /**
     * Get player rank
     * GET /api/stats/rank/{playerId}
     */
    @GetMapping("/rank/{playerId}")
    public ResponseEntity<ApiResponse<Long>> getPlayerRank(
            @PathVariable String playerId) {
        
        Long rank = redisCacheService.getPlayerRank(playerId);
        
        if (rank != null) {
            return ResponseEntity.ok(ApiResponse.success(
                rank,
                "Player rank: " + rank
            ));
        } else {
            return ResponseEntity.ok(ApiResponse.error("Player not ranked"));
        }
    }

    /**
     * Update player skill rating
     * POST /api/stats/updateRating
     */
    @PostMapping("/updateRating")
    public ResponseEntity<ApiResponse<String>> updateRating(
            @RequestBody UpdateRatingRequest request) {
        
        redisCacheService.updateLeaderboard(
            request.playerId(), 
            request.skillRating()
        );
        
        return ResponseEntity.ok(ApiResponse.success(
            request.playerId(),
            "Rating updated successfully"
        ));
    }

    public record UpdateRatingRequest(String playerId, int skillRating) {}
}
