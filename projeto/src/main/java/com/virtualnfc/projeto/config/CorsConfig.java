package com.virtualnfc.projeto.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.core.Ordered;

@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsConfig {

    @Bean
    public CorsFilter corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // ADICIONE SEUS DOMÍNIOS AQUI:
        config.setAllowedOrigins(List.of(
            "http://89.167.42.44:4200",      // IP com porta
            "http://www.virtualnfc.com",     // Domínio SEM porta (via Nginx)
            "https://www.virtualnfc.com",
            "http://virtualnfc.com",         // Domínio raiz
            "https://virtualnfc.com", 
            "http://localhost:4200",          // Local
            "https://89.167.42.44:4200",
            "https://virtualnfcbackend-production.up.railway.app",
            "https://sistema-internovirtual-front.vercel.app"

        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setExposedHeaders(List.of("Authorization"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}