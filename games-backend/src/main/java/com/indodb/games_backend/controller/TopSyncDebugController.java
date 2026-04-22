package com.indodb.games_backend.controller;

import com.indodb.games_backend.service.TopSteamCatalogSourceService;
import com.indodb.games_backend.service.TopSteamSnapshotService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/top-sync")
@RequiredArgsConstructor
public class TopSyncDebugController {

    private final TopSteamCatalogSourceService topSteamCatalogSourceService;
    private final TopSteamSnapshotService topSteamSnapshotService;

    @GetMapping("/preview")
    @Operation(summary = "Preview top catalog sources", description = "Debug endpoint for validating hybrid source fetch and schema checks")
    public ResponseEntity<Map<String, Object>> previewTopCatalog(
            @RequestParam(defaultValue = "2000") int count
    ) {
        TopSteamCatalogSourceService.TopCatalogSnapshotResult result = topSteamCatalogSourceService.fetchTopCatalog(count);

        return ResponseEntity.ok(Map.of(
                "requestedCount", result.getRequestedCount(),
                "fetchedCount", result.getEntries().size(),
                "officialCount", result.getOfficialCount(),
                "tailCount", result.getTailCount(),
                "validationPassed", result.isValidationPassed(),
                "validationErrors", result.getValidationErrors(),
                "sampleAppIds", result.getEntries().stream().limit(20).map(TopSteamCatalogSourceService.TopCatalogEntry::steamAppId).toList()
        ));
    }

    @GetMapping("/snapshot/refresh")
    @Operation(summary = "Refresh and persist snapshot", description = "Debug endpoint to run snapshot persistence and inspect run metadata")
    public ResponseEntity<Map<String, Object>> refreshSnapshot(
            @RequestParam(defaultValue = "2000") int count
    ) {
        TopSteamSnapshotService.SnapshotRefreshResult result = topSteamSnapshotService.refreshSnapshot(null, count);
        return ResponseEntity.ok(Map.of(
                "snapshotDate", result.getSnapshotDate(),
                "requestedCount", result.getRequestedCount(),
                "storedCount", result.getStoredCount(),
                "officialCount", result.getOfficialCount(),
                "tailCount", result.getTailCount(),
                "status", result.getStatus(),
                "validationPassed", result.isValidationPassed(),
                "message", result.getMessage()
        ));
    }

    @GetMapping("/snapshot/latest")
    @Operation(summary = "Inspect latest stored snapshot", description = "Returns latest snapshot date, latest run metadata, and sample app IDs")
    public ResponseEntity<Map<String, Object>> getLatestSnapshot(
            @RequestParam(defaultValue = "20") int limit
    ) {
        var latestDate = topSteamSnapshotService.getLatestSnapshotDate();
        var latestRun = topSteamSnapshotService.getLatestRun();
        var items = topSteamSnapshotService.getLatestSnapshotItems(limit);

        return ResponseEntity.ok(Map.of(
                "latestSnapshotDate", latestDate.orElse(null),
                "latestRunStatus", latestRun.map(r -> r.getStatus()).orElse("NONE"),
                "latestRunMessage", latestRun.map(r -> r.getMessage()).orElse("No snapshot run yet"),
                "sampleAppIds", items.stream().map(i -> i.getSteamAppId()).toList()
        ));
    }
}
