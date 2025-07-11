package com.indodb.games_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/**").permitAll()  // Allow all API access
                .requestMatchers("/actuator/**").permitAll()  // Allow actuator access
                .requestMatchers("/", "/index.html", "/static/**").permitAll()  // Allow static content
                .anyRequest().permitAll()  // Allow everything for development
            )
            .csrf(csrf -> csrf.disable())  // Disable CSRF for API testing
            .cors(cors -> cors.disable())  // Disable CORS restrictions for development
            .headers(headers -> headers
                .frameOptions(frame -> frame.disable())  // For development
                .contentTypeOptions(content -> content.disable())  // Less strict content type checking
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)  // Stateless for APIs
            );
        
        return http.build();
    }
}
