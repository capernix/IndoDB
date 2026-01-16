package com.indodb.games_backend.service;

import com.indodb.games_backend.dto.SignupRequest;
import com.indodb.games_backend.dto.UserDto;
import com.indodb.games_backend.model.User;
import com.indodb.games_backend.model.UserRole;
import com.indodb.games_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * User service for authentication and user management
 * Implements UserDetailsService for Spring Security
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * Load user by username for Spring Security
     * Supports both username and email
     */
    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        return userRepository.findByEmailOrUsername(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + usernameOrEmail));
    }
    
    /**
     * Register a new user
     */
    @Transactional
    public UserDto registerUser(SignupRequest request) {
        log.info("📝 Registering new user: {}", request.getUsername());
        
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + request.getEmail());
        }
        
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already taken: " + request.getUsername());
        }
        
        // Create new user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(UserRole.USER)
                .emailVerified(false)
                .isActive(true)
                .build();
        
        User savedUser = userRepository.save(user);
        
        log.info("✅ User registered successfully: {}", savedUser.getUsername());
        
        return UserDto.fromUser(savedUser);
    }
    
    /**
     * Authenticate user with email/username and password
     */
    public User authenticateUser(String emailOrUsername, String password) {
        log.info("🔐 Authenticating user: {}", emailOrUsername);
        
        User user = userRepository.findByEmailOrUsername(emailOrUsername, emailOrUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
        
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            log.warn("⚠️ Invalid password for user: {}", emailOrUsername);
            throw new IllegalArgumentException("Invalid credentials");
        }
        
        if (!user.isActive()) {
            log.warn("⚠️ Inactive user attempted login: {}", emailOrUsername);
            throw new IllegalArgumentException("Account is inactive");
        }
        
        // Update last login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        
        log.info("✅ User authenticated successfully: {}", user.getUsername());
        
        return user;
    }
    
    /**
     * Get user by ID
     */
    public Optional<User> getUserById(String userId) {
        try {
            return userRepository.findById(java.util.UUID.fromString(userId));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
    
    /**
     * Get current authenticated user from security context
     */
    public User getCurrentUser() {
        org.springframework.security.core.Authentication authentication = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user");
        }
        
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
