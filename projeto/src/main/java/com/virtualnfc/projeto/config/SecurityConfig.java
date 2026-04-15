package com.virtualnfc.projeto.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
public class SecurityConfig {
    private final CorsConfigurationSource corsConfigurationSource;
    public SecurityConfig(CorsConfigurationSource corsConfigurationSource) {
        this.corsConfigurationSource = corsConfigurationSource;
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 🔓 CORS (OBRIGATÓRIO)
            .cors(cors -> {})
            // 🔐 CSRF desativado para API
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(authz -> authz
                .requestMatchers(
                    "/",
                    "/index.html",
                    "/*.js",
                    "/*.css",
                    "/assets/**"
                ).permitAll()
                .anyRequest().permitAll()
            );

        return http.build();
    }
}