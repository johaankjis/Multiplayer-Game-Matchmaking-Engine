package com.matchmaking.controller;

import com.matchmaking.dto.ApiResponse;
import com.matchmaking.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication endpoints for obtaining JWT tokens
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Generate authentication token for a player
     * POST /api/auth/token
     */
    @PostMapping("/token")
    public ResponseEntity<ApiResponse<TokenResponse>> generateToken(
            @RequestBody TokenRequest request) {
        
        log.info("Generating token for player {}", request.playerId());
        
        // In production, validate credentials here
        String token = jwtTokenProvider.generateToken(request.playerId());
        
        TokenResponse response = new TokenResponse(token, "Bearer");
        
        return ResponseEntity.ok(ApiResponse.success(
            response,
            "Token generated successfully"
        ));
    }

    public record TokenRequest(String playerId, String password) {}
    public record TokenResponse(String token, String type) {}
}
