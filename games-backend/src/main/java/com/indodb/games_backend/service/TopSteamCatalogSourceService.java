package com.indodb.games_backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class TopSteamCatalogSourceService {

    private static final String TIER_OFFICIAL_TOP = "OFFICIAL_TOP";
    private static final String TIER_STEAMSPY_TAIL = "STEAMSPY_TAIL";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${top-sync.sources.official.enabled:true}")
    private boolean officialEnabled;

    @Value("${top-sync.sources.official.url:https://api.steampowered.com/ISteamChartsService/GetMostPlayedGames/v1/?key=%s}")
    private String officialUrlTemplate;

    @Value("${top-sync.sources.steamspy.url:https://steamspy.com/api.php?request=all&page=%d}")
    private String steamSpyUrlTemplate;

    @Value("${top-sync.sources.official.count:100}")
    private int officialCount;

    @Value("${top-sync.validation.minimum-items:1500}")
    private int minimumItems;

    @Value("${steam.api.key:}")
    private String steamApiKey;

    public TopCatalogSnapshotResult fetchTopCatalog(int requestedCount) {
        int safeRequestedCount = Math.max(100, Math.min(requestedCount, 5000));
        log.info("Fetching top Steam catalog snapshot targetCount={}", safeRequestedCount);

        List<TopCatalogEntry> officialTop = fetchOfficialTop();
        int remaining = Math.max(0, safeRequestedCount - officialTop.size());
        List<TopCatalogEntry> steamSpyTail = fetchSteamSpyTail(remaining, officialTop);

        Map<Long, TopCatalogEntry> merged = new LinkedHashMap<>();
        for (TopCatalogEntry entry : officialTop) {
            merged.put(entry.steamAppId(), entry);
        }

        int rankCursor = merged.size() + 1;
        for (TopCatalogEntry tailEntry : steamSpyTail) {
            if (merged.containsKey(tailEntry.steamAppId())) {
                continue;
            }
            TopCatalogEntry rankedTail = new TopCatalogEntry(
                    tailEntry.steamAppId(),
                    rankCursor,
                    TIER_STEAMSPY_TAIL,
                    tailEntry.source(),
                    tailEntry.snapshotAt()
            );
            merged.put(rankedTail.steamAppId(), rankedTail);
            rankCursor++;
            if (merged.size() >= safeRequestedCount) {
                break;
            }
        }

        List<TopCatalogEntry> entries = merged.values().stream()
                .sorted(Comparator.comparingInt(TopCatalogEntry::rank))
                .toList();

        List<String> validationErrors = validate(entries, safeRequestedCount);
        boolean validationPassed = validationErrors.isEmpty();
        if (!validationPassed) {
            log.error("Top catalog validation failed errors={}", validationErrors);
        }

        return TopCatalogSnapshotResult.builder()
                .requestedCount(safeRequestedCount)
                .entries(entries)
                .officialCount((int) entries.stream().filter(e -> TIER_OFFICIAL_TOP.equals(e.tier())).count())
                .tailCount((int) entries.stream().filter(e -> TIER_STEAMSPY_TAIL.equals(e.tier())).count())
                .validationPassed(validationPassed)
                .validationErrors(validationErrors)
                .build();
    }

    private List<TopCatalogEntry> fetchOfficialTop() {
        if (!officialEnabled) {
            return List.of();
        }
        if (steamApiKey == null || steamApiKey.isBlank() || steamApiKey.startsWith("your_")) {
            log.warn("Steam API key missing; skipping official top source");
            return List.of();
        }

        String url = String.format(officialUrlTemplate, steamApiKey);
        try {
            String response = restTemplate.getForObject(url, String.class);
            if (response == null || response.isBlank()) {
                return List.of();
            }

            JsonNode root = objectMapper.readTree(response);
            JsonNode ranks = root.path("response").path("ranks");
            if (!ranks.isArray()) {
                log.warn("Official top response missing response.ranks array");
                return List.of();
            }

            List<TopCatalogEntry> result = new ArrayList<>();
            int rank = 1;
            for (JsonNode row : ranks) {
                long appId = row.path("appid").asLong(0L);
                if (appId <= 0) {
                    continue;
                }
                result.add(new TopCatalogEntry(
                        appId,
                        rank,
                        TIER_OFFICIAL_TOP,
                        "STEAM_CHARTS",
                        LocalDateTime.now()
                ));
                rank++;
                if (result.size() >= officialCount) {
                    break;
                }
            }

            log.info("Fetched {} official top entries from Steam charts", result.size());
            return result;
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.FORBIDDEN || e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                log.warn("Official top source rejected Steam key: {}", e.getStatusCode());
                return List.of();
            }
            log.error("Official top source failed with status={}", e.getStatusCode());
            return List.of();
        } catch (Exception e) {
            log.error("Failed to fetch official top source: {}", e.getMessage());
            return List.of();
        }
    }

    private List<TopCatalogEntry> fetchSteamSpyTail(int needed, List<TopCatalogEntry> alreadyCollected) {
        if (needed <= 0) {
            return List.of();
        }

        Map<Long, TopCatalogEntry> deduped = new LinkedHashMap<>();
        alreadyCollected.forEach(e -> deduped.put(e.steamAppId(), e));

        List<TopCatalogEntry> tail = new ArrayList<>();
        int page = 0;
        while (tail.size() < needed && page < 5) {
            String url = String.format(steamSpyUrlTemplate, page);
            try {
                String response = restTemplate.getForObject(url, String.class);
                if (response == null || response.isBlank()) {
                    page++;
                    continue;
                }

                JsonNode root = objectMapper.readTree(response);
                if (!root.isObject()) {
                    page++;
                    continue;
                }

                for (Map.Entry<String, JsonNode> node : iterable(root.fields())) {
                    long appId;
                    try {
                        appId = Long.parseLong(node.getKey());
                    } catch (NumberFormatException ignore) {
                        continue;
                    }
                    if (appId <= 0 || deduped.containsKey(appId)) {
                        continue;
                    }
                    TopCatalogEntry entry = new TopCatalogEntry(
                            appId,
                            0,
                            TIER_STEAMSPY_TAIL,
                            "STEAMSPY_ALL",
                            LocalDateTime.now()
                    );
                    deduped.put(appId, entry);
                    tail.add(entry);
                    if (tail.size() >= needed) {
                        break;
                    }
                }
            } catch (Exception e) {
                log.warn("SteamSpy fetch failed page={} error={}", page, e.getMessage());
            }
            page++;
        }

        log.info("Fetched {} tail entries from SteamSpy", tail.size());
        return tail;
    }

    private List<String> validate(List<TopCatalogEntry> entries, int requestedCount) {
        List<String> errors = new ArrayList<>();

        if (entries.size() < Math.min(requestedCount, minimumItems)) {
            errors.add("INSUFFICIENT_ITEMS:" + entries.size());
        }

        long invalidIds = entries.stream().filter(e -> e.steamAppId() == null || e.steamAppId() <= 0).count();
        if (invalidIds > 0) {
            errors.add("INVALID_IDS:" + invalidIds);
        }

        long nullTiers = entries.stream().filter(e -> e.tier() == null || e.tier().isBlank()).count();
        if (nullTiers > 0) {
            errors.add("INVALID_TIERS:" + nullTiers);
        }

        return errors;
    }

    private static <T> Iterable<T> iterable(java.util.Iterator<T> iterator) {
        return () -> iterator;
    }

    public record TopCatalogEntry(
            Long steamAppId,
            Integer rank,
            String tier,
            String source,
            LocalDateTime snapshotAt
    ) {
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TopCatalogSnapshotResult {
        private int requestedCount;
        private List<TopCatalogEntry> entries;
        private int officialCount;
        private int tailCount;
        private boolean validationPassed;
        private List<String> validationErrors;
    }
}
