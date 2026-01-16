package com.indodb.games_backend.controller;

import com.indodb.games_backend.model.GamePrice;
import com.indodb.games_backend.service.PriceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/prices")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // For frontend development
@Tag(name = "Price Analytics", description = "💰 Price intelligence, deals discovery, and market analytics for Indian gaming")
public class PriceController {
    
    private final PriceService priceService;
    
    // 🔥 MAIN HOMEPAGE ENDPOINTS - Indian Game Price Intelligence
    
    /**
     * GET /api/prices/deals/biggest - Best deals for homepage hero section
     */
    @GetMapping("/deals/biggest")
    public ResponseEntity<List<GamePrice>> getBiggestDeals(
            @RequestParam(defaultValue = "10") int limit) {
        
        List<GamePrice> deals = priceService.getBiggestDeals(limit);
        
        log.info("API: Fetched {} biggest deals (50%+ off)", deals.size());
        return ResponseEntity.ok(deals);
    }
    
    /**
     * GET /api/prices/deals/good - Good deals section (25%+ off)
     */
    @GetMapping("/deals/good")
    public ResponseEntity<List<GamePrice>> getGoodDeals(
            @RequestParam(defaultValue = "20") int limit) {
        
        List<GamePrice> deals = priceService.getGoodDeals(limit);
        
        log.info("API: Fetched {} good deals (25%+ off)", deals.size());
        return ResponseEntity.ok(deals);
    }
    
    /**
     * GET /api/prices/deals/recent - Hot deals (recent price drops)
     */
    @GetMapping("/deals/recent")
    public ResponseEntity<List<GamePrice>> getRecentDeals(
            @RequestParam(defaultValue = "15") int limit) {
        
        List<GamePrice> recentDeals = priceService.getRecentPriceDrops(limit);
        
        log.info("API: Fetched {} recent price drops", recentDeals.size());
        return ResponseEntity.ok(recentDeals);
    }
    
    /**
     * GET /api/prices/free - Free games across all platforms
     */
    @GetMapping("/free")
    public ResponseEntity<List<GamePrice>> getFreeGames(
            @RequestParam(defaultValue = "10") int limit) {
        
        List<GamePrice> freeGames = priceService.getFreeGames(limit);
        
        log.info("API: Fetched {} free games", freeGames.size());
        return ResponseEntity.ok(freeGames);
    }
    
    /**
     * GET /api/prices/budget - Budget gaming (under specific price)
     */
    @GetMapping("/budget")
    public ResponseEntity<List<GamePrice>> getBudgetGames(
            @RequestParam(defaultValue = "500") BigDecimal maxPrice,
            @RequestParam(defaultValue = "20") int limit) {
        
        List<GamePrice> budgetGames = priceService.getBudgetGames(maxPrice, limit);
        
        log.info("API: Fetched {} games under ₹{}", budgetGames.size(), maxPrice);
        return ResponseEntity.ok(budgetGames);
    }
    
    // 🎮 GAME-SPECIFIC ENDPOINTS
    
    /**
     * GET /api/prices/compare/{gameId} - Price comparison across platforms
     */
    @GetMapping("/compare/{gameId}")
    public ResponseEntity<List<GamePrice>> comparePricesForGame(@PathVariable UUID gameId) {
        
        List<GamePrice> prices = priceService.getGamePriceComparison(gameId);
        
        if (prices.isEmpty()) {
            log.warn("No prices found for game {}", gameId);
            return ResponseEntity.notFound().build();
        }
        
        log.info("API: Price comparison for game {} - {} platforms", gameId, prices.size());
        return ResponseEntity.ok(prices);
    }
    
    // 📊 STATS & ANALYTICS ENDPOINTS
    
    /**
     * GET /api/prices/stats/platforms - Platform comparison stats
     */
    @GetMapping("/stats/platforms")
    public ResponseEntity<PriceService.PlatformStats> getPlatformStats() {
        
        PriceService.PlatformStats stats = priceService.getPlatformStats();
        
        log.info("API: Fetched platform statistics");
        return ResponseEntity.ok(stats);
    }
    
    /**
     * GET /api/prices/stats/overview - Overall price statistics
     */
    @GetMapping("/stats/overview")
    public ResponseEntity<PriceOverview> getPriceOverview() {
        
        long totalINRPrices = priceService.getTotalINRPriceCount();
        List<GamePrice> biggestDeals = priceService.getBiggestDeals(5);
        List<GamePrice> freeGames = priceService.getFreeGames(5);
        
        PriceOverview overview = PriceOverview.builder()
                .totalGamesWithINRPricing(totalINRPrices)
                .biggestDealsCount(biggestDeals.size())
                .freeGamesCount(freeGames.size())
                .build();
        
        log.info("API: Price overview - {} games with INR pricing", totalINRPrices);
        return ResponseEntity.ok(overview);
    }
    
    // 📈 DATA STRUCTURE FOR OVERVIEW
    @lombok.Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class PriceOverview {
        private long totalGamesWithINRPricing;
        private int biggestDealsCount;
        private int freeGamesCount;
        
        @lombok.Builder.Default
        private String lastUpdated = java.time.LocalDateTime.now().toString();
    }
}
