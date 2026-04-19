package com.indodb.games_backend.controller;

import com.indodb.games_backend.service.ItadApiService;
import com.indodb.games_backend.service.ItadCatalogSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/itad")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "ITAD Integration", description = "IsThereAnyDeal lookup and multi-store price diagnostics")
public class ItadController {

    private final ItadApiService itadApiService;
    private final ItadCatalogSyncService itadCatalogSyncService;

    @GetMapping("/status")
    @Operation(summary = "Check ITAD configuration", description = "Shows whether an ITAD API key is configured")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(Map.of(
                "configured", itadApiService.isConfigured(),
                "message", itadApiService.isConfigured()
                        ? "ITAD API key is configured"
                        : "Set ITAD_API_KEY before live ITAD calls will run"
        ));
    }

    @GetMapping("/lookup/{steamAppId}")
    @Operation(summary = "Lookup ITAD ID by Steam app ID", description = "Maps a Steam app ID to an ITAD UUID")
    public ResponseEntity<Map<String, Object>> lookupBySteamAppId(@PathVariable String steamAppId) {
        if (!itadApiService.isConfigured()) {
            return ResponseEntity.status(503).body(Map.of(
                    "status", "NOT_CONFIGURED",
                    "message", "Set ITAD_API_KEY before live ITAD calls will run",
                    "steamAppId", steamAppId
            ));
        }

        String itadId = itadApiService.lookupItadIdBySteamId(steamAppId);
        if (itadId == null) {
            return ResponseEntity.status(404).body(Map.of(
                    "status", "NOT_FOUND",
                    "steamAppId", steamAppId
            ));
        }

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "steamAppId", steamAppId,
                "itadId", itadId
        ));
    }

    @PostMapping("/prices")
    @Operation(summary = "Fetch ITAD prices", description = "Fetches Epic/GOG prices for ITAD UUIDs")
    public ResponseEntity<Map<String, Object>> getPrices(@RequestBody List<String> itadIds) {
        if (!itadApiService.isConfigured()) {
            return ResponseEntity.status(503).body(Map.of(
                    "status", "NOT_CONFIGURED",
                    "message", "Set ITAD_API_KEY before live ITAD calls will run"
            ));
        }

        Map<String, Map<String, BigDecimal>> prices = itadApiService.getBatchPrices(itadIds);
        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "count", prices.size(),
                "prices", prices
        ));
    }

    @GetMapping("/test/{steamAppId}")
    @Operation(summary = "End-to-end ITAD smoke test", description = "Looks up an ITAD UUID from Steam app ID, then fetches Epic/GOG prices")
    public ResponseEntity<Map<String, Object>> testPipeline(@PathVariable String steamAppId) {
        if (!itadApiService.isConfigured()) {
            return ResponseEntity.status(503).body(Map.of(
                    "status", "NOT_CONFIGURED",
                    "message", "Set ITAD_API_KEY before live ITAD calls will run",
                    "steamAppId", steamAppId
            ));
        }

        log.info("Testing ITAD pipeline for Steam app ID {}", steamAppId);
        String itadId = itadApiService.lookupItadIdBySteamId(steamAppId);
        if (itadId == null) {
            return ResponseEntity.status(404).body(Map.of(
                    "status", "LOOKUP_NOT_FOUND",
                    "steamAppId", steamAppId
            ));
        }

        Map<String, Map<String, BigDecimal>> prices = itadApiService.getBatchPrices(List.of(itadId));
        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "steamAppId", steamAppId,
                "itadId", itadId,
                "platformPrices", prices.getOrDefault(itadId, Map.of())
        ));
    }

    @PostMapping("/sync/{steamAppId}")
    @Operation(summary = "Sync Epic/GOG prices into IndoDB", description = "Uses Steam app ID to find ITAD ID, then persists Epic/GOG INR prices")
    public ResponseEntity<Map<String, Object>> syncStorePrices(@PathVariable String steamAppId) {
        if (!itadApiService.isConfigured()) {
            return ResponseEntity.status(503).body(Map.of(
                    "status", "NOT_CONFIGURED",
                    "message", "Set ITAD_API_KEY before live ITAD calls will run",
                    "steamAppId", steamAppId
            ));
        }

        try {
            ItadCatalogSyncService.SyncResult result = itadCatalogSyncService.syncStorePricesForSteamApp(steamAppId);
            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "result", result
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of(
                    "status", "NOT_FOUND",
                    "message", e.getMessage(),
                    "steamAppId", steamAppId
            ));
        } catch (Exception e) {
            log.error("Error syncing ITAD prices for Steam app ID {}: {}", steamAppId, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "status", "ERROR",
                    "message", "Internal error while syncing ITAD prices",
                    "steamAppId", steamAppId
            ));
        }
    }
}
