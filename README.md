# Distributed-Rate-Limiter-Using-Spring-Boot
# Distributed Rate Limiter with Spring Boot and Redis

A simple distributed rate limiter built using Spring Boot and Redis.  
This project demonstrates how to limit client requests across multiple application instances using Redis as a centralized store.

## Features

- Distributed rate limiting using Redis
- Filter-based request interception
- Client identification using:
  - `X-API-Key` header
  - `X-Forwarded-For`
  - Remote IP address
- Returns `429 Too Many Requests` when the rate limit is exceeded
- Simple test endpoint for verification
- Redis can be run locally using Docker

## Tech Stack

- Java
- Spring Boot
- Spring Web
- Spring Data Redis
- Redis
- Docker

## Project Structure

```text
src/main/java/com/example/distributed_rate_limiter
├── RateLimitFilter.java
├── SlidingWindowRateLimiter.java
└── TestController.java
