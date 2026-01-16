package com.indodb.games_backend.repository;

import com.indodb.games_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmailOrUsername(String email, String username);
    
    boolean existsByEmail(String email);
    
    boolean existsByUsername(String username);
    
    Optional<User> findBySteamId(String steamId);
    
    Optional<User> findByEpicAccountId(String epicAccountId);
    
    Optional<User> findByGogUserId(String gogUserId);
}
