package com.indodb.games_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * PriceHistory entity matching games.price_history table
 * Records historical price snapshots for trend analysis
 */
@Entity
@Table(name = "price_history", schema = "games")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PriceHistory {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false)
    @EqualsAndHashCode.Include
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;
    
    @Column(name = "platform_id", nullable = false)
    private Integer platformId;
    
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(name = "original_price", precision = 10, scale = 2)
    private BigDecimal originalPrice;
    
    @Column(name = "discount_percentage")
    @Builder.Default
    private Integer discountPercentage = 0;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "currency")
    @Builder.Default
    private PriceCurrency currency = PriceCurrency.INR;
    
    @Column(name = "recorded_at")
    private LocalDateTime recordedAt;
    
    @PrePersist
    protected void onCreate() {
        recordedAt = LocalDateTime.now();
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
