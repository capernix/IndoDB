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
@Table(name = "top_steam_snapshot_items", schema = "games")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TopSteamSnapshotItem {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "steam_app_id", nullable = false)
    private Long steamAppId;

    @Column(name = "rank", nullable = false)
    private Integer rank;

    @Column(name = "tier", nullable = false, length = 16)
    private String tier;

    @Column(name = "source", nullable = false, length = 32)
    private String source;

    @Column(name = "fetched_at", nullable = false)
    private LocalDateTime fetchedAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (fetchedAt == null) {
            fetchedAt = LocalDateTime.now();
        }
    }
}
