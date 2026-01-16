package com.indodb.games_backend.controller;

import com.indodb.games_backend.model.User;
import com.indodb.games_backend.model.UserVote;
import com.indodb.games_backend.service.UserService;
import com.indodb.games_backend.service.VoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Vote Controller
 * Handles monthly game voting and leaderboards
 */
@RestController
@RequestMapping("/api/votes")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "Voting", description = "🗳️ Monthly game voting and leaderboards")
public class VoteController {
    
    private final VoteService voteService;
    private final UserService userService;
    
    @PostMapping("/{gameId}")
    @Operation(summary = "Cast vote", description = "Vote for a game (one vote per user per month)")
    public ResponseEntity<UserVote> castVote(
            @PathVariable UUID gameId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            User user = userService.getCurrentUser();
            UserVote vote = voteService.castVote(user, gameId);
            
            return ResponseEntity.ok(vote);
            
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Vote failed: {}", e.getMessage());
            return ResponseEntity.status(409).build(); // 409 Conflict
        } catch (Exception e) {
            log.error("❌ Error casting vote: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
    
    @GetMapping("/leaderboard")
    @Operation(summary = "Get leaderboard", description = "Get current month's vote leaderboard (public)")
    public ResponseEntity<List<VoteService.VoteLeaderboardEntry>> getLeaderboard(
            @RequestParam(defaultValue = "10") int limit
    ) {
        try {
            List<VoteService.VoteLeaderboardEntry> leaderboard = voteService.getCurrentMonthLeaderboard(limit);
            
            log.info("📊 Leaderboard retrieved with {} entries", leaderboard.size());
            
            return ResponseEntity.ok(leaderboard);
            
        } catch (Exception e) {
            log.error("❌ Error fetching leaderboard: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
    
    @GetMapping("/my-votes")
    @Operation(summary = "Get vote history", description = "Get authenticated user's voting history")
    public ResponseEntity<List<UserVote>> getMyVotes(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.getCurrentUser();
            List<UserVote> votes = voteService.getUserVoteHistory(user.getId());
            
            log.info("📜 Vote history retrieved for user: {} ({} votes)", user.getUsername(), votes.size());
            
            return ResponseEntity.ok(votes);
            
        } catch (Exception e) {
            log.error("❌ Error fetching vote history: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
    
    @GetMapping("/status")
    @Operation(summary = "Get vote status", description = "Check if user has voted this month")
    public ResponseEntity<VoteStatusResponse> getVoteStatus(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.getCurrentUser();
            boolean hasVoted = voteService.hasUserVotedThisMonth(user.getId());
            
            VoteStatusResponse response = new VoteStatusResponse();
            response.setHasVotedThisMonth(hasVoted);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error checking vote status: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
    
    // Response DTO
    
    @lombok.Data
    public static class VoteStatusResponse {
        private boolean hasVotedThisMonth;
    }
}
