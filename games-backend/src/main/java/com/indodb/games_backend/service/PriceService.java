package com.indodb.games_backend.service;

import com.indodb.games_backend.model.GamePrice;
import com.indodb.games_backend.repository.GamePriceRepository;
import com.indodb.games_backend.repository.PriceHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PriceService {
    
    private final GamePriceRepository gamePriceRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    
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

    public List<PriceHistoryPoint> getGamePriceHistory(UUID gameId, int days) {
        int safeDays = Math.max(7, Math.min(days, 365));
        LocalDateTime since = LocalDateTime.now().minusDays(safeDays);

        List<Object[]> rows = priceHistoryRepository.findLatestDailySnapshotsByGameIdSince(gameId, since);
        Map<LocalDate, PriceHistoryPoint> points = new LinkedHashMap<>();

        for (Object[] row : rows) {
            if (row == null || row.length < 3) {
                continue;
            }

            LocalDate date = toLocalDate(row[0]);
            String platformType = row[1] == null ? null : row[1].toString();
            BigDecimal price = toBigDecimal(row[2]);

            if (date == null || platformType == null || price == null) {
                continue;
            }

            PriceHistoryPoint point = points.computeIfAbsent(date, d -> PriceHistoryPoint.builder()
                    .date(d.toString())
                    .build());

            if ("STEAM".equalsIgnoreCase(platformType)) {
                point.setSteam(price);
            } else if ("EPIC".equalsIgnoreCase(platformType)) {
                point.setEpic(price);
            } else if ("GOG".equalsIgnoreCase(platformType)) {
                point.setGog(price);
            }
        }

        List<PriceHistoryPoint> result = points.values().stream().toList();
        log.info("Fetched {} history points for game {} (days={})", result.size(), gameId, safeDays);
        return result;
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

    @lombok.Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class PriceHistoryPoint {
        private String date;
        private BigDecimal steam;
        private BigDecimal epic;
        private BigDecimal gog;
    }

    private LocalDate toLocalDate(Object value) {
        if (value instanceof Timestamp ts) {
            return ts.toLocalDateTime().toLocalDate();
        }
        if (value instanceof java.sql.Date date) {
            return date.toLocalDate();
        }
        if (value instanceof LocalDateTime ldt) {
            return ldt.toLocalDate();
        }
        if (value instanceof LocalDate ld) {
            return ld;
        }
        return null;
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value instanceof BigDecimal b) {
            return b;
        }
        if (value instanceof Number n) {
            return BigDecimal.valueOf(n.doubleValue());
        }
        if (value == null) {
            return null;
        }
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
