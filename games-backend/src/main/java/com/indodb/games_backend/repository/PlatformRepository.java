package com.indodb.games_backend.repository;

import com.indodb.games_backend.model.Platform;
import com.indodb.games_backend.model.PlatformType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlatformRepository extends JpaRepository<Platform, Integer> {
    Optional<Platform> findByType(PlatformType type);

    Optional<Platform> findByNameIgnoreCase(String name);
}
