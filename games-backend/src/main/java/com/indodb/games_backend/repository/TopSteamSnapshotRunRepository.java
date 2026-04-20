package com.indodb.games_backend.repository;

import com.indodb.games_backend.model.TopSteamSnapshotRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TopSteamSnapshotRunRepository extends JpaRepository<TopSteamSnapshotRun, UUID> {

    Optional<TopSteamSnapshotRun> findTopByOrderBySnapshotDateDescCreatedAtDesc();

    Optional<TopSteamSnapshotRun> findBySnapshotDate(LocalDate snapshotDate);
}
