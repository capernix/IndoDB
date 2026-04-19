package com.indodb.games_backend.repository;

import com.indodb.games_backend.model.GamePrice;
import com.indodb.games_backend.model.PlatformType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GamePriceRepository extends JpaRepository<GamePrice, UUID> {
    
    // 🎯 CORE PRICE QUERIES FOR INDIAN GAMERS
    
    // Best deals in INR (highest discounts)
    @Query("""
        SELECT gp FROM GamePrice gp 
        JOIN FETCH gp.game g 
        JOIN FETCH gp.platform p
        WHERE gp.currency = 'INR' 
        AND gp.discountPercentage >= :minDiscount 
        AND gp.isOnSale = true
        AND g.isActive = true
        ORDER BY gp.discountPercentage DESC, gp.currentPrice ASC
        """)
    List<GamePrice> findBestDealsInINR(@Param("minDiscount") Integer minDiscount, Pageable pageable);
    
    // Free games across all platforms
    @Query("""
        SELECT gp FROM GamePrice gp 
        JOIN FETCH gp.game g 
        JOIN FETCH gp.platform p
        WHERE (gp.isFree = true OR gp.currentPrice = 0) 
        AND g.isActive = true
        ORDER BY gp.lastUpdated DESC
        """)
    List<GamePrice> findFreeGames(Pageable pageable);
    
    // Lowest prices for a specific game across platforms
    @Query("""
        SELECT gp FROM GamePrice gp 
        JOIN FETCH gp.platform p
        WHERE gp.game.id = :gameId 
        AND gp.currency = 'INR'
        ORDER BY gp.currentPrice ASC
        """)
    List<GamePrice> findLowestPricesForGame(@Param("gameId") UUID gameId);
    
    // Platform-specific pricing
    Optional<GamePrice> findByGameIdAndPlatformTypeAndCurrency(
        UUID gameId, PlatformType platformType, String currency);
    
    // Price tracking for specific game-platform combination
    Optional<GamePrice> findByGame_IdAndPlatform_Id(UUID gameId, Integer platformId);
    
    // Alternative method name for convenience
    default Optional<GamePrice> findByGameIdAndPlatformId(UUID gameId, Integer platformId) {
        return findByGame_IdAndPlatform_Id(gameId, platformId);
    }
    
    // 🔥 TRENDING PRICE-BASED QUERIES
    
    // Biggest price drops recently (trending deals)
    @Query(value = """
        SELECT gp.* FROM games.game_prices gp 
        INNER JOIN games.games g ON gp.game_id = g.id
        INNER JOIN games.platforms p ON gp.platform_id = p.id
        WHERE gp.currency = 'INR' 
        AND gp.discount_percentage > 0
        AND gp.last_updated >= CURRENT_TIMESTAMP - INTERVAL '7 days'
        AND g.is_active = true
        ORDER BY gp.discount_percentage DESC, gp.last_updated DESC
        """, nativeQuery = true)
    List<GamePrice> findRecentBiggestDrops(Pageable pageable);
    
    // Games under specific price range (budget gaming)
    @Query("""
        SELECT gp FROM GamePrice gp 
        JOIN FETCH gp.game g 
        JOIN FETCH gp.platform p
        WHERE gp.currency = 'INR' 
        AND gp.currentPrice BETWEEN :minPrice AND :maxPrice
        AND g.isActive = true
        ORDER BY gp.discountPercentage DESC, gp.currentPrice ASC
        """)
    List<GamePrice> findGamesInPriceRange(
        @Param("minPrice") BigDecimal minPrice, 
        @Param("maxPrice") BigDecimal maxPrice, 
        Pageable pageable);
    
    // Platform comparison for homepage stats
    @Query("""
        SELECT p.name, COUNT(gp), AVG(gp.currentPrice), AVG(gp.discountPercentage)
        FROM GamePrice gp 
        JOIN gp.platform p
        WHERE gp.currency = 'INR' 
        AND gp.currentPrice > 0
        GROUP BY p.name
        ORDER BY COUNT(gp) DESC
        """)
    List<Object[]> getPlatformPriceStats();
    
    // 💰 ADMIN/ANALYTICS QUERIES
    
    // Count active price entries
    long countByCurrencyAndGame_IsActiveTrue(String currency);
    
    // Recent price updates (for monitoring data freshness)
    @Query(value = """
        SELECT gp.* FROM games.game_prices gp 
        INNER JOIN games.games g ON gp.game_id = g.id
        INNER JOIN games.platforms p ON gp.platform_id = p.id
        WHERE gp.last_updated >= CURRENT_TIMESTAMP - INTERVAL '1 day'
        ORDER BY gp.last_updated DESC
        """, nativeQuery = true)
    List<GamePrice> findRecentlyUpdatedPrices(Pageable pageable);
}
