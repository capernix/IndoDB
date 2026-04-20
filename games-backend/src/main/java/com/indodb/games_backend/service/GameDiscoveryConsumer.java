package com.indodb.games_backend.service;

import com.indodb.games_backend.events.GameDiscoveryEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameDiscoveryConsumer {

    private final SteamCatalogSyncService steamCatalogSyncService;
    private final ItadCatalogSyncService itadCatalogSyncService;
    private final ItadApiService itadApiService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = GameDiscoveryProducer.GAME_DISCOVERY_TOPIC, groupId = "indodb-game-discovery")
    public void consume(ConsumerRecord<String, Object> record) {
        GameDiscoveryEvent event = objectMapper.convertValue(record.value(), GameDiscoveryEvent.class);
        if (event == null || event.getSteamAppId() == null || event.getSteamAppId().isBlank()) {
            log.warn("Skipping invalid game discovery event: {}", record.value());
            return;
        }

        String appId = event.getSteamAppId().trim();
        log.info("Processing discovery event appId={} source={} priority={}",
                appId, event.getSource(), event.getPriority());

        try {
            steamCatalogSyncService.syncSteamGame(appId);
        } catch (Exception e) {
            log.error("Steam sync failed for appId={} from source={}: {}",
                    appId, event.getSource(), e.getMessage(), e);
            return;
        }

        if (!event.isIncludeItad()) {
            return;
        }

        if (!itadApiService.isConfigured()) {
            log.info("Skipping ITAD sync for appId={} because ITAD API key is not configured", appId);
            return;
        }

        try {
            itadCatalogSyncService.syncStorePricesForSteamApp(appId);
        } catch (Exception e) {
            log.warn("ITAD sync failed for appId={}: {}", appId, e.getMessage());
        }
    }
}
