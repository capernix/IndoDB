package com.indodb.games_backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "top_steam_snapshot_runs", schema = "games")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TopSteamSnapshotRun {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "total_items", nullable = false)
    private Integer totalItems;

    @Column(name = "official_count", nullable = false)
    private Integer officialCount;

    @Column(name = "tail_count", nullable = false)
    private Integer tailCount;

    @Column(name = "validation_passed", nullable = false)
    private Boolean validationPassed;

    @Column(name = "message")
    private String message;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
