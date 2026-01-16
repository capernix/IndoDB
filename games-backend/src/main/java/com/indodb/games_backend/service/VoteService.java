package com.indodb.games_backend.service;

import com.indodb.games_backend.model.Game;
import com.indodb.games_backend.model.User;
import com.indodb.games_backend.model.UserVote;
import com.indodb.games_backend.repository.GameRepository;
import com.indodb.games_backend.repository.UserVoteRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Vote service for monthly game voting
 * Enforces one vote per user per month
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class VoteService {
    
    private final UserVoteRepository userVoteRepository;
    private final GameRepository gameRepository;
    
    /**
     * Cast vote for a game (one vote per user per month)
     */
    @Transactional
    public UserVote castVote(User user, UUID gameId) {
        LocalDate currentMonth = LocalDate.now().withDayOfMonth(1);
        
        log.info("🗳️ User {} voting for game {} in month {}", user.getUsername(), gameId, currentMonth);
        
        // Check if user already voted this month
        if (userVoteRepository.existsByUserIdAndGameIdAndVoteMonth(user.getId(), gameId, currentMonth)) {
            throw new IllegalArgumentException("You have already voted for this game this month");
        }
        
        // Check if user voted for a different game this month
        userVoteRepository.findByUserIdAndVoteMonth(user.getId(), currentMonth)
                .ifPresent(existingVote -> {
                    throw new IllegalArgumentException("You have already voted for another game this month");
                });
        
        // Verify game exists
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found: " + gameId));
        
        // Create vote
        UserVote vote = UserVote.builder()
                .user(user)
                .game(game)
                .voteMonth(currentMonth)
                .build();
        
        UserVote saved = userVoteRepository.save(vote);
        
        log.info("✅ Vote cast successfully for game: {}", game.getTitle());
        
        return saved;
    }
    
    /**
     * Get current month's leaderboard
     */
    public List<VoteLeaderboardEntry> getCurrentMonthLeaderboard(int limit) {
        LocalDate currentMonth = LocalDate.now().withDayOfMonth(1);
        return getMonthLeaderboard(currentMonth, limit);
    }
    
    /**
     * Get leaderboard for specific month
     */
    public List<VoteLeaderboardEntry> getMonthLeaderboard(LocalDate month, int limit) {
        LocalDate normalizedMonth = month.withDayOfMonth(1);
        
        log.debug("📊 Fetching vote leaderboard for month: {}", normalizedMonth);
        
        List<Object[]> results = userVoteRepository.findMonthlyLeaderboard(normalizedMonth, limit);
        
        List<VoteLeaderboardEntry> leaderboard = new ArrayList<>();
        for (Object[] row : results) {
            VoteLeaderboardEntry entry = new VoteLeaderboardEntry();
            entry.setGameId(UUID.fromString(row[0].toString()));
            entry.setGameTitle((String) row[1]);
            entry.setVoteCount(((Number) row[2]).longValue());
            leaderboard.add(entry);
        }
        
        log.debug("✅ Found {} games in leaderboard", leaderboard.size());
        
        return leaderboard;
    }
    
    /**
     * Get user's voting history
     */
    public List<UserVote> getUserVoteHistory(UUID userId) {
        log.debug("📜 Fetching vote history for user: {}", userId);
        return userVoteRepository.findByUserIdOrderByVoteMonthDesc(userId);
    }
    
    /**
     * Check if user has voted this month
     */
    public boolean hasUserVotedThisMonth(UUID userId) {
        LocalDate currentMonth = LocalDate.now().withDayOfMonth(1);
        return userVoteRepository.findByUserIdAndVoteMonth(userId, currentMonth).isPresent();
    }
    
    /**
     * Get vote count for a game in current month
     */
    public long getGameVoteCount(UUID gameId) {
        LocalDate currentMonth = LocalDate.now().withDayOfMonth(1);
        return userVoteRepository.countByGameIdAndVoteMonth(gameId, currentMonth);
    }
    
    /**
     * Leaderboard entry DTO
     */
    @Data
    public static class VoteLeaderboardEntry {
        private UUID gameId;
        private String gameTitle;
        private long voteCount;
    }
}
