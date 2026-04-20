package com.indodb.games_backend.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameDiscoveryEvent {
    private String steamAppId;
    private String source;
    private String priority;
    private boolean includeItad;
    private LocalDateTime requestedAt;
}
