package com.indodb.games_backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for IndiaDB Games API documentation.
 * 
 * Access the interactive API documentation at:
 * - Swagger UI: http://localhost:8081/swagger-ui.html
 * - OpenAPI JSON: http://localhost:8081/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI indiaDbGamesOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("IndiaDB Games API")
                        .description("""
                                **🎮 Real-Time Gaming Intelligence Platform for India**
                                
                                A comprehensive **read-only API** for gaming price monitoring, deal discovery, and market intelligence 
                                specifically designed for the Indian gaming market.
                                
                                ## 🚀 Key Features
                                - **Real-time price monitoring** with Steam Store integration  
                                - **Intelligent caching** for sub-20ms response times
                                - **Rate-limited API calls** to prevent service disruption
                                - **Event-driven architecture** with Kafka messaging
                                - **Indian market focus** with INR pricing and regional deals
                                - **Comprehensive game database** with 13+ popular games
                                
                                ## 📊 Performance Metrics
                                - **Cache Hit Ratio**: ~85% for popular games
                                - **API Response Time**: 20ms (cached) vs 400ms (live)
                                - **Rate Limiting**: 2,000/day (well under Steam's 100k limit)
                                - **Database**: 13+ games with real pricing data
                                
                                ## 🔍 Available Operations
                                - **Read-only API**: All endpoints are GET requests (no POST/PUT/DELETE)
                                - **Price Intelligence**: Real-time Steam pricing in INR
                                - **Game Catalog**: Search, filter, and browse games
                                - **Deal Analytics**: Biggest discounts and best deals
                                - **System Monitoring**: Cache stats and performance metrics
                                - **Testing Endpoints**: Kafka event simulation (development only)
                                
                                ## 🛡️ Safety & Reliability
                                - Bulletproof rate limiting prevents API bans
                                - Circuit breaker patterns for external API failures
                                - Comprehensive monitoring and health checks
                                - Redis-powered distributed caching
                                """)
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("IndiaDB Games Team")
                                .email("support@indiadb.games")
                                .url("https://github.com/indiadb/games-backend"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8081")
                                .description("Development Server"),
                        new Server()
                                .url("https://api.indiadb.games")
                                .description("Production Server")
                ))
                .tags(List.of(
                        new Tag()
                                .name("Steam Integration")
                                .description("🎮 Steam Store API integration with Indian pricing"),
                        new Tag()
                                .name("Game Catalog")
                                .description("📚 Game database management and search"),
                        new Tag()
                                .name("System Monitoring")
                                .description("📊 Performance metrics and health monitoring"),
                        new Tag()
                                .name("Event Testing")
                                .description("🧪 Kafka event system testing and development")
                ));
    }
}
