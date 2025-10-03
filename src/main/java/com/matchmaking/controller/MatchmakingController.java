package com.matchmaking.controller;

import com.matchmaking.dto.ApiResponse;
import com.matchmaking.model.Match;
import com.matchmaking.model.MatchResult;
import com.matchmaking.model.MatchmakingRequest;
import com.matchmaking.model.Player;
import com.matchmaking.service.MatchmakingService;
import com.matchmaking.service.QueueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API endpoints for matchmaking operations
 */
@Slf4j
@RestController
@RequestMapping("/api/matchmaking")
@RequiredArgsConstructor
public class MatchmakingController {

    private final MatchmakingService matchmakingService;
    private final QueueService queueService;

    /**
     * Join the matchmaking queue
     * POST /api/matchmaking/joinQueue
     */
    @PostMapping("/joinQueue")
    public ResponseEntity<ApiResponse<String>> joinQueue(
            @Valid @RequestBody MatchmakingRequest request) {
        
        log.info("Player {} requesting to join queue", request.getPlayerId());
        
        // Check if player is already in queue
        if (queueService.isPlayerInQueue(request.getPlayerId())) {
            return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("Player already in queue"));
        }
        
        // Convert request to Player model
        Player player = Player.builder()
            .playerId(request.getPlayerId())
            .username(request.getUsername())
            .skillRating(request.getSkillRating())
            .latency(request.getLatency())
            .region(request.getRegion())
            .build();
        
        matchmakingService.joinQueue(player);
        
        long position = queueService.getPlayerPosition(request.getPlayerId());
        
        return ResponseEntity.ok(ApiResponse.success(
            request.getPlayerId(),
            "Successfully joined queue at position " + position
        ));
    }

    /**
     * Leave the matchmaking queue
     * POST /api/matchmaking/leaveQueue
     */
    @PostMapping("/leaveQueue")
    public ResponseEntity<ApiResponse<String>> leaveQueue(
            @RequestBody LeaveQueueRequest request) {
        
        log.info("Player {} requesting to leave queue", request.getPlayerId());
        
        boolean removed = matchmakingService.leaveQueue(request.getPlayerId());
        
        if (removed) {
            return ResponseEntity.ok(ApiResponse.success(
                request.getPlayerId(),
                "Successfully left the queue"
            ));
        } else {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Player not found in queue"));
        }
    }

    /**
     * Get match result for a player
     * GET /api/matchmaking/matchResult/{playerId}
     */
    @GetMapping("/matchResult/{playerId}")
    public ResponseEntity<ApiResponse<Match>> getMatchResult(
            @PathVariable String playerId) {
        
        log.debug("Checking match result for player {}", playerId);
        
        MatchResult result = matchmakingService.getMatchResult(playerId);
        
        if (result.isSuccess()) {
            return ResponseEntity.ok(ApiResponse.success(
                result.getMatch(),
                "Match found"
            ));
        } else {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("No match found yet"));
        }
    }

    /**
     * Get current queue status
     * GET /api/matchmaking/queueStatus
     */
    @GetMapping("/queueStatus")
    public ResponseEntity<ApiResponse<QueueStatus>> getQueueStatus() {
        long queueSize = queueService.getQueueSize();
        
        QueueStatus status = QueueStatus.builder()
            .queueSize(queueSize)
            .estimatedWaitTime(calculateEstimatedWaitTime(queueSize))
            .build();
        
        return ResponseEntity.ok(ApiResponse.success(
            status,
            "Queue status retrieved"
        ));
    }

    /**
     * Get player's position in queue
     * GET /api/matchmaking/queuePosition/{playerId}
     */
    @GetMapping("/queuePosition/{playerId}")
    public ResponseEntity<ApiResponse<Long>> getQueuePosition(
            @PathVariable String playerId) {
        
        long position = queueService.getPlayerPosition(playerId);
        
        if (position > 0) {
            return ResponseEntity.ok(ApiResponse.success(
                position,
                "Player position: " + position
            ));
        } else {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Player not in queue"));
        }
    }

    /**
     * Calculate estimated wait time based on queue size
     */
    private long calculateEstimatedWaitTime(long queueSize) {
        // Simple estimation: 5 seconds per 2 players
        return (queueSize / 2) * 5;
    }

    // Inner classes for request/response
    
    public record LeaveQueueRequest(String playerId) {}
    
    @lombok.Data
    @lombok.Builder
    public static class QueueStatus {
        private long queueSize;
        private long estimatedWaitTime;
    }
}
