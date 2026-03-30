package com.example.distributed_rate_limiter;


import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class FixedWindowRateLimiter {

    private final StringRedisTemplate redisTemplate;

    @Value("${ratelimiter.limit}")
    private int limit;

    @Value("${ratelimiter.window-seconds}")
    private int windowSeconds;

    public FixedWindowRateLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isAllowed(String clientId) {
        // Key resets every window — e.g. "rl:user123:1710000060"
        long windowStart = System.currentTimeMillis() / 1000 / windowSeconds;
        String key = "rl:" + clientId + ":" + windowStart;

        Long count = redisTemplate.opsForValue().increment(key);
        if (count == 1) {
            // First request in this window — set expiry
            redisTemplate.expire(key, windowSeconds, TimeUnit.SECONDS);
        }
        return count <= limit;
    }
}