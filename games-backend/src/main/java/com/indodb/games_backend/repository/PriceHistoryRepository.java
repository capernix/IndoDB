package com.indodb.games_backend.repository;

import com.indodb.games_backend.model.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, UUID> {
    
    /**
     * Find price history for a game ordered by most recent
     */
    List<PriceHistory> findByGameIdOrderByRecordedAtDesc(UUID gameId);
    
    /**
     * Find price history for a game and platform
     */
    @Query("SELECT ph FROM PriceHistory ph WHERE ph.game.id = :gameId AND ph.platformId = :platformId ORDER BY ph.recordedAt DESC")
    List<PriceHistory> findByGameIdAndPlatformId(@Param("gameId") UUID gameId, @Param("platformId") Integer platformId);
    
    /**
     * Find price changes (where price differs from previous record)
     */
    @Query(value = """
        SELECT ph.* FROM games.price_history ph
        WHERE ph.game_id = :gameId
        AND ph.platform_id = :platformId
        AND ph.recorded_at >= :since
        ORDER BY ph.recorded_at DESC
        """, nativeQuery = true)
    List<PriceHistory> findPriceChangesSince(
        @Param("gameId") UUID gameId, 
        @Param("platformId") Integer platformId,
        @Param("since") LocalDateTime since
    );

    @Query(value = """
        SELECT daily.day_date, daily.platform_type, daily.price
        FROM (
            SELECT
                DATE_TRUNC('day', ph.recorded_at) AS day_date,
                p.type AS platform_type,
                ph.price,
                ROW_NUMBER() OVER (
                    PARTITION BY DATE_TRUNC('day', ph.recorded_at), p.type
                    ORDER BY ph.recorded_at DESC
                ) AS rn
            FROM games.price_history ph
            JOIN games.platforms p ON p.id = ph.platform_id
            WHERE ph.game_id = :gameId
              AND ph.recorded_at >= :since
              AND ph.currency = 'INR'
        ) daily
        WHERE daily.rn = 1
        ORDER BY daily.day_date ASC
        """, nativeQuery = true)
    List<Object[]> findLatestDailySnapshotsByGameIdSince(
            @Param("gameId") UUID gameId,
            @Param("since") LocalDateTime since
    );
}
