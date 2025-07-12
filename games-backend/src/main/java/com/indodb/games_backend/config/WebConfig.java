package com.indodb.games_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;

import java.time.Duration;

/**
 * Web Configuration for external API calls
 */
@Configuration
public class WebConfig {
    
    /**
     * RestTemplate bean for making HTTP requests to external APIs
     * Configured with timeouts to prevent hanging requests
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .connectTimeout(Duration.ofSeconds(10))   // Connection timeout
                .readTimeout(Duration.ofSeconds(15))      // Read timeout  
                .build();
    }
}
