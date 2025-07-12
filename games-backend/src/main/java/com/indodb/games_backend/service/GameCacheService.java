package com.indodb.games_backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Set;

/**
 * Redis-powered caching service for game data
 * Uses single Redis database for simplicity and reliability
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GameCacheService {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    
    // Cache TTL configuration
    private static final Duration PRICE_CACHE_TTL = Duration.ofMinutes(10);
    private static final Duration GAME_DETAILS_CACHE_TTL = Duration.ofHours(1);
    private static final Duration POPULAR_GAMES_CACHE_TTL = Duration.ofMinutes(30);
    
    // Cache key prefixes
    private static final String PRICE_PREFIX = "price:";
    private static final String DETAILS_PREFIX = "details:";
    
    /**
     * Cache game price with TTL
     */
    public void cachePrice(String appId, Object priceData) {
        try {
            String key = PRICE_PREFIX + appId;
            redisTemplate.opsForValue().set(key, priceData.toString(), PRICE_CACHE_TTL);
            log.debug("💾 Cached price for app {}: {}", appId, priceData);
            
            // Track popularity
            trackGamePopularity(appId);
            
        } catch (Exception e) {
            log.error("❌ Failed to cache price for app {}: {}", appId, e.getMessage());
        }
    }
    
    /**
     * Get cached price from Redis
     */
    public String getCachedPrice(String appId) {
        try {
            String key = PRICE_PREFIX + appId;
            String value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                log.debug("⚡ Cache HIT for price: {}", appId);
                trackGamePopularity(appId); // Track access
                return value;
            }
            log.debug("❌ Cache MISS for price: {}", appId);
            return null;
        } catch (Exception e) {
            log.error("❌ Failed to retrieve cached price for app {}: {}", appId, e.getMessage());
            return null;
        }
    }
    
    /**
     * Get cached price with type conversion for SteamApiService compatibility
     */
    public <T> T getCachedPrice(String appId, Class<T> valueType) {
        try {
            String value = getCachedPrice(appId);
            if (value != null) {
                if (valueType == String.class) {
                    return valueType.cast(value);
                } else if (valueType == java.math.BigDecimal.class) {
                    return valueType.cast(new java.math.BigDecimal(value));
                }
                return objectMapper.readValue(value, valueType);
            }
            return null;
        } catch (Exception e) {
            log.error("❌ Failed to convert cached price for app {}: {}", appId, e.getMessage());
            return null;
        }
    }
    
    /**
     * Cache game details with longer TTL
     */
    public void cacheGameDetails(String appId, Object gameDetails) {
        try {
            String key = DETAILS_PREFIX + appId;
            String value = objectMapper.writeValueAsString(gameDetails);
            redisTemplate.opsForValue().set(key, value, GAME_DETAILS_CACHE_TTL);
            log.debug("💾 Cached details for app {}", appId);
        } catch (JsonProcessingException e) {
            log.error("❌ Failed to cache details for app {}: {}", appId, e.getMessage());
        }
    }
    
    /**
     * Get cached game details
     */
    public <T> T getCachedGameDetails(String appId, Class<T> valueType) {
        try {
            String key = DETAILS_PREFIX + appId;
            String value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                log.debug("⚡ Cache HIT for details: {}", appId);
                return objectMapper.readValue(value, valueType);
            }
            log.debug("❌ Cache MISS for details: {}", appId);
            return null;
        } catch (JsonProcessingException e) {
            log.error("❌ Failed to deserialize cached details for app {}: {}", appId, e.getMessage());
            return null;
        }
    }
    
    /**
     * Track game popularity using sorted sets
     */
    public void trackGamePopularity(String appId) {
        try {
            String key = "popular_games";
            redisTemplate.opsForZSet().incrementScore(key, appId, 1.0);
            redisTemplate.expire(key, POPULAR_GAMES_CACHE_TTL);
            log.debug("📊 Tracked popularity for app: {}", appId);
        } catch (Exception e) {
            log.error("❌ Failed to track popularity for app {}: {}", appId, e.getMessage());
        }
    }
    
    /**
     * Alias for trackGamePopularity for SteamApiService compatibility
     */
    public void incrementGamePopularity(String appId) {
        trackGamePopularity(appId);
    }
    
    /**
     * Get most popular games
     */
    public Set<String> getMostPopularGames(int limit) {
        try {
            Set<String> result = redisTemplate.opsForZSet().reverseRange("popular_games", 0, limit - 1);
            log.debug("📊 Retrieved {} popular games", result != null ? result.size() : 0);
            return result != null ? result : Set.of();
        } catch (Exception e) {
            log.error("❌ Failed to get popular games: {}", e.getMessage());
            return Set.of();
        }
    }
    
    /**
     * Track game discounts for best deals
     */
    public void trackGameDiscount(String appId, double discountPercent) {
        try {
            if (discountPercent > 0) {
                redisTemplate.opsForZSet().add("best_deals", appId, discountPercent);
                redisTemplate.expire("best_deals", Duration.ofHours(6)); // Refresh deals every 6 hours
                log.debug("💰 Tracked {:.1f}% discount for app: {}", discountPercent, appId);
            }
        } catch (Exception e) {
            log.error("❌ Failed to track discount for app {}: {}", appId, e.getMessage());
        }
    }
    
    /**
     * Alias for trackGameDiscount for SteamApiService compatibility
     */
    public void updateBestDeal(String appId, int discountPercent) {
        trackGameDiscount(appId, (double) discountPercent);
    }
    
    /**
     * Get best deals (highest discount first)
     */
    public Set<String> getBestDeals(int limit) {
        try {
            Set<String> result = redisTemplate.opsForZSet().reverseRange("best_deals", 0, limit - 1);
            log.debug("💰 Retrieved {} best deals", result != null ? result.size() : 0);
            return result != null ? result : Set.of();
        } catch (Exception e) {
            log.error("❌ Failed to get best deals: {}", e.getMessage());
            return Set.of();
        }
    }
    
    /**
     * Get comprehensive cache statistics
     */
    public CacheStats getCacheStats() {
        try {
            // Count keys in Redis
            Set<String> priceKeys = redisTemplate.keys(PRICE_PREFIX + "*");
            Set<String> detailKeys = redisTemplate.keys(DETAILS_PREFIX + "*");
            
            Long popularGamesCount = redisTemplate.opsForZSet().count("popular_games", 
                Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
            Long bestDealsCount = redisTemplate.opsForZSet().count("best_deals", 
                Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
            
            return new CacheStats(
                priceKeys != null ? priceKeys.size() : 0,
                detailKeys != null ? detailKeys.size() : 0,
                popularGamesCount != null ? popularGamesCount.intValue() : 0,
                bestDealsCount != null ? bestDealsCount.intValue() : 0
            );
        } catch (Exception e) {
            log.error("❌ Failed to get cache stats: {}", e.getMessage());
            return new CacheStats(0, 0, 0, 0);
        }
    }
    
    /**
     * Cache statistics record
     */
    public record CacheStats(
        int cachedPrices,
        int cachedGameDetails, 
        int popularGamesTracked,
        int bestDealsTracked
    ) {}
}
