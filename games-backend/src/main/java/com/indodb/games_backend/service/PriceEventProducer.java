package com.indodb.games_backend.service;

import com.indodb.games_backend.events.PriceChangeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceEventProducer {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    public static final String PRICE_CHANGE_TOPIC = "price-changes";
    
    public void sendPriceChangeEvent(PriceChangeEvent event) {
        try {
            log.info("📤 Producing price change event: {} price changed from ₹{} to ₹{}", 
                    event.getGameTitle(), event.getOldPrice(), event.getNewPrice());
            
            kafkaTemplate.send(PRICE_CHANGE_TOPIC, event.getGameTitle(), event);
            
            log.info("✅ Price change event sent successfully for {}", event.getGameTitle());
            
        } catch (Exception e) {
            log.error("❌ Failed to send price change event for {}: {}", 
                    event.getGameTitle(), e.getMessage(), e);
        }
    }
    
    // Utility method for quick testing
    public void sendTestPriceChange(String gameTitle, double oldPrice, double newPrice) {
        PriceChangeEvent event = new PriceChangeEvent(
            gameTitle, 
            java.math.BigDecimal.valueOf(oldPrice), 
            java.math.BigDecimal.valueOf(newPrice)
        );
        sendPriceChangeEvent(event);
    }
}
