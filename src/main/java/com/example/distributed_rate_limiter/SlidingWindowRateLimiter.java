package com.example.distributed_rate_limiter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SlidingWindowRateLimiter {

    private final StringRedisTemplate redisTemplate;

    @Value("${ratelimiter.limit}")
    private int limit;

    @Value("${ratelimiter.window-seconds}")
    private long windowSeconds;

    public SlidingWindowRateLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isAllowed(String clientId) {
        long now = System.currentTimeMillis();
        long windowStart = now - (windowSeconds * 1000);
        String key = "rl:sliding:" + clientId;

        // Lua script ensures atomicity — no race conditions
        String luaScript = """
            local key = KEYS[1]
            local now = tonumber(ARGV[1])
            local windowStart = tonumber(ARGV[2])
            local limit = tonumber(ARGV[3])
            local uniqueMember = ARGV[4]
            
            -- Remove entries outside the window
            redis.call('ZREMRANGEBYSCORE', key, 0, windowStart)
            
            -- Count current entries
            local count = redis.call('ZCARD', key)
            
            if count < limit then
                -- Add this request
                redis.call('ZADD', key, now, uniqueMember)
                redis.call('EXPIRE', key, tonumber(ARGV[5]))
                return 1
            else
                return 0
            end
            """;

        String uniqueMember = now + "-" + UUID.randomUUID();

        Long result = redisTemplate.execute(
                new DefaultRedisScript<>(luaScript, Long.class),
                List.of(key),
                String.valueOf(now),
                String.valueOf(windowStart),
                String.valueOf(limit),
                uniqueMember,
                String.valueOf(windowSeconds + 1)
        );

        return Long.valueOf(1).equals(result);
    }
}