package com.matchmaking.service;

import com.matchmaking.model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for QueueService with Redis
 */
@SpringBootTest
@Testcontainers
@DisplayName("Queue Service Integration Tests")
class QueueServiceTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
        .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @Autowired
    private QueueService queueService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @BeforeEach
    void setUp() {
        queueService.clearQueue();
    }

    @Test
    @DisplayName("Should enqueue player successfully")
    void testEnqueuePlayer() {
        Player player = createTestPlayer("player1");
        
        queueService.enqueue(player);
        
        assertTrue(queueService.isPlayerInQueue("player1"));
        assertEquals(1, queueService.getQueueSize());
    }

    @Test
    @DisplayName("Should dequeue player successfully")
    void testDequeuePlayer() {
        Player player = createTestPlayer("player1");
        queueService.enqueue(player);
        
        boolean removed = queueService.dequeue("player1");
        
        assertTrue(removed);
        assertFalse(queueService.isPlayerInQueue("player1"));
        assertEquals(0, queueService.getQueueSize());
    }

    @Test
    @DisplayName("Should maintain FIFO order")
    void testFIFOOrder() {
        Player player1 = createTestPlayer("player1");
        Player player2 = createTestPlayer("player2");
        Player player3 = createTestPlayer("player3");
        
        queueService.enqueue(player1);
        queueService.enqueue(player2);
        queueService.enqueue(player3);
        
        List<Player> players = queueService.getQueuedPlayers();
        
        assertEquals(3, players.size());
        assertEquals("player1", players.get(0).getPlayerId());
        assertEquals("player2", players.get(1).getPlayerId());
        assertEquals("player3", players.get(2).getPlayerId());
    }

    @Test
    @DisplayName("Should get correct player position")
    void testPlayerPosition() {
        queueService.enqueue(createTestPlayer("player1"));
        queueService.enqueue(createTestPlayer("player2"));
        queueService.enqueue(createTestPlayer("player3"));
        
        assertEquals(1, queueService.getPlayerPosition("player1"));
        assertEquals(2, queueService.getPlayerPosition("player2"));
        assertEquals(3, queueService.getPlayerPosition("player3"));
    }

    private Player createTestPlayer(String id) {
        return Player.builder()
            .playerId(id)
            .username("User" + id)
            .skillRating(1500)
            .latency(50)
            .region("us-east")
            .status(Player.PlayerStatus.QUEUED)
            .build();
    }
}
