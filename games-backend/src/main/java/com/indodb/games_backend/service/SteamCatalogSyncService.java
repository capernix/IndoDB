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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SteamCatalogSyncService {

    private static final DateTimeFormatter STEAM_RELEASE_DATE = DateTimeFormatter.ofPattern("d MMM, yyyy");

    private final SteamApiService steamApiService;
    private final GameRepository gameRepository;
    private final GamePriceRepository gamePriceRepository;
    private final PlatformRepository platformRepository;
    private final PriceHistoryRepository priceHistoryRepository;

    @Transactional
    public SyncResult syncSteamGame(String appId) {
        Map<String, Object> details = steamApiService.getGameDetails(appId);
        if (details == null) {
            throw new IllegalArgumentException("Steam game not found or unavailable: " + appId);
        }

        Long steamAppId = Long.parseLong(appId);
        Game game = gameRepository.findBySteamAppIdAndIsActiveTrue(steamAppId)
                .orElseGet(() -> Game.builder()
                        .steamAppId(steamAppId)
                        .isActive(true)
                        .build());

        applyGameDetails(game, details);
        Game savedGame = gameRepository.save(game);

        Platform steam = platformRepository.findByType(PlatformType.STEAM)
                .orElseThrow(() -> new IllegalStateException("Steam platform row is missing"));

        GamePrice price = upsertSteamPrice(savedGame, steam, details);
        if (price.getCurrentPrice() != null) {
            recordHistory(savedGame, steam, price);
        }

        log.info("Synced Steam game {} ({}) with price {}", savedGame.getTitle(), appId, price.getFormattedPrice());

        return SyncResult.builder()
                .gameId(savedGame.getId().toString())
                .steamAppId(steamAppId)
                .title(savedGame.getTitle())
                .currentPrice(price.getCurrentPrice())
                .originalPrice(price.getOriginalPrice())
                .discountPercentage(price.getDiscountPercentage())
                .currency(price.getCurrency().name())
                .isFree(price.isCurrentlyFree())
                .build();
    }

    private void applyGameDetails(Game game, Map<String, Object> details) {
        game.setTitle(stringValue(details.get("name"), game.getTitle()));
        game.setDescription(cleanDescription(stringValue(details.get("description"), game.getDescription())));
        game.setShortDescription(cleanDescription(stringValue(details.get("shortDescription"), game.getShortDescription())));
        game.setHeaderImageUrl(stringValue(details.get("headerImageUrl"), game.getHeaderImageUrl()));
        game.setMetacriticScore(integerValue(details.get("metacriticScore"), game.getMetacriticScore()));
        game.setReleaseDate(parseReleaseDate(stringValue(details.get("releaseDate"), null), game.getReleaseDate()));

        List<String> developers = stringList(details.get("developers"));
        if (!developers.isEmpty()) {
            game.setDeveloper(String.join(", ", developers));
        }

        List<String> publishers = stringList(details.get("publishers"));
        if (!publishers.isEmpty()) {
            game.setPublisher(String.join(", ", publishers));
        }

        List<String> genres = stringList(details.get("genres"));
        if (!genres.isEmpty()) {
            game.setGenres(genres);
        }

        List<String> tags = stringList(details.get("tags"));
        if (!tags.isEmpty()) {
            game.setTags(tags);
        }
    }

    private GamePrice upsertSteamPrice(Game game, Platform steam, Map<String, Object> details) {
        GamePrice price = gamePriceRepository.findByGameIdAndPlatformId(game.getId(), steam.getId())
                .orElseGet(() -> GamePrice.builder()
                        .game(game)
                        .platform(steam)
                        .currency(PriceCurrency.INR)
                        .build());

        BigDecimal currentPrice = bigDecimalValue(details.get("currentPrice"), BigDecimal.ZERO);
        BigDecimal originalPrice = bigDecimalValue(details.get("originalPrice"), currentPrice);
        Integer discount = integerValue(details.get("discountPercent"), 0);
        Boolean isFree = booleanValue(details.get("isFree"), false);
        Boolean priceAvailable = booleanValue(details.get("priceAvailable"), false);

        price.setCurrentPrice(priceAvailable ? currentPrice : null);
        price.setOriginalPrice(originalPrice);
        price.setDiscountPercentage(discount);
        price.setCurrency(PriceCurrency.INR);
        price.setIsFree(isFree);
        price.setIsOnSale(discount != null && discount > 0);

        return gamePriceRepository.save(price);
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

    private String cleanDescription(String value) {
        if (value == null) {
            return null;
        }
        return value.replaceAll("<[^>]*>", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private LocalDate parseReleaseDate(String value, LocalDate fallback) {
        if (value == null || value.isBlank() || value.equalsIgnoreCase("Coming soon")) {
            return fallback;
        }
        try {
            return LocalDate.parse(value, STEAM_RELEASE_DATE);
        } catch (DateTimeParseException e) {
            log.debug("Could not parse Steam release date '{}'", value);
            return fallback;
        }
    }

    private String stringValue(Object value, String fallback) {
        return value == null ? fallback : value.toString();
    }

    private Integer integerValue(Object value, Integer fallback) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private BigDecimal bigDecimalValue(Object value, BigDecimal fallback) {
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        if (value == null) {
            return fallback;
        }
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private Boolean booleanValue(Object value, Boolean fallback) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value == null) {
            return fallback;
        }
        return Boolean.parseBoolean(value.toString());
    }

    @SuppressWarnings("unchecked")
    private List<String> stringList(Object value) {
        if (value instanceof List<?> list) {
            return list.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .toList();
        }
        return List.of();
    }

    @Data
    @Builder
    public static class SyncResult {
        private String gameId;
        private Long steamAppId;
        private String title;
        private BigDecimal currentPrice;
        private BigDecimal originalPrice;
        private Integer discountPercentage;
        private String currency;
        private Boolean isFree;
    }
}
