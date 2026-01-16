package com.indodb.games_backend.service;

import com.indodb.games_backend.model.Game;
import com.indodb.games_backend.model.User;
import com.indodb.games_backend.model.Wishlist;
import com.indodb.games_backend.repository.GameRepository;
import com.indodb.games_backend.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Wishlist service for managing user game wishlists
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class WishlistService {
    
    private final WishlistRepository wishlistRepository;
    private final GameRepository gameRepository;
    
    /**
     * Get user's wishlist
     */
    public List<Wishlist> getUserWishlist(UUID userId) {
        log.debug("📋 Fetching wishlist for user: {}", userId);
        return wishlistRepository.findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(userId);
    }
    
    /**
     * Add game to wishlist
     */
    @Transactional
    public Wishlist addToWishlist(User user, UUID gameId, BigDecimal targetPrice) {
        log.info("➕ Adding game {} to wishlist for user: {}", gameId, user.getUsername());
        
        // Check if already in wishlist
        if (wishlistRepository.existsByUserIdAndGameIdAndIsActiveTrue(user.getId(), gameId)) {
            throw new IllegalArgumentException("Game already in wishlist");
        }
        
        // Verify game exists
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found: " + gameId));
        
        // Create wishlist entry
        Wishlist wishlist = Wishlist.builder()
                .user(user)
                .game(game)
                .targetPrice(targetPrice)
                .isActive(true)
                .build();
        
        Wishlist saved = wishlistRepository.save(wishlist);
        
        log.info("✅ Game added to wishlist: {} for user: {}", game.getTitle(), user.getUsername());
        
        return saved;
    }
    
    /**
     * Remove game from wishlist (soft delete)
     */
    @Transactional
    public void removeFromWishlist(UUID userId, UUID gameId) {
        log.info("➖ Removing game {} from wishlist for user: {}", gameId, userId);
        
        Wishlist wishlist = wishlistRepository.findByUserIdAndGameId(userId, gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not in wishlist"));
        
        wishlist.setIsActive(false);
        wishlistRepository.save(wishlist);
        
        log.info("✅ Game removed from wishlist");
    }
    
    /**
     * Update target price for wishlist item
     */
    @Transactional
    public Wishlist updateTargetPrice(UUID userId, UUID gameId, BigDecimal targetPrice) {
        log.info("💰 Updating target price for game {} to ₹{}", gameId, targetPrice);
        
        Wishlist wishlist = wishlistRepository.findByUserIdAndGameId(userId, gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not in wishlist"));
        
        wishlist.setTargetPrice(targetPrice);
        Wishlist updated = wishlistRepository.save(wishlist);
        
        log.info("✅ Target price updated");
        
        return updated;
    }
    
    /**
     * Get all users who wishlisted a specific game (for notifications)
     */
    public List<Wishlist> getWishlistersForGame(UUID gameId) {
        return wishlistRepository.findActiveWishlistsByGameId(gameId);
    }
    
    /**
     * Get price alert opportunities for user
     */
    public List<Wishlist> getPriceAlertOpportunities(UUID userId) {
        return wishlistRepository.findPriceAlertOpportunities(userId);
    }
}
