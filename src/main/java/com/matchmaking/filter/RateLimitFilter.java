package com.matchmaking.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Rate limiting filter using Redis
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter implements Filter {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${rate.limit.requests:100}")
    private int maxRequests;

    @Value("${rate.limit.duration:60000}")
    private long durationMs;

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String clientId = getClientIdentifier(httpRequest);
        String rateLimitKey = "rate_limit:" + clientId;
        
        // Get current request count
        Object countObj = redisTemplate.opsForValue().get(rateLimitKey);
        int currentCount = countObj != null ? Integer.parseInt(countObj.toString()) : 0;
        
        if (currentCount >= maxRequests) {
            log.warn("Rate limit exceeded for client: {}", clientId);
            httpResponse.setStatus(429); // Too Many Requests
            httpResponse.getWriter().write("Rate limit exceeded");
            return;
        }
        
        // Increment counter
        redisTemplate.opsForValue().increment(rateLimitKey);
        
        // Set expiry on first request
        if (currentCount == 0) {
            redisTemplate.expire(rateLimitKey, durationMs, TimeUnit.MILLISECONDS);
        }
        
        chain.doFilter(request, response);
    }

    /**
     * Get client identifier (IP address or authenticated user)
     */
    private String getClientIdentifier(HttpServletRequest request) {
        // Try to get from authentication first
        String playerId = (String) request.getAttribute("playerId");
        if (playerId != null) {
            return playerId;
        }
        
        // Fall back to IP address
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null) {
            ip = request.getRemoteAddr();
        }
        
        return ip;
    }
}
