package com.indodb.games_backend.service;

import com.indodb.games_backend.events.PriceChangeEvent;
import com.indodb.games_backend.model.GamePrice;
import com.indodb.games_backend.model.PriceHistory;
import com.indodb.games_backend.model.Wishlist;
import com.indodb.games_backend.repository.GamePriceRepository;
import com.indodb.games_backend.repository.PriceHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceEventConsumer {
    
    private final GamePriceRepository gamePriceRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final WishlistService wishlistService;
    private final EmailService emailService;
    private final GameCacheService gameCacheService;
    
    @KafkaListener(topics = "price-changes", groupId = "indodb-price-alerts")
    @Transactional
    public void handlePriceChangeEvent(PriceChangeEvent event) {
        try {
            log.info("📥 Consumed price change event: {} price changed from ₹{} to ₹{}", 
                    event.getGameTitle(), event.getOldPrice(), event.getNewPrice());
            
            // Process the price change
            processPriceChange(event);
            
        } catch (Exception e) {
            log.error("❌ Error processing price change event for {}: {}", 
                    event.getGameTitle(), e.getMessage(), e);
        }
    }
    
    private void processPriceChange(PriceChangeEvent event) {
        // 1. Update game_prices table
        updateGamePrice(event);
        
        // 2. Record price history
        recordPriceHistory(event);
        
        // 3. Update Redis cache
        updateCache(event);
        
        // 4. Check wishlists and send notifications
        if (event.isPriceDrop()) {
            log.info("🔻 Price DROP detected: {} - Save ₹{} ({:.1f}% off)", 
                    event.getGameTitle(), 
                    event.getDiscountAmount(), 
                    event.getDiscountPercentage());
            
            if (event.isSignificantDrop()) {
                log.info("🚨 SIGNIFICANT PRICE DROP: {} - This is a great deal!", event.getGameTitle());
            }
            
            // Send notifications to wishlisters
            notifyWishlisters(event);
        } else {
            log.info("📈 Price INCREASE: {} - Price went up by ₹{}", 
                    event.getGameTitle(), 
                    event.getNewPrice().subtract(event.getOldPrice()));
        }
        
        log.info("✅ Price change processing completed for {}", event.getGameTitle());
    }
    
    private void updateGamePrice(PriceChangeEvent event) {
        // Note: This assumes we have gameId in the event
        // For now, we'll skip this as we need to enhance PriceChangeEvent first
        // TODO: Add gameId and platformId to PriceChangeEvent
        log.debug("💾 Would update game_prices table (needs gameId in event)");
    }
    
    private void recordPriceHistory(PriceChangeEvent event) {
        // Note: This also needs gameId and platformId from event
        // TODO: Create PriceHistory record
        log.debug("📊 Would record price history (needs gameId in event)");
    }
    
    private void updateCache(PriceChangeEvent event) {
        try {
            // Update cache with new price
            // Note: Needs appId from event
            log.debug("💾 Cache would be updated (needs appId in event)");
        } catch (Exception e) {
            log.warn("⚠️ Failed to update cache: {}", e.getMessage());
        }
    }
    
    private void notifyWishlisters(PriceChangeEvent event) {
        try {
            // Note: This needs gameId to find wishlisters
            // For now, log the intent
            log.info("📧 Would notify wishlisters about price drop for: {}", event.getGameTitle());
            
            // TODO: Once we have gameId in event:
            // List<Wishlist> wishlisters = wishlistService.getWishlistersForGame(gameId);
            // for (Wishlist wishlist : wishlisters) {
            //     if (wishlist.getTargetPrice() == null || event.getNewPrice().compareTo(wishlist.getTargetPrice()) <= 0) {
            //         emailService.sendPriceDropAlert(
            //             wishlist.getUser().getEmail(),
            //             event.getGameTitle(),
            //             event.getOldPrice().toString(),
            //             event.getNewPrice().toString(),
            //             event.getDiscountPercentage().toString()
            //         );
            //     }
            // }
            
        } catch (Exception e) {
            log.error("❌ Error notifying wishlisters: {}", e.getMessage());
        }
    }
}
