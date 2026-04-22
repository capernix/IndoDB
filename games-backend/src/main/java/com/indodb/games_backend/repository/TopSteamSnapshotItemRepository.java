package com.indodb.games_backend.repository;

import com.indodb.games_backend.model.TopSteamSnapshotItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TopSteamSnapshotItemRepository extends JpaRepository<TopSteamSnapshotItem, UUID> {

    @Query("SELECT MAX(i.snapshotDate) FROM TopSteamSnapshotItem i")
    Optional<LocalDate> findLatestSnapshotDate();

    List<TopSteamSnapshotItem> findBySnapshotDateOrderByRankAsc(LocalDate snapshotDate, Pageable pageable);

    long countBySnapshotDate(LocalDate snapshotDate);

    void deleteBySnapshotDate(LocalDate snapshotDate);

    @Query("SELECT i FROM TopSteamSnapshotItem i WHERE i.snapshotDate = :snapshotDate ORDER BY i.rank ASC")
    List<TopSteamSnapshotItem> findAllBySnapshotDateOrdered(@Param("snapshotDate") LocalDate snapshotDate);
}
