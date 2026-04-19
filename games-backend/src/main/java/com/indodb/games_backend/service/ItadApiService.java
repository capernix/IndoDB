package com.indodb.games_backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service to interact with the IsThereAnyDeal (ITAD) API.
 * Provides lookup capabilities to map Steam App IDs to ITAD UUIDs,
 * and fetches multi-store pricing (Epic, GOG) and historical lows.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ItadApiService {

    private final ApiRateLimiterService rateLimiter;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${itad.api.key:}")
    private String itadApiKey;

    @Value("${itad.api.base-url:https://api.isthereanydeal.com}")
    private String itadApiBaseUrl;

    public boolean isConfigured() {
        return itadApiKey != null
                && !itadApiKey.isBlank()
                && !itadApiKey.contains("your_itad_api_key_here");
    }

    /**
     * Looks up an ITAD game UUID from a Steam App ID.
     * Uses GET /games/lookup/v1
     * 
     * @param steamAppId Steam App ID
     * @return ITAD UUID as String, or null if not found
     */
    public String lookupItadIdBySteamId(String steamAppId) {
        if (!isConfigured()) {
            log.warn("ITAD API key not configured. Skipping lookup.");
            return null;
        }

        if (!rateLimiter.canMakeOtherApiCall("itad")) {
            log.warn("ITAD API rate limit reached. Skipping lookup for Steam App ID: {}", steamAppId);
            return null;
        }

        try {
            // Provide key as query param
            String url = itadApiBaseUrl + "/games/lookup/v1?key=" + itadApiKey + "&appid=" + steamAppId;
            log.debug("Calling ITAD lookup API for Steam ID: {}", steamAppId);
            
            String response = restTemplate.getForObject(url, String.class);
            rateLimiter.recordApiCall("itad");

            if (response != null) {
                JsonNode root = objectMapper.readTree(response);
                if (root.has("found") && root.get("found").asBoolean() && root.has("game")) {
                    return root.get("game").get("id").asText();
                }
            }
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Game not found on ITAD for Steam ID: {}", steamAppId);
            rateLimiter.recordApiCall("itad");
        } catch (Exception e) {
            log.error("Error looking up ITAD ID for Steam ID {}: {}", steamAppId, e.getMessage());
        }
        
        return null;
    }

    /**
     * Fetches prices for a batch of ITAD UUIDs.
     * Extracts prices specifically for Epic Games Store and GOG.
     * Uses POST /games/prices/v3
     * 
     * @param itadIds List of ITAD UUIDs
     * @return Map of ITAD UUID -> Map of Platform -> Price Data
     */
    public Map<String, Map<String, BigDecimal>> getBatchPrices(List<String> itadIds) {
        Map<String, Map<String, StorePrice>> detailedPrices = getBatchPriceDetails(itadIds);
        Map<String, Map<String, BigDecimal>> results = new HashMap<>();

        detailedPrices.forEach((itadId, platformPrices) -> {
            Map<String, BigDecimal> prices = new HashMap<>();
            platformPrices.forEach((platform, price) -> prices.put(platform, price.currentPrice()));
            results.put(itadId, prices);
        });

        return results;
    }

    public Map<String, Map<String, StorePrice>> getBatchPriceDetails(List<String> itadIds) {
        Map<String, Map<String, StorePrice>> results = new HashMap<>();
        
        if (itadIds == null || itadIds.isEmpty()) return results;
        if (!isConfigured()) {
            log.warn("ITAD API key not configured. Skipping price fetch.");
            return results;
        }

        if (!rateLimiter.canMakeOtherApiCall("itad")) {
            log.warn("ITAD API rate limit reached. Skipping bulk price fetch.");
            return results;
        }

        try {
            String url = itadApiBaseUrl + "/games/prices/v3?key=" + itadApiKey + "&country=IN";
            log.info("Calling ITAD prices API for {} games", itadIds.size());
            
            // Send the array of UUIDs as the JSON body
            String requestJson = objectMapper.writeValueAsString(itadIds);
            
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(requestJson, headers);
            
            String response = restTemplate.postForObject(url, entity, String.class);
            rateLimiter.recordApiCall("itad");

            if (response != null && !response.isEmpty()) {
                JsonNode root = objectMapper.readTree(response);
                // v3 returns an array of price objects, one per game
                for (JsonNode gameNode : root) {
                    if (!gameNode.has("id")) continue;
                    String gameId = gameNode.get("id").asText();
                    Map<String, StorePrice> platformPrices = new HashMap<>();
                    
                    if (gameNode.has("deals")) {
                        for (JsonNode deal : gameNode.get("deals")) {
                            if (!deal.has("shop")) continue;
                            String shopName = deal.get("shop").get("name").asText().toLowerCase();
                            
                            // Check if this is a store we care about
                            boolean isEpic = shopName.contains("epic");
                            boolean isGog = shopName.contains("gog");
                            
                            if (isEpic || isGog) {
                                StorePrice price = parseStorePrice(deal);
                                if (price != null) {
                                    
                                    if (isEpic && !platformPrices.containsKey("EPIC")) {
                                        platformPrices.put("EPIC", price);
                                    } else if (isGog && !platformPrices.containsKey("GOG")) {
                                        platformPrices.put("GOG", price);
                                    }
                                }
                            }
                        }
                    }
                    results.put(gameId, platformPrices);
                }
            }
        } catch (Exception e) {
            log.error("Error fetching ITAD prices: {}", e.getMessage());
        }
        
        return results;
    }

    private StorePrice parseStorePrice(JsonNode deal) {
        JsonNode priceNode = deal.get("price");
        if (priceNode == null || !priceNode.has("amount")) {
            return null;
        }

        String currency = priceNode.has("currency") ? priceNode.get("currency").asText() : "INR";
        if (!"INR".equalsIgnoreCase(currency)) {
            log.debug("Skipping non-INR ITAD deal price in country=IN response: {}", currency);
            return null;
        }

        BigDecimal currentPrice = new BigDecimal(priceNode.get("amount").asText());
        BigDecimal originalPrice = currentPrice;
        JsonNode regularNode = deal.get("regular");
        if (regularNode != null && regularNode.has("amount")) {
            originalPrice = new BigDecimal(regularNode.get("amount").asText());
        }

        Integer discountPercentage = deal.has("cut") ? deal.get("cut").asInt() : calculateDiscount(currentPrice, originalPrice);

        return new StorePrice(currentPrice, originalPrice, discountPercentage);
    }

    private Integer calculateDiscount(BigDecimal currentPrice, BigDecimal originalPrice) {
        if (originalPrice == null || originalPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }

        return originalPrice.subtract(currentPrice)
                .multiply(new BigDecimal("100"))
                .divide(originalPrice, 0, java.math.RoundingMode.HALF_UP)
                .intValue();
    }

    public record StorePrice(
            BigDecimal currentPrice,
            BigDecimal originalPrice,
            Integer discountPercentage
    ) {}
}
