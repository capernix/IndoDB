package com.indodb.games_backend.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceChangeEvent {
    
    private String gameTitle;
    private BigDecimal oldPrice;
    private BigDecimal newPrice;
    private String currency;
    private String platform;
    private LocalDateTime timestamp;
    
    // Constructor for easy testing
    public PriceChangeEvent(String gameTitle, BigDecimal oldPrice, BigDecimal newPrice) {
        this.gameTitle = gameTitle;
        this.oldPrice = oldPrice;
        this.newPrice = newPrice;
        this.currency = "INR";
        this.platform = "Steam";
        this.timestamp = LocalDateTime.now();
    }
    
    // Utility methods
    public BigDecimal getDiscountAmount() {
        return oldPrice.subtract(newPrice);
    }
    
    public BigDecimal getDiscountPercentage() {
        if (oldPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return getDiscountAmount()
                .multiply(BigDecimal.valueOf(100))
                .divide(oldPrice, 2, RoundingMode.HALF_UP);
    }
    
    public boolean isPriceDrop() {
        return newPrice.compareTo(oldPrice) < 0;
    }
    
    public boolean isSignificantDrop() {
        return isPriceDrop() && getDiscountPercentage().compareTo(BigDecimal.valueOf(20)) >= 0;
    }
}
