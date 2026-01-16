package com.indodb.games_backend.scheduler;

import com.indodb.games_backend.events.PriceChangeEvent;
import com.indodb.games_backend.model.Game;
import com.indodb.games_backend.model.GamePrice;
import com.indodb.games_backend.repository.GamePriceRepository;
import com.indodb.games_backend.repository.GameRepository;
import com.indodb.games_backend.service.ApiRateLimiterService;
import com.indodb.games_backend.service.PriceEventProducer;
import com.indodb.games_backend.service.SteamApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Scheduled job to fetch and update game prices hourly
 * Respects rate limits and processes games in batches
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PriceFetchScheduler {
    
    private final GameRepository gameRepository;
    private final GamePriceRepository gamePriceRepository;
    private final SteamApiService steamApiService;
    private final ApiRateLimiterService rateLimiterService;
    private final PriceEventProducer priceEventProducer;
    
    @Value("${price.fetch.batch-size:50}")
    private int batchSize;
    
    private int currentPage = 0;
    
    /**
     * Fetch prices for games hourly
     * Processes games in batches to respect rate limits
     */
    @Scheduled(fixedRateString = "${price.fetch.interval:3600000}")
    public void fetchPrices() {
        log.info("🕐 Starting scheduled price fetch (batch size: {})", batchSize);
        
        try {
            // Get a batch of games with Steam IDs
            Page<Game> gamesPage = gameRepository.findByIsActiveTrueOrderByCreatedAtDesc(
                    PageRequest.of(currentPage, batchSize)
            );
            
            if (gamesPage.isEmpty()) {
                // Reset to first page if we've processed all games
                currentPage = 0;
                log.info("🔄 Completed full cycle, resetting to first page");
                return;
            }
            
            int successCount = 0;
            int skipCount = 0;
            
            for (Game game : gamesPage.getContent()) {
                // Only fetch for games with Steam IDs
                if (game.getSteamAppId() == null) {
                    skipCount++;
                    continue;
                }
                
                // Check rate limit before each call
                if (!rateLimiterService.canMakeSteamApiCall()) {
                    log.warn("⚠️ Rate limit reached, stopping batch processing");
                    break;
                }
                
                try {
                    // Fetch current price from Steam
                    BigDecimal newPrice = steamApiService.getCurrentPrice(game.getSteamAppId().toString());
                    
                    if (newPrice != null) {
                        // Check if price changed
                        gamePriceRepository.findByGameIdAndPlatformId(game.getId(), 1) // 1 = Steam platform
                                .ifPresent(existingPrice -> {
                                    if (!newPrice.equals(existingPrice.getCurrentPrice())) {
                                        // Price changed - publish event
                                        PriceChangeEvent event = new PriceChangeEvent(
                                                game.getTitle(),
                                                existingPrice.getCurrentPrice(),
                                                newPrice
                                        );
                                        priceEventProducer.sendPriceChangeEvent(event);
                                        
                                        log.info("💰 Price changed for {}: ₹{} → ₹{}", 
                                                game.getTitle(), existingPrice.getCurrentPrice(), newPrice);
                                    }
                                });
                        
                        successCount++;
                    }
                    
                    // Record the API call
                    rateLimiterService.recordApiCall("steam");
                    
                } catch (Exception e) {
                    log.error("❌ Error fetching price for game {}: {}", game.getTitle(), e.getMessage());
                }
            }
            
            log.info("✅ Batch complete: {} prices fetched, {} skipped (no Steam ID)", successCount, skipCount);
            
            // Move to next page for next run
            currentPage++;
            
        } catch (Exception e) {
            log.error("❌ Error in scheduled price fetch: {}", e.getMessage(), e);
        }
    }
}
