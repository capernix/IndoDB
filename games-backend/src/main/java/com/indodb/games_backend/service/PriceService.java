package com.indodb.games_backend.service;

import com.indodb.games_backend.model.GamePrice;
import com.indodb.games_backend.repository.GamePriceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PriceService {
    
    private final GamePriceRepository gamePriceRepository;
    
    // 🎯 CORE INDIAN GAMING PRICE INTELLIGENCE
    
    /**
     * Get the best deals in INR - your main homepage feature!
     */
    public List<GamePrice> getBestDealsInINR(int limit, int minDiscountPercent) {
        Pageable pageable = PageRequest.of(0, limit);
        List<GamePrice> deals = gamePriceRepository.findBestDealsInINR(minDiscountPercent, pageable);
        
        log.info("Found {} best deals in INR with {}%+ discount", deals.size(), minDiscountPercent);
        return deals;
    }
    
    /**
     * Get biggest deals (50%+ off) - trending section
     */
    public List<GamePrice> getBiggestDeals(int limit) {
        return getBestDealsInINR(limit, 50); // 50%+ discount
    }
    
    /**
     * Get good deals (25%+ off) - broader selection
     */
    public List<GamePrice> getGoodDeals(int limit) {
        return getBestDealsInINR(limit, 25); // 25%+ discount
    }
    
    /**
     * Get free games across all platforms
     */
    public List<GamePrice> getFreeGames(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<GamePrice> freeGames = gamePriceRepository.findFreeGames(pageable);
        
        log.info("Found {} free games", freeGames.size());
        return freeGames;
    }
    
    /**
     * Get recent price drops (last 7 days) - hot deals
     */
    public List<GamePrice> getRecentPriceDrops(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<GamePrice> recentDrops = gamePriceRepository.findRecentBiggestDrops(pageable);
        
        log.info("Found {} recent price drops", recentDrops.size());
        return recentDrops;
    }
    
    /**
     * Budget gaming - games under specific price
     */
    public List<GamePrice> getBudgetGames(BigDecimal maxPrice, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        BigDecimal minPrice = BigDecimal.ZERO;
        
        List<GamePrice> budgetGames = gamePriceRepository.findGamesInPriceRange(minPrice, maxPrice, pageable);
        
        log.info("Found {} games under ₹{}", budgetGames.size(), maxPrice);
        return budgetGames;
    }
    
    /**
     * Price comparison for a specific game across platforms
     */
    public List<GamePrice> getGamePriceComparison(UUID gameId) {
        List<GamePrice> prices = gamePriceRepository.findLowestPricesForGame(gameId);
        
        log.info("Found {} price options for game {}", prices.size(), gameId);
        return prices;
    }
    
    /**
     * Platform statistics for homepage insights
     */
    public PlatformStats getPlatformStats() {
        List<Object[]> stats = gamePriceRepository.getPlatformPriceStats();
        
        // Process the raw data into a nice format
        List<PlatformStats.PlatformInfo> platforms = new java.util.ArrayList<>();
        
        for (Object[] row : stats) {
            String platformName = (String) row[0];
            Long gameCount = (Long) row[1];
            Double avgPrice = (Double) row[2];
            Double avgDiscount = (Double) row[3];
            
            platforms.add(PlatformStats.PlatformInfo.builder()
                .name(platformName)
                .gameCount(gameCount.intValue())
                .averagePrice(BigDecimal.valueOf(avgPrice))
                .averageDiscount(avgDiscount.intValue())
                .build());
        }
        
        return PlatformStats.builder()
            .platforms(platforms)
            .build();
    }
    
    // 💾 CRUD Operations
    @Transactional
    public GamePrice saveGamePrice(GamePrice gamePrice) {
        log.info("Saving price for game {} on platform {}: {}", 
                gamePrice.getGame().getTitle(), 
                gamePrice.getPlatform().getName(), 
                gamePrice.getFormattedPrice());
        
        return gamePriceRepository.save(gamePrice);
    }
    
    public Optional<GamePrice> getGamePriceById(UUID id) {
        return gamePriceRepository.findById(id);
    }
    
    // 📊 Stats and Analytics
    public long getTotalINRPriceCount() {
        return gamePriceRepository.countByCurrencyAndGame_IsActiveTrue("INR");
    }
    
    /**
     * Platform statistics data structure
     */
    @lombok.Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class PlatformStats {
        private List<PlatformInfo> platforms;
        
        @lombok.Data
        @lombok.AllArgsConstructor
        @lombok.NoArgsConstructor
        @lombok.Builder
        public static class PlatformInfo {
            private String name;
            private int gameCount;
            private BigDecimal averagePrice;
            private int averageDiscount;
        }
    }
}
