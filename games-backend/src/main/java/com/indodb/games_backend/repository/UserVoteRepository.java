package com.indodb.games_backend.repository;

import com.indodb.games_backend.model.UserVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserVoteRepository extends JpaRepository<UserVote, UUID> {
    
    /**
     * Find user's vote for a specific month
     */
    Optional<UserVote> findByUserIdAndVoteMonth(UUID userId, LocalDate voteMonth);
    
    /**
     * Check if user already voted for a game in a specific month
     */
    boolean existsByUserIdAndGameIdAndVoteMonth(UUID userId, UUID gameId, LocalDate voteMonth);
    
    /**
     * Get user's voting history
     */
    List<UserVote> findByUserIdOrderByVoteMonthDesc(UUID userId);
    
    /**
     * Count votes for a game in a specific month
     */
    @Query("SELECT COUNT(v) FROM UserVote v WHERE v.game.id = :gameId AND v.voteMonth = :voteMonth")
    long countByGameIdAndVoteMonth(@Param("gameId") UUID gameId, @Param("voteMonth") LocalDate voteMonth);
    
    /**
     * Get leaderboard for current month (top voted games)
     */
    @Query(value = """
        SELECT g.id, g.title, COUNT(v.id) as vote_count
        FROM users.user_votes v
        INNER JOIN games.games g ON v.game_id = g.id
        WHERE v.vote_month = :voteMonth
        GROUP BY g.id, g.title
        ORDER BY vote_count DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findMonthlyLeaderboard(@Param("voteMonth") LocalDate voteMonth, @Param("limit") int limit);
    
    /**
     * Get all votes for a specific month
     */
    List<UserVote> findByVoteMonth(LocalDate voteMonth);
}
