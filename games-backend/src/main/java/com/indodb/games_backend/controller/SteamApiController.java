package com.indodb.games_backend.controller;

import com.indodb.games_backend.service.ApiRateLimiterService;
import com.indodb.games_backend.service.GameCacheService;
import com.indodb.games_backend.service.SteamApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for testing Steam API integration and rate limiting
 */
@RestController
@RequestMapping("/api/steam")
@RequiredArgsConstructor
@Slf4j
public class SteamApiController {
    
    private final SteamApiService steamApiService;
    private final ApiRateLimiterService rateLimiterService;
    private final GameCacheService cacheService;
    
    /**
     * Test endpoint to fetch current price for a Steam game
     * Example: GET /api/steam/price/271590 (GTA V)
     */
    @GetMapping("/price/{appId}")
    public ResponseEntity<Map<String, Object>> getCurrentPrice(@PathVariable String appId) {
        log.info("Fetching current price for Steam app ID: {}", appId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("appId", appId);
        response.put("timestamp", System.currentTimeMillis());
        
        // Check if API is available (rate limit check)
        if (!steamApiService.isApiAvailable()) {
            response.put("status", "RATE_LIMITED");
            response.put("message", "Steam API rate limit exceeded. Please try again later.");
            return ResponseEntity.status(429).body(response);
        }
        
        try {
            BigDecimal currentPrice = steamApiService.getCurrentPrice(appId);
            
            if (currentPrice != null) {
                response.put("status", "SUCCESS");
                response.put("currentPrice", currentPrice);
                response.put("currency", "INR");
                response.put("priceFormatted", "₹" + currentPrice);
            } else {
                response.put("status", "NOT_FOUND");
                response.put("message", "Could not fetch price for this app ID");
            }
            
        } catch (Exception e) {
            log.error("Error fetching price for app ID {}: {}", appId, e.getMessage(), e);
            response.put("status", "ERROR");
            response.put("message", "Internal error while fetching price");
            return ResponseEntity.status(500).body(response);
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Test endpoint to fetch detailed game information
     * Example: GET /api/steam/details/271590 (GTA V)
     */
    @GetMapping("/details/{appId}")
    public ResponseEntity<Map<String, Object>> getGameDetails(@PathVariable String appId) {
        log.info("Fetching game details for Steam app ID: {}", appId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("appId", appId);
        response.put("timestamp", System.currentTimeMillis());
        
        // Check if API is available (rate limit check)
        if (!steamApiService.isApiAvailable()) {
            response.put("status", "RATE_LIMITED");
            response.put("message", "Steam API rate limit exceeded. Please try again later.");
            return ResponseEntity.status(429).body(response);
        }
        
        try {
            Map<String, Object> gameDetails = steamApiService.getGameDetails(appId);
            
            if (gameDetails != null) {
                response.put("status", "SUCCESS");
                response.putAll(gameDetails);
            } else {
                response.put("status", "NOT_FOUND");
                response.put("message", "Could not fetch details for this app ID");
            }
            
        } catch (Exception e) {
            log.error("Error fetching details for app ID {}: {}", appId, e.getMessage(), e);
            response.put("status", "ERROR");
            response.put("message", "Internal error while fetching game details");
            return ResponseEntity.status(500).body(response);
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get rate limiting status for Steam API
     */
    @GetMapping("/rate-limit-status")
    public ResponseEntity<Map<String, Object>> getRateLimitStatus() {
        Map<String, Object> response = steamApiService.getRateLimitStatus();
        
        // Add detailed usage stats
        ApiRateLimiterService.ApiUsageStats stats = rateLimiterService.getUsageStats("steam");
        Map<String, Object> detailedStats = new HashMap<>();
        detailedStats.put("callsThisMinute", stats.callsThisMinute);
        detailedStats.put("callsThisHour", stats.callsThisHour);
        detailedStats.put("callsThisDay", stats.callsThisDay);
        
        response.put("detailedStats", detailedStats);
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Test endpoint for common Indian games
     * This helps verify our integration with popular games in India
     */
    @GetMapping("/test-indian-games")
    public ResponseEntity<Map<String, Object>> testIndianGames() {
        log.info("Testing Steam API with popular Indian games");
        
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> games = new HashMap<>();
        
        // Popular games in India with their Steam App IDs
        Map<String, String> testGames = Map.of(
            "271590", "GTA V",
            "292030", "The Witcher 3: Wild Hunt", 
            "578080", "PUBG: BATTLEGROUNDS",
            "730", "Counter-Strike 2",
            "1091500", "Cyberpunk 2077"
        );
        
        for (Map.Entry<String, String> game : testGames.entrySet()) {
            String appId = game.getKey();
            String gameName = game.getValue();
            
            if (!steamApiService.isApiAvailable()) {
                games.put(gameName, Map.of(
                    "appId", appId,
                    "status", "RATE_LIMITED",
                    "message", "Rate limit exceeded"
                ));
                break; // Stop testing if rate limited
            }
            
            try {
                BigDecimal price = steamApiService.getCurrentPrice(appId);
                games.put(gameName, Map.of(
                    "appId", appId,
                    "price", price != null ? price : "Not available",
                    "priceFormatted", price != null ? "₹" + price : "Free/Not available",
                    "status", "SUCCESS"
                ));
                
                // Small delay between requests to be extra safe
                Thread.sleep(2000);
                
            } catch (Exception e) {
                games.put(gameName, Map.of(
                    "appId", appId,
                    "status", "ERROR",
                    "error", e.getMessage()
                ));
            }
        }
        
        response.put("games", games);
        response.put("timestamp", System.currentTimeMillis());
        response.put("rateLimitStatus", steamApiService.getRateLimitStatus());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get Redis cache statistics and performance metrics
     */
    @GetMapping("/cache-stats")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            GameCacheService.CacheStats stats = cacheService.getCacheStats();
            
            response.put("status", "SUCCESS");
            response.put("cacheStats", Map.of(
                "cachedPrices", stats.cachedPrices(),
                "cachedGameDetails", stats.cachedGameDetails(),
                "popularGamesTracked", stats.popularGamesTracked(),
                "bestDealsTracked", stats.bestDealsTracked()
            ));
            response.put("performance", Map.of(
                "cacheHitRatio", "Check logs for ⚡ Cache HIT vs ❌ Cache MISS",
                "avgResponseTime", "Sub-millisecond for cached results"
            ));
            response.put("timestamp", System.currentTimeMillis());
            
        } catch (Exception e) {
            log.error("Error getting cache stats: {}", e.getMessage(), e);
            response.put("status", "ERROR");
            response.put("message", "Failed to retrieve cache statistics");
            return ResponseEntity.status(500).body(response);
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get most popular games (tracked via Redis sorted sets)
     */
    @GetMapping("/popular-games")
    public ResponseEntity<Map<String, Object>> getPopularGames(@RequestParam(defaultValue = "10") int limit) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            java.util.Set<String> popularGames = cacheService.getMostPopularGames(limit);
            
            response.put("status", "SUCCESS");
            response.put("popularGames", popularGames);
            response.put("count", popularGames.size());
            response.put("description", "Games ranked by API request frequency");
            response.put("timestamp", System.currentTimeMillis());
            
        } catch (Exception e) {
            log.error("Error getting popular games: {}", e.getMessage(), e);
            response.put("status", "ERROR");
            response.put("message", "Failed to retrieve popular games");
            return ResponseEntity.status(500).body(response);
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get best deals (games with highest discounts)
     */
    @GetMapping("/best-deals")
    public ResponseEntity<Map<String, Object>> getBestDeals(@RequestParam(defaultValue = "10") int limit) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            java.util.Set<String> bestDeals = cacheService.getBestDeals(limit);
            
            response.put("status", "SUCCESS");
            response.put("bestDeals", bestDeals);
            response.put("count", bestDeals.size());
            response.put("description", "Games ranked by discount percentage (highest first)");
            response.put("timestamp", System.currentTimeMillis());
            
        } catch (Exception e) {
            log.error("Error getting best deals: {}", e.getMessage(), e);
            response.put("status", "ERROR");
            response.put("message", "Failed to retrieve best deals");
            return ResponseEntity.status(500).body(response);
        }
        
        return ResponseEntity.ok(response);
    }
}
