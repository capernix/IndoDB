package com.indodb.games_backend.controller;

import com.indodb.games_backend.model.User;
import com.indodb.games_backend.model.Wishlist;
import com.indodb.games_backend.service.UserService;
import com.indodb.games_backend.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Wishlist Controller
 * Manages user game wishlists with price alerts
 */
@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "Wishlist", description = "⭐ User wishlist management with price alerts")
public class WishlistController {
    
    private final WishlistService wishlistService;
    private final UserService userService;
    
    @GetMapping
    @Operation(summary = "Get user wishlist", description = "Get all games in authenticated user's wishlist")
    public ResponseEntity<List<Wishlist>> getUserWishlist(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.getCurrentUser();
            List<Wishlist> wishlist = wishlistService.getUserWishlist(user.getId());
            
            log.info("📋 Wishlist retrieved for user: {} ({} items)", user.getUsername(), wishlist.size());
            
            return ResponseEntity.ok(wishlist);
            
        } catch (Exception e) {
            log.error("❌ Error fetching wishlist: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
    
    @PostMapping("/{gameId}")
    @Operation(summary = "Add to wishlist", description = "Add a game to user's wishlist with optional target price")
    public ResponseEntity<Wishlist> addToWishlist(
            @PathVariable UUID gameId,
            @RequestBody(required = false) AddToWishlistRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            User user = userService.getCurrentUser();
            BigDecimal targetPrice = request != null ? request.getTargetPrice() : null;
            
            Wishlist wishlist = wishlistService.addToWishlist(user, gameId, targetPrice);
            
            return ResponseEntity.ok(wishlist);
            
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Add to wishlist failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("❌ Error adding to wishlist: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
    
    @DeleteMapping("/{gameId}")
    @Operation(summary = "Remove from wishlist", description = "Remove a game from user's wishlist")
    public ResponseEntity<Void> removeFromWishlist(
            @PathVariable UUID gameId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            User user = userService.getCurrentUser();
            wishlistService.removeFromWishlist(user.getId(), gameId);
            
            return ResponseEntity.noContent().build();
            
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Remove from wishlist failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("❌ Error removing from wishlist: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
    
    @PutMapping("/{gameId}/target-price")
    @Operation(summary = "Update target price", description = "Set or update target price for price drop alerts")
    public ResponseEntity<Wishlist> updateTargetPrice(
            @PathVariable UUID gameId,
            @RequestBody UpdateTargetPriceRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            User user = userService.getCurrentUser();
            Wishlist updated = wishlistService.updateTargetPrice(user.getId(), gameId, request.getTargetPrice());
            
            return ResponseEntity.ok(updated);
            
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Update target price failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("❌ Error updating target price: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
    
    @GetMapping("/price-alerts")
    @Operation(summary = "Get price alerts", description = "Get wishlist items where current price is below target")
    public ResponseEntity<List<Wishlist>> getPriceAlerts(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.getCurrentUser();
            List<Wishlist> alerts = wishlistService.getPriceAlertOpportunities(user.getId());
            
            log.info("🔔 Found {} price alert opportunities for user: {}", alerts.size(), user.getUsername());
            
            return ResponseEntity.ok(alerts);
            
        } catch (Exception e) {
            log.error("❌ Error fetching price alerts: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
    
    // Request DTOs
    
    @Data
    public static class AddToWishlistRequest {
        private BigDecimal targetPrice;
    }
    
    @Data
    public static class UpdateTargetPriceRequest {
        private BigDecimal targetPrice;
    }
}
