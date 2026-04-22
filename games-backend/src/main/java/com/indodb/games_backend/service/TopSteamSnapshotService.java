package com.indodb.games_backend.service;

import com.indodb.games_backend.model.TopSteamSnapshotItem;
import com.indodb.games_backend.model.TopSteamSnapshotRun;
import com.indodb.games_backend.repository.TopSteamSnapshotItemRepository;
import com.indodb.games_backend.repository.TopSteamSnapshotRunRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TopSteamSnapshotService {

    private final TopSteamCatalogSourceService topSteamCatalogSourceService;
    private final TopSteamSnapshotItemRepository topSteamSnapshotItemRepository;
    private final TopSteamSnapshotRunRepository topSteamSnapshotRunRepository;

    @Value("${top-sync.count:2000}")
    private int defaultSnapshotCount;

    @Transactional
    public SnapshotRefreshResult refreshSnapshot(LocalDate snapshotDate, int requestedCount) {
        int safeRequestedCount = Math.max(100, Math.min(requestedCount, 5000));
        LocalDate date = snapshotDate == null ? LocalDate.now() : snapshotDate;

        TopSteamCatalogSourceService.TopCatalogSnapshotResult sourceResult =
                topSteamCatalogSourceService.fetchTopCatalog(safeRequestedCount);

        if (!sourceResult.isValidationPassed()) {
            String message = "Validation failed: " + String.join(",", sourceResult.getValidationErrors());
            TopSteamSnapshotRun failedRun = TopSteamSnapshotRun.builder()
                    .snapshotDate(date)
                    .status("FAILED_VALIDATION")
                    .totalItems(sourceResult.getEntries().size())
                    .officialCount(sourceResult.getOfficialCount())
                    .tailCount(sourceResult.getTailCount())
                    .validationPassed(false)
                    .message(message)
                    .build();
            topSteamSnapshotRunRepository.save(failedRun);

            log.warn("Top snapshot refresh aborted for {}: {}", date, message);
            return SnapshotRefreshResult.builder()
                    .snapshotDate(date)
                    .requestedCount(safeRequestedCount)
                    .storedCount(0)
                    .officialCount(sourceResult.getOfficialCount())
                    .tailCount(sourceResult.getTailCount())
                    .status("FAILED_VALIDATION")
                    .validationPassed(false)
                    .message(message)
                    .build();
        }

        topSteamSnapshotItemRepository.deleteBySnapshotDate(date);

        LocalDateTime now = LocalDateTime.now();
        List<TopSteamSnapshotItem> entities = sourceResult.getEntries().stream()
                .map(e -> TopSteamSnapshotItem.builder()
                        .snapshotDate(date)
                        .steamAppId(e.steamAppId())
                        .rank(e.rank())
                        .tier(e.tier())
                        .source(e.source())
                        .fetchedAt(now)
                        .build())
                .toList();

        topSteamSnapshotItemRepository.saveAll(entities);

        String status = sourceResult.getOfficialCount() > 0 ? "SUCCESS" : "DEGRADED_SOURCE";
        String message = sourceResult.getOfficialCount() > 0
                ? "Snapshot refreshed from hybrid sources"
                : "Official source unavailable; SteamSpy-only snapshot";

        TopSteamSnapshotRun run = TopSteamSnapshotRun.builder()
                .snapshotDate(date)
                .status(status)
                .totalItems(entities.size())
                .officialCount(sourceResult.getOfficialCount())
                .tailCount(sourceResult.getTailCount())
                .validationPassed(true)
                .message(message)
                .build();
        topSteamSnapshotRunRepository.save(run);

        log.info("Top snapshot refreshed date={} requested={} stored={} official={} tail={} status={}",
                date, safeRequestedCount, entities.size(), sourceResult.getOfficialCount(), sourceResult.getTailCount(), status);

        return SnapshotRefreshResult.builder()
                .snapshotDate(date)
                .requestedCount(safeRequestedCount)
                .storedCount(entities.size())
                .officialCount(sourceResult.getOfficialCount())
                .tailCount(sourceResult.getTailCount())
                .status(status)
                .validationPassed(true)
                .message(message)
                .build();
    }

    @Transactional
    public SnapshotRefreshResult refreshTodaySnapshot() {
        return refreshSnapshot(LocalDate.now(), defaultSnapshotCount);
    }

    @Transactional(readOnly = true)
    public Optional<LocalDate> getLatestSnapshotDate() {
        return topSteamSnapshotItemRepository.findLatestSnapshotDate();
    }

    @Transactional(readOnly = true)
    public List<TopSteamSnapshotItem> getLatestSnapshotItems(int count) {
        int safeCount = Math.max(1, Math.min(count, 5000));
        Optional<LocalDate> latestDate = getLatestSnapshotDate();
        if (latestDate.isEmpty()) {
            return List.of();
        }
        return topSteamSnapshotItemRepository.findBySnapshotDateOrderByRankAsc(latestDate.get(), PageRequest.of(0, safeCount));
    }

    @Transactional(readOnly = true)
    public Optional<TopSteamSnapshotRun> getLatestRun() {
        return topSteamSnapshotRunRepository.findTopByOrderBySnapshotDateDescCreatedAtDesc();
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SnapshotRefreshResult {
        private LocalDate snapshotDate;
        private int requestedCount;
        private int storedCount;
        private int officialCount;
        private int tailCount;
        private String status;
        private boolean validationPassed;
        private String message;
    }
}
