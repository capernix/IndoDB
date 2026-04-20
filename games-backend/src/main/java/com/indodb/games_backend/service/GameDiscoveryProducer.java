package com.indodb.games_backend.service;

import com.indodb.games_backend.events.GameDiscoveryEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameDiscoveryProducer {

    public static final String GAME_DISCOVERY_TOPIC = "game-discovery";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(String steamAppId, String source, String priority, boolean includeItad) {
        GameDiscoveryEvent event = GameDiscoveryEvent.builder()
                .steamAppId(steamAppId)
                .source(source)
                .priority(priority)
                .includeItad(includeItad)
                .requestedAt(LocalDateTime.now())
                .build();

        kafkaTemplate.send(GAME_DISCOVERY_TOPIC, steamAppId, event);
        log.info("Queued game discovery event appId={} source={} priority={} includeItad={}",
                steamAppId, source, priority, includeItad);
    }
}
