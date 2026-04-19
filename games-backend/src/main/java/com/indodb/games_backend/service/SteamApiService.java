package com.indodb.games_backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Steam API Service with comprehensive rate limiting and error handling.
 * 
 * Rate Limiting Strategy:
 * - Steam Web API: 100,000 calls per day (≈1.16 calls/second)
 * - Conservative limit: 1 call per 2 seconds (0.5 calls/second)
 * - Burst protection: max 5 calls per 10 seconds
 * 
 * This ensures we stay well under Steam's limits and avoid getting banned.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SteamApiService {
    
    private final ApiRateLimiterService rateLimiter;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final GameCacheService cacheService;
    
    @Value("${steam.api.key:}")
    private String steamApiKey;
    
    @Value("${steam.api.base-url:https://store.steampowered.com/api}")
    private String steamApiBaseUrl;
    
    /**
     * Fetches current price for a Steam game with strict rate limiting and intelligent caching.
     * 
     * @param appId Steam application ID (e.g., 271590 for GTA V)
     * @return Current price in INR, or null if unavailable
     */
    public BigDecimal getCurrentPrice(String appId) {
        log.info("Fetching Steam price for app ID: {}", appId);
        
        // 🚀 STEP 1: Try cache first (sub-millisecond response)
        BigDecimal cachedPrice = cacheService.getCachedPrice(appId, BigDecimal.class);
        if (cachedPrice != null) {
            log.info("⚡ Returning cached price for app ID {}: ₹{}", appId, cachedPrice);
            cacheService.incrementGamePopularity(appId); // Track popularity
            return cachedPrice;
        }
        
        // 🛡️ STEP 2: Check rate limit before external API call
        if (!rateLimiter.canMakeSteamApiCall()) {
            log.warn("Rate limit exceeded for Steam API. Skipping request for app ID: {}", appId);
            return null;
        }
        
        try {
            // 🌐 STEP 3: Make external API call
            String url = steamApiBaseUrl + "/appdetails?appids=" + appId + "&cc=in&l=english&filters=price_overview";
            
            log.debug("Making Steam API request to: {}", url);
            String response = restTemplate.getForObject(url, String.class);
            
            // Record the API call after successful request
            rateLimiter.recordApiCall("steam");
            
            if (response == null || response.trim().isEmpty()) {
                log.warn("Empty response from Steam API for app ID: {}", appId);
                return null;
            }
            
            // 📊 STEP 4: Parse price and cache result
            BigDecimal currentPrice = parsePrice(response, appId);
            
            if (currentPrice != null) {
                // 📦 Cache the result for future requests
                cacheService.cachePrice(appId, currentPrice);
                cacheService.incrementGamePopularity(appId);
                
                // 💰 Track best deals if there's a discount
                trackBestDeals(response, appId, currentPrice);
            }
            
            return currentPrice;
            
        } catch (HttpClientErrorException e) {
            log.error("HTTP error calling Steam API for app ID {}: {} - {}", 
                appId, e.getStatusCode(), e.getMessage());
            
            // If we get 429 (Too Many Requests), we need to back off even more
            if (e.getStatusCode().value() == 429) {
                log.error("RATE LIMIT HIT! Steam API returned 429. We need to slow down!");
                // Log the rate limit hit for monitoring (the existing service handles backoff)
            }
            return null;
            
        } catch (ResourceAccessException e) {
            log.error("Network error calling Steam API for app ID {}: {}", appId, e.getMessage());
            return null;
            
        } catch (Exception e) {
            log.error("Unexpected error calling Steam API for app ID {}: {}", appId, e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Parses Steam API response to extract price information.
     */
    private BigDecimal parsePrice(String response, String appId) {
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode appNode = rootNode.get(appId);
            
            if (appNode == null) {
                log.warn("No data found for app ID: {}", appId);
                return null;
            }
            
            JsonNode dataNode = appNode.get("data");
            if (dataNode == null || !appNode.get("success").asBoolean()) {
                log.warn("Steam API returned unsuccessful response for app ID: {}", appId);
                return null;
            }
            
            JsonNode priceOverview = dataNode.get("price_overview");
            if (priceOverview == null) {
                log.info("No price information available for app ID: {} (might be free)", appId);
                return BigDecimal.ZERO; // Free game
            }
            
            // Steam returns price in currency's smallest unit (paise for INR)
            int finalPrice = priceOverview.get("final").asInt();
            BigDecimal priceInINR = new BigDecimal(finalPrice).divide(new BigDecimal("100"));
            
            log.info("Successfully fetched price for app ID {}: ₹{}", appId, priceInINR);
            return priceInINR;
            
        } catch (Exception e) {
            log.error("Error parsing Steam API response for app ID {}: {}", appId, e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Track best deals if the game has a discount
     */
    private void trackBestDeals(String response, String appId, BigDecimal currentPrice) {
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode appNode = rootNode.get(appId);
            
            if (appNode != null) {
                JsonNode dataNode = appNode.get("data");
                if (dataNode != null) {
                    JsonNode priceOverview = dataNode.get("price_overview");
                    if (priceOverview != null && priceOverview.has("discount_percent")) {
                        int discountPercent = priceOverview.get("discount_percent").asInt();
                        if (discountPercent > 0) {
                            cacheService.updateBestDeal(appId, discountPercent);
                            log.info("💰 Tracked best deal for app ID {}: {}% off", appId, discountPercent);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error tracking best deals for app ID {}: {}", appId, e.getMessage());
        }
    }
    
    /**
     * Gets detailed game information including name and price.
     * Uses the same rate limiting as price fetching.
     */
    public Map<String, Object> getGameDetails(String appId) {
        log.info("Fetching Steam game details for app ID: {}", appId);
        
        // SAFETY FIRST: Check rate limit
        if (!rateLimiter.canMakeSteamApiCall()) {
            log.warn("Rate limit exceeded for Steam API. Skipping game details request for app ID: {}", appId);
            return null;
        }
        
        try {
            String url = steamApiBaseUrl + "/appdetails?appids=" + appId + "&cc=in&l=english";
            String response = restTemplate.getForObject(url, String.class);
            
            // Record the API call after successful request
            rateLimiter.recordApiCall("steam");
            
            if (response == null) {
                return null;
            }
            
            return parseGameDetails(response, appId);
            
        } catch (Exception e) {
            log.error("Error fetching game details for app ID {}: {}", appId, e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Parses detailed game information from Steam API response.
     */
    private Map<String, Object> parseGameDetails(String response, String appId) {
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode appNode = rootNode.get(appId);
            
            if (appNode == null || !appNode.get("success").asBoolean()) {
                return null;
            }
            
            JsonNode dataNode = appNode.get("data");
            Map<String, Object> gameDetails = new HashMap<>();
            
            gameDetails.put("appId", appId);
            gameDetails.put("name", dataNode.get("name").asText());
            gameDetails.put("type", dataNode.get("type").asText());
            putIfPresent(gameDetails, "shortDescription", dataNode, "short_description");
            putIfPresent(gameDetails, "description", dataNode, "detailed_description");
            putIfPresent(gameDetails, "headerImageUrl", dataNode, "header_image");
            putIfPresent(gameDetails, "metacriticScore", dataNode.path("metacritic"), "score");
            if (dataNode.has("is_free")) {
                gameDetails.put("isFree", dataNode.get("is_free").asBoolean());
            }
            if (dataNode.has("release_date") && dataNode.get("release_date").has("date")) {
                gameDetails.put("releaseDate", dataNode.get("release_date").get("date").asText());
            }
            if (dataNode.has("genres") && dataNode.get("genres").isArray()) {
                gameDetails.put("genres", streamDescriptions(dataNode.get("genres")));
            }
            if (dataNode.has("categories") && dataNode.get("categories").isArray()) {
                gameDetails.put("tags", streamDescriptions(dataNode.get("categories")));
            }
            
            // Price information
            JsonNode priceOverview = dataNode.get("price_overview");
            if (priceOverview != null) {
                gameDetails.put("priceAvailable", true);
                int finalPrice = priceOverview.get("final").asInt();
                BigDecimal priceInINR = new BigDecimal(finalPrice).divide(new BigDecimal("100"));
                gameDetails.put("currentPrice", priceInINR);
                gameDetails.put("currency", "INR");
                
                // Discount information
                if (priceOverview.has("discount_percent")) {
                    gameDetails.put("discountPercent", priceOverview.get("discount_percent").asInt());
                }
                if (priceOverview.has("initial")) {
                    int originalPrice = priceOverview.get("initial").asInt();
                    BigDecimal originalPriceINR = new BigDecimal(originalPrice).divide(new BigDecimal("100"));
                    gameDetails.put("originalPrice", originalPriceINR);
                }
            } else {
                gameDetails.put("currentPrice", BigDecimal.ZERO);
                gameDetails.put("currency", "INR");
                gameDetails.put("priceAvailable", dataNode.has("is_free") && dataNode.get("is_free").asBoolean());
            }
            
            // Additional details
            if (dataNode.has("steam_appid")) {
                gameDetails.put("steamAppId", dataNode.get("steam_appid").asText());
            }
            if (dataNode.has("developers")) {
                gameDetails.put("developers", streamTextValues(dataNode.get("developers")));
            }
            if (dataNode.has("publishers")) {
                gameDetails.put("publishers", streamTextValues(dataNode.get("publishers")));
            }
            
            return gameDetails;
            
        } catch (Exception e) {
            log.error("Error parsing game details for app ID {}: {}", appId, e.getMessage(), e);
            return null;
        }
    }

    private void putIfPresent(Map<String, Object> target, String key, JsonNode parent, String fieldName) {
        JsonNode value = parent.get(fieldName);
        if (value != null && !value.isNull()) {
            if (value.isNumber()) {
                target.put(key, value.asInt());
            } else {
                target.put(key, value.asText());
            }
        }
    }

    private java.util.List<String> streamDescriptions(JsonNode nodes) {
        return java.util.stream.StreamSupport.stream(nodes.spliterator(), false)
                .filter(node -> node.has("description"))
                .map(node -> node.get("description").asText())
                .collect(Collectors.toList());
    }

    private java.util.List<String> streamTextValues(JsonNode nodes) {
        return java.util.stream.StreamSupport.stream(nodes.spliterator(), false)
                .map(JsonNode::asText)
                .collect(Collectors.toList());
    }
    
    /**
     * Check if Steam API is available and we haven't hit rate limits.
     */
    public boolean isApiAvailable() {
        return rateLimiter.canMakeSteamApiCall();
    }
    
    /**
     * Get current rate limit status for monitoring.
     */
    public Map<String, Object> getRateLimitStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("apiKey", "steam");
        status.put("isAvailable", isApiAvailable());
        
        // Get actual usage stats from the rate limiter
        ApiRateLimiterService.ApiUsageStats stats = rateLimiter.getUsageStats("steam");
        status.put("usageStats", stats.toString());
        
        return status;
    }
}
