package com.indodb.games_backend.controller;

import com.indodb.games_backend.service.TopSteamCatalogSourceService;
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
}
