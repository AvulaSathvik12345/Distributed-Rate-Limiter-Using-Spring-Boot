package com.example.distributed_rate_limiter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final SlidingWindowRateLimiter rateLimiter;

    public RateLimitFilter(SlidingWindowRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String clientId = resolveClientId(request);

        if (!rateLimiter.isAllowed(clientId)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value()); // 429
            response.setContentType("application/json");
            response.getWriter().write("""
                {"error": "Too many requests", "retryAfter": 60}
                """);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String resolveClientId(HttpServletRequest request) {
        // Option 1: by API key header
        String apiKey = request.getHeader("X-API-Key");
        if (apiKey != null) return apiKey;

        // Option 2: by IP (handles proxies via X-Forwarded-For)
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null) return forwarded.split(",")[0].trim();

        return request.getRemoteAddr();
    }
}