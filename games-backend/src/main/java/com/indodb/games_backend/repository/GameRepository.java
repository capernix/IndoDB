package com.indodb.games_backend.repository;

import com.indodb.games_backend.model.Game;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GameRepository extends JpaRepository<Game, UUID> {
    
    // Basic operations
    Page<Game> findByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);
    
    // Search functionality
    @Query("SELECT g FROM Game g WHERE LOWER(g.title) LIKE LOWER(CONCAT('%', :title, '%')) AND g.isActive = true")
    Page<Game> findByTitleContainingIgnoreCase(@Param("title") String title, Pageable pageable);
    
    // Platform-specific lookups
    Optional<Game> findBySteamAppIdAndIsActiveTrue(Long steamAppId);
    Optional<Game> findByEpicCatalogItemIdAndIsActiveTrue(String epicCatalogItemId);
    Optional<Game> findByGogProductIdAndIsActiveTrue(Long gogProductId);
    
    // 🔥 TRENDING LOGIC - Your Core Feature
    
    // Most wishlisted games (trending by demand)
    @Query(value = """
        SELECT g.* FROM games.games g 
        LEFT JOIN users.wishlists w ON g.id = w.game_id AND w.is_active = true
        WHERE g.is_active = true
        GROUP BY g.id 
        ORDER BY
            CASE WHEN g.data_source IN ('STEAM_API', 'ITAD_API') THEN 0 ELSE 1 END,
            g.last_synced_at DESC NULLS LAST,
            COUNT(w.id) DESC,
            g.metacritic_score DESC NULLS LAST
        """, nativeQuery = true)
    List<Game> findTrendingByWishlists(Pageable pageable);
    
    // Most voted this month (hottest by community)
    @Query(value = """
        SELECT g.* FROM games.games g 
        LEFT JOIN users.user_votes v ON g.id = v.game_id 
        AND DATE_TRUNC('month', v.vote_month) = DATE_TRUNC('month', CURRENT_DATE)
        WHERE g.is_active = true
        GROUP BY g.id 
        ORDER BY
            CASE WHEN g.data_source IN ('STEAM_API', 'ITAD_API') THEN 0 ELSE 1 END,
            g.last_synced_at DESC NULLS LAST,
            COUNT(v.id) DESC,
            g.release_date DESC NULLS LAST
        """, nativeQuery = true)
    List<Game> findHottestByVotes(Pageable pageable);
    
    // Biggest price drops (deal alerts)
    @Query(value = """
        SELECT g.* FROM games.games g
        INNER JOIN (
            SELECT gp.game_id, MAX(gp.discount_percentage) AS best_discount
            FROM games.game_prices gp
            WHERE gp.is_on_sale = true
            GROUP BY gp.game_id
        ) d ON g.id = d.game_id
        WHERE g.is_active = true
        ORDER BY
            CASE WHEN g.data_source IN ('STEAM_API', 'ITAD_API') THEN 0 ELSE 1 END,
            g.last_synced_at DESC NULLS LAST,
            d.best_discount DESC,
            g.metacritic_score DESC NULLS LAST
        """, nativeQuery = true)
    List<Game> findBiggestDeals(Pageable pageable);
    
    // Free games (epic freebies, etc.)
    @Query(value = """
        SELECT g.* FROM games.games g
        INNER JOIN (
            SELECT gp.game_id, MAX(gp.last_updated) AS latest_free_at
            FROM games.game_prices gp
            WHERE gp.is_free = true
            GROUP BY gp.game_id
        ) f ON g.id = f.game_id
        WHERE g.is_active = true
        ORDER BY
            CASE WHEN g.data_source IN ('STEAM_API', 'ITAD_API') THEN 0 ELSE 1 END,
            g.last_synced_at DESC NULLS LAST,
            f.latest_free_at DESC
        """, nativeQuery = true)
    List<Game> findFreeGames(Pageable pageable);
    
    // Recently updated games (fresh data)
    @Query("SELECT g FROM Game g WHERE g.isActive = true ORDER BY g.updatedAt DESC")
    List<Game> findRecentlyUpdated(Pageable pageable);
    
    // Games by genre (for filtering)
    @Query(value = """
        SELECT * FROM games.games g 
        WHERE g.is_active = true 
        AND :genre = ANY(g.genres)
        ORDER BY g.created_at DESC
        """, nativeQuery = true)
    Page<Game> findByGenre(@Param("genre") String genre, Pageable pageable);
    
    // Advanced search with multiple filters
    @Query("""
        SELECT g FROM Game g WHERE 
        (:title IS NULL OR LOWER(g.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND 
        (:developer IS NULL OR LOWER(g.developer) LIKE LOWER(CONCAT('%', :developer, '%'))) AND 
        g.isActive = true
        """)
    Page<Game> searchGames(@Param("title") String title, 
                          @Param("developer") String developer, 
                          Pageable pageable);
    
    // Stats for homepage
    long countByIsActiveTrue();
}
