package com.indodb.games_backend.controller;

import com.indodb.games_backend.service.ApiRateLimiterService;
import com.indodb.games_backend.service.GameDiscoveryProducer;
import com.indodb.games_backend.service.GameService;
import com.indodb.games_backend.service.GameCacheService;
import com.indodb.games_backend.service.ItadApiService;
import com.indodb.games_backend.service.ItadCatalogSyncService;
import com.indodb.games_backend.service.SteamCatalogSyncService;
import com.indodb.games_backend.service.SteamApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Controller for Steam API integration with comprehensive rate limiting and caching
 */
@RestController
@RequestMapping("/api/steam")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "Steam Integration", description = "🎮 Steam Store API integration with Indian pricing and intelligent caching")
public class SteamApiController {
    
    private final SteamApiService steamApiService;
    private final SteamCatalogSyncService steamCatalogSyncService;
    private final GameDiscoveryProducer gameDiscoveryProducer;
    private final GameService gameService;
    private final ItadApiService itadApiService;
    private final ItadCatalogSyncService itadCatalogSyncService;
    private final ApiRateLimiterService rateLimiterService;
    private final GameCacheService cacheService;
    
    /**
     * Get current Indian price for a Steam game
     * 
     * Fetches real-time pricing from Steam Store API with intelligent caching.
     * Prices are displayed in Indian Rupees (INR) and cached for optimal performance.
     * 
     * @param appId Steam application ID (e.g., 271590 for GTA V, 292030 for Witcher 3)
     * @return Price information with current INR pricing and status
     */
    @Operation(
        summary = "Get Steam game price in INR",
        description = """
            **Fetch real-time Steam game pricing for Indian market**
            
            This endpoint provides current Indian pricing (INR) for Steam games with:
            - ⚡ **Sub-20ms response** for cached prices
            - 🛡️ **Rate limiting protection** to prevent API bans  
            - 🇮🇳 **Indian market focus** with regional pricing
            - 📊 **Intelligent caching** for optimal performance
            
            **Popular Game IDs:**
            - `271590` - Grand Theft Auto V (₹0 - Free)
            - `292030` - The Witcher 3: Wild Hunt (₹1699)
            - `1091500` - Cyberpunk 2077 (₹2999)
            - `730` - Counter-Strike 2 (₹0 - Free)
            - `578080` - PUBG: BATTLEGROUNDS (₹0 - Free)
            
            **Response includes:**
            - Current price in INR
            - Formatted price string (₹1699)
            - Cache status and timestamp
            - Rate limiting information
            """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "✅ Price successfully retrieved"),
        @ApiResponse(responseCode = "429", description = "⚠️ Rate limit exceeded - try again later"),
        @ApiResponse(responseCode = "404", description = "❌ Game not found or unavailable"),
        @ApiResponse(responseCode = "500", description = "🔥 Server error - check logs")
    })
    @GetMapping("/price/{appId}")
    public ResponseEntity<Map<String, Object>> getCurrentPrice(
        @Parameter(
            description = "Steam Application ID (numeric string)", 
            example = "292030",
            required = true
        )
        @PathVariable String appId
    ) {
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
     * Fetch a Steam app from the live Steam Store API and persist it into IndoDB.
     * This is the bridge from "external API response" to the normal game/deal APIs.
     */
    @PostMapping("/sync/{appId}")
    public ResponseEntity<?> syncSteamGame(@PathVariable String appId) {
        log.info("Syncing Steam app ID into IndoDB: {}", appId);

        if (!steamApiService.isApiAvailable()) {
            return ResponseEntity.status(429).body(Map.of(
                    "status", "RATE_LIMITED",
                    "message", "Steam API rate limit exceeded. Please try again later.",
                    "appId", appId
            ));
        }

        try {
            SteamCatalogSyncService.SyncResult result = steamCatalogSyncService.syncSteamGame(appId);
            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "game", result
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of(
                    "status", "NOT_FOUND",
                    "message", e.getMessage(),
                    "appId", appId
            ));
        } catch (Exception e) {
            log.error("Error syncing Steam app ID {}: {}", appId, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "status", "ERROR",
                    "message", "Internal error while syncing Steam game",
                    "appId", appId
            ));
        }
    }

    @PostMapping("/sync/queue/{appId}")
    @Operation(summary = "Queue async sync for a Steam app", description = "Pushes a game-discovery event for background Steam/ITAD ingestion")
    public ResponseEntity<Map<String, Object>> queueSteamGameSync(
            @PathVariable String appId,
            @RequestParam(defaultValue = "USER_IMPORT") String source,
            @RequestParam(defaultValue = "HIGH") String priority,
            @RequestParam(defaultValue = "true") boolean includeItad
    ) {
        gameDiscoveryProducer.publish(appId, source, priority, includeItad);
        return ResponseEntity.accepted().body(Map.of(
                "status", "QUEUED",
                "appId", appId,
                "source", source,
                "priority", priority,
                "includeItad", includeItad
        ));
    }

    @PostMapping("/sync/queue/batch")
    @Operation(summary = "Queue async sync for multiple Steam apps", description = "Bulk enqueue game-discovery events for baseline seeding")
    public ResponseEntity<Map<String, Object>> queueBatchSteamGameSync(
            @RequestBody List<String> appIds,
            @RequestParam(defaultValue = "BASELINE_SEED") String source,
            @RequestParam(defaultValue = "MEDIUM") String priority,
            @RequestParam(defaultValue = "true") boolean includeItad
    ) {
        List<String> accepted = new ArrayList<>();
        for (String appId : appIds) {
            if (appId != null && appId.matches("\\d+")) {
                gameDiscoveryProducer.publish(appId, source, priority, includeItad);
                accepted.add(appId);
            }
        }

        return ResponseEntity.accepted().body(Map.of(
                "status", "QUEUED",
                "acceptedCount", accepted.size(),
                "acceptedAppIds", accepted,
                "source", source,
                "priority", priority,
                "includeItad", includeItad
        ));
    }

    @PostMapping("/sync-visible")
    @Operation(
            summary = "Sync visible homepage Steam games",
            description = "Collects app IDs from trending/deals/hottest lists and syncs them in one call"
    )
    public ResponseEntity<Map<String, Object>> syncVisibleHomepageGames(
            @RequestParam(defaultValue = "8") int limit,
            @RequestParam(defaultValue = "true") boolean includeItad,
            @RequestParam(defaultValue = "false") boolean queueOnly
    ) {
        int safeLimit = Math.max(1, Math.min(limit, 20));

        Set<String> appIds = new LinkedHashSet<>();
        gameService.getTrendingGames("trending", safeLimit).forEach(game -> {
            if (game.getSteamAppId() != null) {
                appIds.add(game.getSteamAppId().toString());
            }
        });
        gameService.getTrendingGames("deals", safeLimit).forEach(game -> {
            if (game.getSteamAppId() != null) {
                appIds.add(game.getSteamAppId().toString());
            }
        });
        gameService.getTrendingGames("hottest", safeLimit).forEach(game -> {
            if (game.getSteamAppId() != null) {
                appIds.add(game.getSteamAppId().toString());
            }
        });

        List<String> synced = new ArrayList<>();
        List<String> queued = new ArrayList<>();
        List<String> failed = new ArrayList<>();

        for (String appId : appIds) {
            try {
                if (queueOnly) {
                    gameDiscoveryProducer.publish(appId, "VISIBLE_BATCH", "HIGH", includeItad);
                    queued.add(appId);
                    continue;
                }

                steamCatalogSyncService.syncSteamGame(appId);
                synced.add(appId);

                if (includeItad && itadApiService.isConfigured()) {
                    try {
                        itadCatalogSyncService.syncStorePricesForSteamApp(appId);
                    } catch (Exception ignored) {
                        // Ignore per-app ITAD failures; steam sync already succeeded.
                    }
                }
            } catch (Exception e) {
                failed.add(appId);
                log.warn("Failed syncing visible appId={}: {}", appId, e.getMessage());
            }
        }

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "mode", queueOnly ? "QUEUED" : "SYNCED",
                "limit", safeLimit,
                "includeItad", includeItad,
                "totalCandidates", appIds.size(),
                "candidateAppIds", appIds,
                "syncedAppIds", synced,
                "queuedAppIds", queued,
                "failedAppIds", failed
        ));
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
        response.put("tokenBucket", rateLimiterService.getTokenBucketStatus("steam"));
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
