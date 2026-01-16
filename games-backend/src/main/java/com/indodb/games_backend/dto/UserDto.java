package com.indodb.games_backend.dto;

import com.indodb.games_backend.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Safe user representation without sensitive data (password hash)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    
    private UUID id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private String steamId;
    private String epicAccountId;
    private String gogUserId;
    private boolean emailVerified;
    private LocalDateTime createdAt;
    
    public static UserDto fromUser(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .steamId(user.getSteamId())
                .epicAccountId(user.getEpicAccountId())
                .gogUserId(user.getGogUserId())
                .emailVerified(user.isEmailVerified())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
