package com.indodb.games_backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "games", schema = "games")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Game {
    
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false)
    @EqualsAndHashCode.Include
    private UUID id;
    
    @Column(name = "title", nullable = false)
    private String title;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "short_description", columnDefinition = "TEXT")
    private String shortDescription;
    
    @Column(name = "developer")
    private String developer;
    
    @Column(name = "publisher")
    private String publisher;
    
    @Column(name = "release_date")
    private LocalDate releaseDate;
    
    // PostgreSQL arrays mapped to Java Lists
    @Column(name = "genres")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private List<String> genres;
    
    @Column(name = "tags")
    @JdbcTypeCode(SqlTypes.ARRAY) 
    private List<String> tags;
    
    @Column(name = "metacritic_score")
    private Integer metacriticScore;
    
    @Column(name = "steam_app_id")
    private Long steamAppId;
    
    @Column(name = "epic_catalog_item_id")
    private String epicCatalogItemId;
    
    @Column(name = "gog_product_id")
    private Long gogProductId;
    
    @Column(name = "header_image_url", length = 500)
    private String headerImageUrl;
    
    @Column(name = "screenshots")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private List<String> screenshots;
    
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Helper methods
    public boolean isAvailableOnPlatform(PlatformType platformType) {
        return switch (platformType) {
            case STEAM -> steamAppId != null;
            case EPIC -> epicCatalogItemId != null && !epicCatalogItemId.isEmpty();
            case GOG -> gogProductId != null;
            case OTHER -> true;
        };
    }
    
    public String getPlatformSpecificId(PlatformType platformType) {
        return switch (platformType) {
            case STEAM -> steamAppId != null ? steamAppId.toString() : null;
            case EPIC -> epicCatalogItemId;
            case GOG -> gogProductId != null ? gogProductId.toString() : null;
            case OTHER -> null;
        };
    }
}
