package com.indodb.games_backend.service;

import com.indodb.games_backend.events.PriceChangeEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PriceEventConsumer {
    
    @KafkaListener(topics = "price-changes", groupId = "indodb-price-alerts")
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
        // Log the details
        if (event.isPriceDrop()) {
            log.info("🔻 Price DROP detected: {} - Save ₹{} ({:.1f}% off)", 
                    event.getGameTitle(), 
                    event.getDiscountAmount(), 
                    event.getDiscountPercentage());
            
            if (event.isSignificantDrop()) {
                log.info("🚨 SIGNIFICANT PRICE DROP: {} - This is a great deal!", event.getGameTitle());
                // Here we would send notifications to users who have this game wishlisted
                // For now, just log it
            }
        } else {
            log.info("📈 Price INCREASE: {} - Price went up by ₹{}", 
                    event.getGameTitle(), 
                    event.getNewPrice().subtract(event.getOldPrice()));
        }
        
        // TODO: In Step 2, we'll:
        // 1. Update database with new price
        // 2. Check if any users have this game wishlisted
        // 3. Send real-time notifications
        // 4. Update trending calculations
        
        log.info("✅ Price change processing completed for {}", event.getGameTitle());
    }
}
