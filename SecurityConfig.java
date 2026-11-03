package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

/**
 * Spring Security 6 / Spring Boot 3 Defense-in-Depth Configuration.
 * Implements password hashing, security headers, and public/protected routes.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Industry-standard BCrypt password hashing (work factor 12)
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for statutory REST endpoints, or use cookies for stateless APIs
            .csrf(AbstractHttpConfigurer::disable)
            
            // Authorization Rules
            .authorizeHttpRequests(auth -> auth
                // Allow static resources & single-page frontend
                .requestMatchers("/", "/index.html", "/static/**", "/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                // Allow public authentication & CSV endpoints
                .requestMatchers("/api/auth/**", "/api/csv/**").permitAll()
                // All other requests require authentication
                .anyRequest().authenticated()
            )

            // Session Management
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )

            // Defense-in-Depth HTTP Security Headers
            .headers(headers -> headers
                .contentTypeOptions(contentType -> {}) // X-Content-Type-Options: nosniff
                .frameOptions(frame -> frame.sameOrigin()) // Allow same-origin iframe preview
                .referrerPolicy(referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31536000)
                )
            );

        return http.build();
    }
}
