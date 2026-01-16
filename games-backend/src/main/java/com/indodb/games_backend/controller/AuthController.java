package com.indodb.games_backend.controller;

import com.indodb.games_backend.dto.AuthResponse;
import com.indodb.games_backend.dto.LoginRequest;
import com.indodb.games_backend.dto.SignupRequest;
import com.indodb.games_backend.dto.UserDto;
import com.indodb.games_backend.model.User;
import com.indodb.games_backend.security.JwtService;
import com.indodb.games_backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller
 * Handles user registration, login, and current user info
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "Authentication", description = "🔐 User registration, login, and authentication")
public class AuthController {
    
    private final UserService userService;
    private final JwtService jwtService;
    
    @PostMapping("/signup")
    @Operation(summary = "Register new user", description = "Create a new user account with username, email, and password")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        log.info("📝 Signup request for: {}", request.getUsername());
        
        try {
            // Register user
            UserDto userDto = userService.registerUser(request);
            
            // Load user details for token generation
            UserDetails userDetails = userService.loadUserByUsername(userDto.getUsername());
            
            // Generate JWT token
            String token = jwtService.generateToken(userDetails);
            
            // Build response
            AuthResponse response = new AuthResponse(token, userDto);
            
            log.info("✅ User registered and authenticated: {}", userDto.getUsername());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Signup failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticate with email/username and password, returns JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("🔐 Login request for: {}", request.getEmailOrUsername());
        
        try {
            // Authenticate user
            User user = userService.authenticateUser(
                    request.getEmailOrUsername(),
                    request.getPassword()
            );
            
            // Generate JWT token
            String token = jwtService.generateToken(user);
            
            // Build response
            UserDto userDto = UserDto.fromUser(user);
            AuthResponse response = new AuthResponse(token, userDto);
            
            log.info("✅ User authenticated: {}", user.getUsername());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.warn("⚠️ Login failed for {}: {}", request.getEmailOrUsername(), e.getMessage());
            return ResponseEntity.status(401).build();
        }
    }
    
    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get currently authenticated user information (requires Bearer token)")
    public ResponseEntity<UserDto> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        
        try {
            User user = userService.getCurrentUser();
            UserDto userDto = UserDto.fromUser(user);
            
            log.debug("📋 Current user info requested: {}", user.getUsername());
            
            return ResponseEntity.ok(userDto);
            
        } catch (Exception e) {
            log.error("❌ Error getting current user: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
}
