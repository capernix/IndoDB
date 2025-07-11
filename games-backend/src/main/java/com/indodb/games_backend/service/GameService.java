package com.indodb.games_backend.service;

import com.indodb.games_backend.model.Game;
import com.indodb.games_backend.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GameService {
    
    private final GameRepository gameRepository;
    
    // 🎮 CORE GAME OPERATIONS
    
    public Page<Game> getAllGames(Pageable pageable) {
        return gameRepository.findByIsActiveTrueOrderByCreatedAtDesc(pageable);
    }
    
    public Optional<Game> getGameById(UUID id) {
        return gameRepository.findById(id);
    }
    
    public Page<Game> searchGames(String title, String developer, Pageable pageable) {
        return gameRepository.searchGames(title, developer, pageable);
    }
    
    public Page<Game> searchByTitle(String title, Pageable pageable) {
        return gameRepository.findByTitleContainingIgnoreCase(title, pageable);
    }
    
    // 🔥 TRENDING & HOTTEST LOGIC (Your Core Feature!)
    
    public List<Game> getTrendingGames(String trendingType, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        
        return switch (trendingType.toLowerCase()) {
            case "wishlisted", "trending" -> {
                log.info("Fetching trending games by wishlists (limit: {})", limit);
                yield gameRepository.findTrendingByWishlists(pageable);
            }
            case "voted", "hottest" -> {
                log.info("Fetching hottest games by votes (limit: {})", limit);
                yield gameRepository.findHottestByVotes(pageable);
            }
            case "deals", "discounted" -> {
                log.info("Fetching biggest deals (limit: {})", limit);
                yield gameRepository.findBiggestDeals(pageable);
            }
            case "free" -> {
                log.info("Fetching free games (limit: {})", limit);
                yield gameRepository.findFreeGames(pageable);
            }
            case "recent", "updated" -> {
                log.info("Fetching recently updated games (limit: {})", limit);
                yield gameRepository.findRecentlyUpdated(pageable);
            }
            default -> {
                log.warn("Unknown trending type: {}, defaulting to wishlisted", trendingType);
                yield gameRepository.findTrendingByWishlists(pageable);
            }
        };
    }
    
    // Convenience methods for specific trending types
    public List<Game> getMostWishlistedGames(int limit) {
        return getTrendingGames("wishlisted", limit);
    }
    
    public List<Game> getHottestGames(int limit) {
        return getTrendingGames("voted", limit);
    }
    
    public List<Game> getBiggestDeals(int limit) {
        return getTrendingGames("deals", limit);
    }
    
    public List<Game> getFreeGames(int limit) {
        return getTrendingGames("free", limit);
    }
    
    // 📊 HOMEPAGE STATS
    public GameStats getGameStats() {
        long totalGames = gameRepository.countByIsActiveTrue();
        
        return GameStats.builder()
                .totalGames(totalGames)
                .trendingGamesCount(Math.min(10, (int) totalGames))
                .build();
    }
    
    // 🎯 PLATFORM-SPECIFIC LOOKUPS (for API integrations later)
    public Optional<Game> findBySteamAppId(Long steamAppId) {
        return gameRepository.findBySteamAppIdAndIsActiveTrue(steamAppId);
    }
    
    public Optional<Game> findByEpicCatalogItemId(String epicId) {
        return gameRepository.findByEpicCatalogItemIdAndIsActiveTrue(epicId);
    }
    
    public Optional<Game> findByGogProductId(Long gogId) {
        return gameRepository.findByGogProductIdAndIsActiveTrue(gogId);
    }
    
    // 🏷️ GENRE FILTERING
    public Page<Game> getGamesByGenre(String genre, Pageable pageable) {
        return gameRepository.findByGenre(genre, pageable);
    }
    
    // 💾 CRUD OPERATIONS (for admin/API integrations)
    @Transactional
    public Game saveGame(Game game) {
        log.info("Saving game: {}", game.getTitle());
        return gameRepository.save(game);
    }
    
    @Transactional
    public void deleteGame(UUID id) {
        gameRepository.findById(id).ifPresent(game -> {
            game.setIsActive(false);
            gameRepository.save(game);
            log.info("Soft deleted game: {}", game.getTitle());
        });
    }
    
    // 📈 STATS INNER CLASS
    @lombok.Data
    @lombok.Builder
    public static class GameStats {
        private long totalGames;
        private int trendingGamesCount;
    }
}
