package com.indodb.games_backend.repository;

import com.indodb.games_backend.model.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, UUID> {
    
    /**
     * Find all active wishlist items for a user
     */
    List<Wishlist> findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(UUID userId);
    
    /**
     * Find specific wishlist item for user and game
     */
    Optional<Wishlist> findByUserIdAndGameId(UUID userId, UUID gameId);
    
    /**
     * Check if user has game in wishlist
     */
    boolean existsByUserIdAndGameIdAndIsActiveTrue(UUID userId, UUID gameId);
    
    /**
     * Count wishlisters for a game (for trending calculations)
     */
    @Query("SELECT COUNT(w) FROM Wishlist w WHERE w.game.id = :gameId AND w.isActive = true")
    long countByGameId(@Param("gameId") UUID gameId);
    
    /**
     * Find all users who wishlisted a specific game (for price drop notifications)
     */
    @Query("SELECT w FROM Wishlist w WHERE w.game.id = :gameId AND w.isActive = true")
    List<Wishlist> findActiveWishlistsByGameId(@Param("gameId") UUID gameId);
    
    /**
     * Find wishlists where current price is below target price (for alerts)
     */
    @Query(value = """
        SELECT w.* FROM users.wishlists w
        INNER JOIN games.game_prices gp ON w.game_id = gp.game_id
        WHERE w.user_id = :userId 
        AND w.is_active = true
        AND w.target_price IS NOT NULL
        AND gp.current_price <= w.target_price
        """, nativeQuery = true)
    List<Wishlist> findPriceAlertOpportunities(@Param("userId") UUID userId);
}
