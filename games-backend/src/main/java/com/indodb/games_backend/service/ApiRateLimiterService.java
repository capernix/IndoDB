package com.indodb.games_backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * BULLETPROOF Rate Limiting Service
 * Prevents API bans by strictly controlling request rates
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ApiRateLimiterService {
    
    private final RedisTemplate<String, String> redisTemplate;
    
    // CONSERVATIVE Steam API Limits (much lower than actual to be safe)
    private static final int STEAM_CALLS_PER_MINUTE = 10;  // Steam allows ~200, we use 10
    private static final int STEAM_CALLS_PER_HOUR = 200;   // Steam allows ~10000, we use 200
    private static final int STEAM_CALLS_PER_DAY = 2000;   // Steam allows ~100000, we use 2000
    
    // Epic/Other API limits (very conservative)
    private static final int OTHER_CALLS_PER_MINUTE = 5;
    private static final int OTHER_CALLS_PER_HOUR = 100;
    
    // Minimum interval between Steam API calls (5 seconds = 0.2 calls/second)
    private static final long MIN_CALL_INTERVAL_MS = 5000;
    
    /**
     * Check if we can make a Steam API call
     * Ultra conservative: 0.2 calls/second with 5-second minimum gap
     * @return true if allowed, false if rate limited
     */
    public boolean canMakeSteamApiCall() {
        // First check the 5-second minimum interval
        if (!checkMinimumInterval("steam")) {
            return false;
        }
        
        // Then check the overall rate limits
        return checkRateLimit("steam", STEAM_CALLS_PER_MINUTE, STEAM_CALLS_PER_HOUR, STEAM_CALLS_PER_DAY);
    }
    
    /**
     * Check if we can make other API calls (Epic, GOG, etc.)
     */
    public boolean canMakeOtherApiCall(String apiName) {
        return checkRateLimit(apiName.toLowerCase(), OTHER_CALLS_PER_MINUTE, OTHER_CALLS_PER_HOUR, 1000);
    }
    
    /**
     * Record that we made an API call (increments counters)
     */
    public void recordApiCall(String apiName) {
        try {
            String baseKey = "api_limit:" + apiName.toLowerCase();
            
            // Increment minute counter
            String minuteKey = baseKey + ":minute:" + (System.currentTimeMillis() / 60000);
            redisTemplate.opsForValue().increment(minuteKey);
            redisTemplate.expire(minuteKey, Duration.ofMinutes(2));
            
            // Increment hour counter  
            String hourKey = baseKey + ":hour:" + (System.currentTimeMillis() / 3600000);
            redisTemplate.opsForValue().increment(hourKey);
            redisTemplate.expire(hourKey, Duration.ofHours(2));
            
            // Increment day counter
            String dayKey = baseKey + ":day:" + (System.currentTimeMillis() / 86400000);
            redisTemplate.opsForValue().increment(dayKey);
            redisTemplate.expire(dayKey, Duration.ofDays(2));
            
            // Record the call time for minimum interval checking
            String lastCallKey = "api_last_call:" + apiName.toLowerCase();
            redisTemplate.opsForValue().set(lastCallKey, String.valueOf(System.currentTimeMillis()), Duration.ofSeconds(10));
            
            log.debug("🔢 Recorded API call for {}", apiName);
            
        } catch (Exception e) {
            log.error("❌ Failed to record API call for {}: {}", apiName, e.getMessage());
        }
    }
    
    /**
     * Get current usage stats for monitoring
     */
    public ApiUsageStats getUsageStats(String apiName) {
        try {
            String baseKey = "api_limit:" + apiName.toLowerCase();
            
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
    
    private boolean checkRateLimit(String apiName, int perMinute, int perHour, int perDay) {
        try {
            String baseKey = "api_limit:" + apiName;
            
            long currentMinute = System.currentTimeMillis() / 60000;
            long currentHour = System.currentTimeMillis() / 3600000;
            long currentDay = System.currentTimeMillis() / 86400000;
            
            String minuteKey = baseKey + ":minute:" + currentMinute;
            String hourKey = baseKey + ":hour:" + currentHour;
            String dayKey = baseKey + ":day:" + currentDay;
            
            int minuteCount = getCount(minuteKey);
            int hourCount = getCount(hourKey);
            int dayCount = getCount(dayKey);
            
            boolean allowed = minuteCount < perMinute && hourCount < perHour && dayCount < perDay;
            
            if (!allowed) {
                log.warn("🚫 Rate limit exceeded for {}: {}min/{}hr/{}day (limits: {}/{}/{})", 
                        apiName, minuteCount, hourCount, dayCount, perMinute, perHour, perDay);
            }
            
            return allowed;
            
        } catch (Exception e) {
            log.error("❌ Error checking rate limit for {}: {}", apiName, e.getMessage());
            return false; // Fail safe - don't allow calls if we can't check limits
        }
    }
    
    /**
     * Check minimum interval between calls (5 seconds for Steam API)
     * This enforces 0.2 calls per second maximum
     */
    private boolean checkMinimumInterval(String apiName) {
        try {
            String lastCallKey = "api_last_call:" + apiName.toLowerCase();
            String lastCallStr = redisTemplate.opsForValue().get(lastCallKey);
            
            if (lastCallStr != null) {
                long lastCallTime = Long.parseLong(lastCallStr);
                long timeSinceLastCall = System.currentTimeMillis() - lastCallTime;
                
                if (timeSinceLastCall < MIN_CALL_INTERVAL_MS) {
                    log.warn("🕐 Minimum interval not met for {}: {}ms < {}ms required", 
                            apiName, timeSinceLastCall, MIN_CALL_INTERVAL_MS);
                    return false;
                }
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("❌ Error checking minimum interval for {}: {}", apiName, e.getMessage());
            return false; // Fail safe
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
