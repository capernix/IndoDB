package com.indodb.games_backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * BULLETPROOF Rate Limiting Service
 * Prevents API bans by strictly controlling request rates
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ApiRateLimiterService {
    
    private final RedisTemplate<String, String> redisTemplate;
    
    @Value("${api.rate-limit.steam.capacity:20}")
    private int steamCapacity;

    @Value("${api.rate-limit.steam.refill-per-second:0.67}")
    private double steamRefillPerSecond;

    @Value("${api.rate-limit.other.capacity:8}")
    private int otherCapacity;

    @Value("${api.rate-limit.other.refill-per-second:0.2}")
    private double otherRefillPerSecond;
    
    /**
     * Check if we can make a Steam API call
     * Ultra conservative: 0.2 calls/second with 5-second minimum gap
     * @return true if allowed, false if rate limited
     */
    public boolean canMakeSteamApiCall() {
        return tryConsumeToken("steam", steamCapacity, steamRefillPerSecond);
    }
    
    /**
     * Check if we can make other API calls (Epic, GOG, etc.)
     */
    public boolean canMakeOtherApiCall(String apiName) {
        return tryConsumeToken(apiName.toLowerCase(), otherCapacity, otherRefillPerSecond);
    }
    
    /**
     * Record that we made an API call (increments counters)
     */
    public void recordApiCall(String apiName) {
        try {
            String key = apiName.toLowerCase();
            String usageMinuteKey = "api_usage:" + key + ":minute:" + (System.currentTimeMillis() / 60000);
            String usageHourKey = "api_usage:" + key + ":hour:" + (System.currentTimeMillis() / 3600000);
            String usageDayKey = "api_usage:" + key + ":day:" + (System.currentTimeMillis() / 86400000);

            redisTemplate.opsForValue().increment(usageMinuteKey);
            redisTemplate.expire(usageMinuteKey, Duration.ofMinutes(2));
            redisTemplate.opsForValue().increment(usageHourKey);
            redisTemplate.expire(usageHourKey, Duration.ofHours(2));
            redisTemplate.opsForValue().increment(usageDayKey);
            redisTemplate.expire(usageDayKey, Duration.ofDays(2));
        } catch (Exception e) {
            log.error("❌ Failed to record API call for {}: {}", apiName, e.getMessage());
        }
    }
    
    /**
     * Get current usage stats for monitoring
     */
    public ApiUsageStats getUsageStats(String apiName) {
        try {
            String baseKey = "api_usage:" + apiName.toLowerCase();
            
            long currentMinute = System.currentTimeMillis() / 60000;
            long currentHour = System.currentTimeMillis() / 3600000;
            long currentDay = System.currentTimeMillis() / 86400000;
            
            String minuteKey = baseKey + ":minute:" + currentMinute;
            String hourKey = baseKey + ":hour:" + currentHour;
            String dayKey = baseKey + ":day:" + currentDay;
            
            int minuteCount = getCount(minuteKey);
            int hourCount = getCount(hourKey);
            int dayCount = getCount(dayKey);
            
            return new ApiUsageStats(apiName, minuteCount, hourCount, dayCount);
            
        } catch (Exception e) {
            log.error("❌ Failed to get usage stats for {}: {}", apiName, e.getMessage());
            return new ApiUsageStats(apiName, 0, 0, 0);
        }
    }

    public Map<String, Object> getTokenBucketStatus(String apiName) {
        try {
            String key = apiName.toLowerCase();
            int capacity = "steam".equals(key) ? steamCapacity : otherCapacity;
            double refill = "steam".equals(key) ? steamRefillPerSecond : otherRefillPerSecond;
            TokenState state = getAndRefillBucket(key, capacity, refill);

            Map<String, Object> status = new HashMap<>();
            status.put("api", key);
            status.put("capacity", capacity);
            status.put("refillPerSecond", refill);
            status.put("tokensRemaining", state.tokens);
            status.put("updatedAt", state.lastRefillMs);
            return status;
        } catch (Exception e) {
            log.error("❌ Failed token bucket status lookup for {}: {}", apiName, e.getMessage());
            return Map.of("api", apiName.toLowerCase(), "error", e.getMessage());
        }
    }

    private boolean tryConsumeToken(String apiName, int capacity, double refillPerSecond) {
        try {
            TokenState state = getAndRefillBucket(apiName, capacity, refillPerSecond);
            if (state.tokens < 1.0) {
                log.warn("🚫 Token bucket empty for {} (tokens={})", apiName, state.tokens);
                return false;
            }

            state.tokens = state.tokens - 1.0;
            saveTokenState(apiName, state);
            return true;
        } catch (Exception e) {
            log.error("❌ Error consuming token for {}: {}", apiName, e.getMessage());
            return false;
        }
    }

    private TokenState getAndRefillBucket(String apiName, int capacity, double refillPerSecond) {
        String tokenKey = "api_bucket:" + apiName + ":tokens";
        String refillKey = "api_bucket:" + apiName + ":last_refill_ms";

        long now = System.currentTimeMillis();
        double tokens = parseDouble(redisTemplate.opsForValue().get(tokenKey), capacity);
        long lastRefillMs = parseLong(redisTemplate.opsForValue().get(refillKey), now);

        long elapsedMs = Math.max(0, now - lastRefillMs);
        double refillTokens = (elapsedMs / 1000.0) * refillPerSecond;
        double newTokens = Math.min(capacity, tokens + refillTokens);

        TokenState state = new TokenState();
        state.tokens = newTokens;
        state.lastRefillMs = now;
        saveTokenState(apiName, state);
        return state;
    }

    private void saveTokenState(String apiName, TokenState state) {
        String tokenKey = "api_bucket:" + apiName + ":tokens";
        String refillKey = "api_bucket:" + apiName + ":last_refill_ms";

        redisTemplate.opsForValue().set(tokenKey, Double.toString(state.tokens), Duration.ofHours(1));
        redisTemplate.opsForValue().set(refillKey, Long.toString(state.lastRefillMs), Duration.ofHours(1));
    }

    private long parseLong(String value, long fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private double parseDouble(String value, double fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
    
    private int getCount(String key) {
        try {
            String value = redisTemplate.opsForValue().get(key);
            return value != null ? Integer.parseInt(value) : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private static class TokenState {
        private double tokens;
        private long lastRefillMs;
    }
    
    /**
     * Usage statistics data class
     */
    public static class ApiUsageStats {
        public final String apiName;
        public final int callsThisMinute;
        public final int callsThisHour;
        public final int callsThisDay;
        
        public ApiUsageStats(String apiName, int callsThisMinute, int callsThisHour, int callsThisDay) {
            this.apiName = apiName;
            this.callsThisMinute = callsThisMinute;
            this.callsThisHour = callsThisHour;
            this.callsThisDay = callsThisDay;
        }
        
        @Override
        public String toString() {
            return String.format("%s: %d/min, %d/hr, %d/day", apiName, callsThisMinute, callsThisHour, callsThisDay);
        }
    }
}
