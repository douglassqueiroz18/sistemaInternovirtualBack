package com.virtualnfc.projeto.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // ADICIONE SEUS DOMÍNIOS AQUI:
        config.setAllowedOrigins(List.of(
            "http://89.167.42.44:4200",      // IP com porta
            "http://www.virtualnfc.com",     // Domínio SEM porta (via Nginx)
            "https://www.virtualnfc.com",
            "http://virtualnfc.com",         // Domínio raiz
            "https://virtualnfc.com", 
            "http://localhost:4200"          // Local
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}