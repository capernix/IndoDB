package com.indodb.games_backend.controller;

import com.indodb.games_backend.model.Game;
import com.indodb.games_backend.service.GameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // For frontend development
public class GameController {
    
    private final GameService gameService;
    
    // 🎮 BASIC GAME OPERATIONS
    
    @GetMapping
    public ResponseEntity<Page<Game>> getAllGames(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Game> games = gameService.getAllGames(pageable);
        
        log.info("Fetched {} games (page {}, size {})", games.getNumberOfElements(), page, size);
        return ResponseEntity.ok(games);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Game> getGameById(@PathVariable UUID id) {
        return gameService.getGameById(id)
                .map(game -> {
                    log.info("Found game: {}", game.getTitle());
                    return ResponseEntity.ok(game);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    // 🔍 SEARCH FUNCTIONALITY
    
    @GetMapping("/search")
    public ResponseEntity<Page<Game>> searchGames(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String developer,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Game> games = gameService.searchGames(title, developer, pageable);
        
        log.info("Search results: {} games found for title='{}', developer='{}'", 
                games.getTotalElements(), title, developer);
        
        return ResponseEntity.ok(games);
    }
    
    // 🔥 TRENDING & HOTTEST (Your Core Feature!)
    
    @GetMapping("/trending")
    public ResponseEntity<List<Game>> getTrendingGames(
            @RequestParam(defaultValue = "wishlisted") String type,
            @RequestParam(defaultValue = "10") int limit) {
        
        List<Game> trendingGames = gameService.getTrendingGames(type, limit);
        
        log.info("Fetched {} trending games (type: {}, limit: {})", 
                trendingGames.size(), type, limit);
        
        return ResponseEntity.ok(trendingGames);
    }
    
    @GetMapping("/hottest")
    public ResponseEntity<List<Game>> getHottestGames(
            @RequestParam(defaultValue = "10") int limit) {
        
        List<Game> hottestGames = gameService.getHottestGames(limit);
        
        log.info("Fetched {} hottest games by votes", hottestGames.size());
        return ResponseEntity.ok(hottestGames);
    }
    
    @GetMapping("/deals")
    public ResponseEntity<List<Game>> getBiggestDeals(
            @RequestParam(defaultValue = "10") int limit) {
        
        List<Game> deals = gameService.getBiggestDeals(limit);
        
        log.info("Fetched {} biggest deals", deals.size());
        return ResponseEntity.ok(deals);
    }
    
    @GetMapping("/free")
    public ResponseEntity<List<Game>> getFreeGames(
            @RequestParam(defaultValue = "10") int limit) {
        
        List<Game> freeGames = gameService.getFreeGames(limit);
        
        log.info("Fetched {} free games", freeGames.size());
        return ResponseEntity.ok(freeGames);
    }
    
    // 🏷️ FILTERING BY GENRE
    
    @GetMapping("/genre/{genre}")
    public ResponseEntity<Page<Game>> getGamesByGenre(
            @PathVariable String genre,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Game> games = gameService.getGamesByGenre(genre, pageable);
        
        log.info("Found {} games in genre: {}", games.getTotalElements(), genre);
        return ResponseEntity.ok(games);
    }
    
    // 📊 STATS FOR HOMEPAGE
    
    @GetMapping("/stats")
    public ResponseEntity<GameService.GameStats> getGameStats() {
        GameService.GameStats stats = gameService.getGameStats();
        return ResponseEntity.ok(stats);
    }
    
    // 🎯 PLATFORM-SPECIFIC LOOKUPS
    
    @GetMapping("/steam/{steamAppId}")
    public ResponseEntity<Game> getGameBySteamId(@PathVariable Long steamAppId) {
        return gameService.findBySteamAppId(steamAppId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/epic/{epicId}")
    public ResponseEntity<Game> getGameByEpicId(@PathVariable String epicId) {
        return gameService.findByEpicCatalogItemId(epicId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/gog/{gogId}")
    public ResponseEntity<Game> getGameByGogId(@PathVariable Long gogId) {
        return gameService.findByGogProductId(gogId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
