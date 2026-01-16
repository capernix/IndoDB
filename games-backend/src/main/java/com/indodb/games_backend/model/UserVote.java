package com.indodb.games_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * UserVote entity matching users.user_votes table
 * Tracks monthly game votes with uniqueness constraint per user per month
 */
@Entity
@Table(
    name = "user_votes", 
    schema = "users",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "game_id", "vote_month"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserVote {
    
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
    
    @Column(name = "vote_month", nullable = false)
    private LocalDate voteMonth;  // First day of the month
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (id == null) {
            id = UUID.randomUUID();
        }
        // Ensure vote_month is always the first day of the month
        if (voteMonth == null) {
            voteMonth = LocalDate.now().withDayOfMonth(1);
        } else {
            voteMonth = voteMonth.withDayOfMonth(1);
        }
    }
}
