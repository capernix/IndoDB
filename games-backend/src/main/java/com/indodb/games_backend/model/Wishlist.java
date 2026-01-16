package com.indodb.games_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Wishlist entity matching users.wishlists table
 * Tracks games users want to buy with optional target price for alerts
 */
@Entity
@Table(name = "wishlists", schema = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Wishlist {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false)
    @EqualsAndHashCode.Include
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;
    
    @Column(name = "target_price", precision = 10, scale = 2)
    private BigDecimal targetPrice;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "currency")
    @Builder.Default
    private PriceCurrency currency = PriceCurrency.INR;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "platform_preference")
    private PlatformType platformPreference;
    
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
