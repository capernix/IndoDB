package com.indodb.games_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "game_prices", schema = "games")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class GamePrice {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false)
    @EqualsAndHashCode.Include
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonBackReference
    private Game game;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "platform_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Platform platform;
    
    @Column(name = "current_price", precision = 10, scale = 2)
    private BigDecimal currentPrice;
    
    @Column(name = "original_price", precision = 10, scale = 2)
    private BigDecimal originalPrice;
    
    @Column(name = "discount_percentage")
    @Builder.Default
    private Integer discountPercentage = 0;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "currency")
    @Builder.Default
    private PriceCurrency currency = PriceCurrency.INR;
    
    @Column(name = "is_free")
    @Builder.Default
    private Boolean isFree = false;
    
    @Column(name = "is_on_sale")
    @Builder.Default
    private Boolean isOnSale = false;
    
    @Column(name = "sale_end_date")
    private LocalDateTime saleEndDate;
    
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "data_source", nullable = false)
    @Builder.Default
    private String dataSource = "SEEDED";

    @Column(name = "sync_status", nullable = false)
    @Builder.Default
    private String syncStatus = "SUCCESS";

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastUpdated = LocalDateTime.now();
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (dataSource == null || dataSource.isBlank()) {
            dataSource = "SEEDED";
        }
        if (syncStatus == null || syncStatus.isBlank()) {
            syncStatus = "SUCCESS";
        }
        calculateDiscountPercentage();
    }
    
    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
        calculateDiscountPercentage();
    }
    
    // Business Logic Methods
    public void calculateDiscountPercentage() {
        if (originalPrice != null && currentPrice != null && originalPrice.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discount = originalPrice.subtract(currentPrice);
            BigDecimal percentage = discount.divide(originalPrice, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
            this.discountPercentage = percentage.intValue();
            this.isOnSale = this.discountPercentage > 0;
        } else {
            this.discountPercentage = 0;
            this.isOnSale = false;
        }
    }
    
    public boolean isCurrentlyFree() {
        return isFree || (currentPrice != null && currentPrice.compareTo(BigDecimal.ZERO) == 0);
    }
    
    public boolean hasGoodDiscount() {
        return discountPercentage != null && discountPercentage >= 50; // 50%+ discount
    }
    
    public String getFormattedPrice() {
        if (isCurrentlyFree()) {
            return "Free";
        }
        if (currentPrice == null) {
            return "Price Not Available";
        }
        return switch (currency) {
            case INR -> "₹" + currentPrice;
            case USD -> "$" + currentPrice;
            case EUR -> "€" + currentPrice;
            case GBP -> "£" + currentPrice;
        };
    }
    
    public String getFormattedOriginalPrice() {
        if (originalPrice == null) {
            return null;
        }
        return switch (currency) {
            case INR -> "₹" + originalPrice;
            case USD -> "$" + originalPrice;
            case EUR -> "€" + originalPrice;
            case GBP -> "£" + originalPrice;
        };
    }
}
