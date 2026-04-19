package com.indodb.games_backend.service;

import com.indodb.games_backend.model.Game;
import com.indodb.games_backend.model.GamePrice;
import com.indodb.games_backend.model.Platform;
import com.indodb.games_backend.model.PlatformType;
import com.indodb.games_backend.model.PriceCurrency;
import com.indodb.games_backend.model.PriceHistory;
import com.indodb.games_backend.repository.GamePriceRepository;
import com.indodb.games_backend.repository.GameRepository;
import com.indodb.games_backend.repository.PlatformRepository;
import com.indodb.games_backend.repository.PriceHistoryRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItadCatalogSyncService {

    private final ItadApiService itadApiService;
    private final GameRepository gameRepository;
    private final GamePriceRepository gamePriceRepository;
    private final PlatformRepository platformRepository;
    private final PriceHistoryRepository priceHistoryRepository;

    @Transactional
    public SyncResult syncStorePricesForSteamApp(String steamAppId) {
        Long parsedSteamAppId = Long.parseLong(steamAppId);
        Game game = gameRepository.findBySteamAppIdAndIsActiveTrue(parsedSteamAppId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Game must exist in IndoDB before ITAD prices can be synced. Sync Steam first: " + steamAppId));

        String itadId = game.getItadId();
        boolean linkedItadId = false;

        if (itadId == null || itadId.isBlank()) {
            itadId = itadApiService.lookupItadIdBySteamId(steamAppId);
            if (itadId == null) {
                return SyncResult.builder()
                        .gameId(game.getId().toString())
                        .steamAppId(parsedSteamAppId)
                        .title(game.getTitle())
                        .itadId(null)
                        .linkedItadId(false)
                        .prices(List.of())
                        .message("ITAD did not return a game mapping for this Steam app id")
                        .build();
            }

            game.setItadId(itadId);
            gameRepository.save(game);
            linkedItadId = true;
        }

        Map<String, Map<String, ItadApiService.StorePrice>> pricesByGame =
                itadApiService.getBatchPriceDetails(List.of(itadId));
        Map<String, ItadApiService.StorePrice> platformPrices = pricesByGame.getOrDefault(itadId, Map.of());

        List<PlatformSyncResult> syncedPrices = new ArrayList<>();
        syncPlatformPrice(game, PlatformType.EPIC, platformPrices.get("EPIC")).ifPresent(syncedPrices::add);
        syncPlatformPrice(game, PlatformType.GOG, platformPrices.get("GOG")).ifPresent(syncedPrices::add);

        return SyncResult.builder()
                .gameId(game.getId().toString())
                .steamAppId(parsedSteamAppId)
                .title(game.getTitle())
                .itadId(itadId)
                .linkedItadId(linkedItadId)
                .prices(syncedPrices)
                .message(syncedPrices.isEmpty()
                        ? "ITAD lookup worked, but no Epic/GOG INR prices were returned"
                        : "ITAD prices synced")
                .build();
    }

    private java.util.Optional<PlatformSyncResult> syncPlatformPrice(
            Game game,
            PlatformType platformType,
            ItadApiService.StorePrice storePrice
    ) {
        if (storePrice == null) {
            return java.util.Optional.empty();
        }

        Platform platform = platformRepository.findByType(platformType)
                .orElseThrow(() -> new IllegalStateException(platformType + " platform row is missing"));

        GamePrice price = gamePriceRepository.findByGameIdAndPlatformId(game.getId(), platform.getId())
                .orElseGet(() -> GamePrice.builder()
                        .game(game)
                        .platform(platform)
                        .currency(PriceCurrency.INR)
                        .build());

        price.setCurrentPrice(storePrice.currentPrice());
        price.setOriginalPrice(storePrice.originalPrice());
        price.setDiscountPercentage(storePrice.discountPercentage());
        price.setCurrency(PriceCurrency.INR);
        price.setIsFree(storePrice.currentPrice().compareTo(BigDecimal.ZERO) == 0);
        price.setIsOnSale(storePrice.discountPercentage() != null && storePrice.discountPercentage() > 0);

        GamePrice savedPrice = gamePriceRepository.save(price);
        recordHistory(game, platform, savedPrice);

        log.info("Synced {} ITAD price for {}: {}", platform.getName(), game.getTitle(), savedPrice.getFormattedPrice());

        return java.util.Optional.of(PlatformSyncResult.builder()
                .platform(platform.getName())
                .currentPrice(savedPrice.getCurrentPrice())
                .originalPrice(savedPrice.getOriginalPrice())
                .discountPercentage(savedPrice.getDiscountPercentage())
                .currency(savedPrice.getCurrency().name())
                .build());
    }

    private void recordHistory(Game game, Platform platform, GamePrice price) {
        PriceHistory history = PriceHistory.builder()
                .game(game)
                .platformId(platform.getId())
                .price(price.getCurrentPrice())
                .originalPrice(price.getOriginalPrice())
                .discountPercentage(price.getDiscountPercentage())
                .currency(price.getCurrency())
                .build();

        priceHistoryRepository.save(history);
    }

    @Data
    @Builder
    public static class SyncResult {
        private String gameId;
        private Long steamAppId;
        private String title;
        private String itadId;
        private Boolean linkedItadId;
        private List<PlatformSyncResult> prices;
        private String message;
    }

    @Data
    @Builder
    public static class PlatformSyncResult {
        private String platform;
        private BigDecimal currentPrice;
        private BigDecimal originalPrice;
        private Integer discountPercentage;
        private String currency;
    }
}
