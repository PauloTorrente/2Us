package com.coupleapp.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
// import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

/**
 * Rate Limiter Service using Redis
 * TEMPORARIAMENTE DESABILITADO - PRECISA REDIS CONFIGURADO
 */
@Slf4j
//@Service  // COMENTADO - Descomente quando Redis estiver configurado
public class RateLimiterService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${app.cache.ttl-hours:24}")
    private int cacheTtlHours;

    public RateLimiterService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Check if rate limit has been exceeded for a given key
     */
    public boolean isRateLimitExceeded(String key, int maxAttempts, int windowMinutes) {
        try {
            String rateLimitKey = "rate_limit:" + key;
            Long attempts = redisTemplate.opsForValue().increment(rateLimitKey);
            
            if (attempts == null) {
                return false;
            }

            if (attempts == 1) {
                redisTemplate.expire(rateLimitKey, Duration.ofMinutes(windowMinutes));
            }

            boolean exceeded = attempts > maxAttempts;
            if (exceeded) {
                log.warn("Rate limit exceeded for key: {}, attempts: {}, max: {}", 
                    key, attempts, maxAttempts);
            }

            return exceeded;

        } catch (Exception e) {
            log.error("Error checking rate limit for key: {}", key, e);
            return false;
        }
    }

    /**
     * Reset rate limit for a given key
     */
    public void resetRateLimit(String key) {
        try {
            String rateLimitKey = "rate_limit:" + key;
            redisTemplate.delete(rateLimitKey);
            log.info("Reset rate limit for key: {}", key);
        } catch (Exception e) {
            log.error("Error resetting rate limit for key: {}", key, e);
        }
    }

    /**
     * Get remaining attempts for a given key
     */
    public int getRemainingAttempts(String key, int maxAttempts) {
        try {
            String rateLimitKey = "rate_limit:" + key;
            Object attemptsObj = redisTemplate.opsForValue().get(rateLimitKey);
            
            if (attemptsObj == null) {
                return maxAttempts;
            }

            int attempts = Integer.parseInt(attemptsObj.toString());
            return Math.max(0, maxAttempts - attempts);

        } catch (Exception e) {
            log.error("Error getting remaining attempts for key: {}", key, e);
            return maxAttempts;
        }
    }

    /**
     * Cache a value with TTL
     */
    public void cacheValue(String key, Object value, int ttlMinutes) {
        try {
            redisTemplate.opsForValue().set(key, value, Duration.ofMinutes(ttlMinutes));
            log.debug("Cached value for key: {} with TTL: {} minutes", key, ttlMinutes);
        } catch (Exception e) {
            log.error("Error caching value for key: {}", key, e);
        }
    }

    /**
     * Get cached value
     */
    public Object getCachedValue(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Error getting cached value for key: {}", key, e);
            return null;
        }
    }

    /**
     * Delete cached value
     */
    public void deleteCachedValue(String key) {
        try {
            redisTemplate.delete(key);
            log.debug("Deleted cached value for key: {}", key);
        } catch (Exception e) {
            log.error("Error deleting cached value for key: {}", key, e);
        }
    }
}
