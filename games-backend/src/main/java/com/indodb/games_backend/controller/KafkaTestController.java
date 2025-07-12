package com.indodb.games_backend.controller;

import com.indodb.games_backend.service.PriceEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/test/kafka")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class KafkaTestController {
    
    private final PriceEventProducer priceEventProducer;
    
    /**
     * Test endpoint to trigger a price change event
     * Example: /api/test/kafka/price-change?gameTitle=GTA V&oldPrice=2999&newPrice=1499
     */
    @PostMapping("/price-change")
    public ResponseEntity<Map<String, Object>> testPriceChange(
            @RequestParam String gameTitle,
            @RequestParam double oldPrice,
            @RequestParam double newPrice) {
        
        log.info("🧪 Testing Kafka with price change: {} from ₹{} to ₹{}", gameTitle, oldPrice, newPrice);
        
        try {
            // Send the price change event through Kafka
            priceEventProducer.sendTestPriceChange(gameTitle, oldPrice, newPrice);
            
            Map<String, Object> response = Map.of(
                "status", "success",
                "message", "Price change event sent to Kafka",
                "gameTitle", gameTitle,
                "oldPrice", oldPrice,
                "newPrice", newPrice,
                "kafkaTopic", "price-changes"
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Failed to test Kafka price change: {}", e.getMessage(), e);
            
            Map<String, Object> response = Map.of(
                "status", "error",
                "message", "Failed to send price change event: " + e.getMessage(),
                "gameTitle", gameTitle
            );
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Test endpoint for popular Indian games
     */
    @PostMapping("/test-indian-games")
    public ResponseEntity<Map<String, Object>> testIndianGamePrices() {
        log.info("🇮🇳 Testing Kafka with popular Indian game prices");
        
        try {
            // Simulate price changes for your sample games
            priceEventProducer.sendTestPriceChange("GTA V", 2999.0, 1499.0);          // 50% off
            priceEventProducer.sendTestPriceChange("The Witcher 3", 1399.0, 699.0);  // 50% off  
            priceEventProducer.sendTestPriceChange("Among Us", 339.0, 169.0);         // 50% off
            priceEventProducer.sendTestPriceChange("Counter-Strike 2", 0.0, 0.0);     // Free game
            
            Map<String, Object> response = Map.of(
                "status", "success",
                "message", "Sent 4 test price change events for popular Indian games",
                "games", new String[]{"GTA V", "The Witcher 3", "Among Us", "Counter-Strike 2"}
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Failed to test Indian game prices: {}", e.getMessage(), e);
            
            Map<String, Object> response = Map.of(
                "status", "error",
                "message", "Failed to send test events: " + e.getMessage()
            );
            
            return ResponseEntity.status(500).body(response);
        }
    }
}
