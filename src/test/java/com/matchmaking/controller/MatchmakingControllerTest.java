package com.matchmaking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matchmaking.model.MatchmakingRequest;
import com.matchmaking.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Matchmaking REST API
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Matchmaking API Tests")
class MatchmakingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String authToken;

    @BeforeEach
    void setUp() {
        authToken = jwtTokenProvider.generateToken("test-player");
    }

    @Test
    @DisplayName("Should join queue successfully")
    void testJoinQueue() throws Exception {
        MatchmakingRequest request = MatchmakingRequest.builder()
            .playerId("player1")
            .username("TestPlayer")
            .skillRating(1500)
            .latency(50)
            .region("us-east")
            .build();

        mockMvc.perform(post("/api/matchmaking/joinQueue")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").value("player1"));
    }

    @Test
    @DisplayName("Should reject invalid request")
    void testJoinQueueValidation() throws Exception {
        MatchmakingRequest request = MatchmakingRequest.builder()
            .playerId("")  // Invalid: empty player ID
            .username("TestPlayer")
            .skillRating(1500)
            .latency(50)
            .region("us-east")
            .build();

        mockMvc.perform(post("/api/matchmaking/joinQueue")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should leave queue successfully")
    void testLeaveQueue() throws Exception {
        // First join
        MatchmakingRequest joinRequest = MatchmakingRequest.builder()
            .playerId("player2")
            .username("TestPlayer2")
            .skillRating(1500)
            .latency(50)
            .region("us-east")
            .build();

        mockMvc.perform(post("/api/matchmaking/joinQueue")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(joinRequest)))
            .andExpect(status().isOk());

        // Then leave
        mockMvc.perform(post("/api/matchmaking/leaveQueue")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"playerId\":\"player2\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Should get queue status")
    void testGetQueueStatus() throws Exception {
        mockMvc.perform(get("/api/matchmaking/queueStatus")
                .header("Authorization", "Bearer " + authToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.queueSize").exists());
    }

    @Test
    @DisplayName("Should reject unauthorized requests")
    void testUnauthorizedAccess() throws Exception {
        mockMvc.perform(get("/api/matchmaking/queueStatus"))
            .andExpect(status().isUnauthorized());
    }
}
