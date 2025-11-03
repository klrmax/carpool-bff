package com.carpool.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Gilt für alle Routen, inkl. /api/** und /graphql
                        .allowedOrigins(
                                // Lokale Entwicklungs-URLs
                                "http://localhost:3000",
                                "http://127.0.0.1:3000",
                                "http://localhost:4200",
                                "http://127.0.0.1:5501",
                                "http://localhost:5501",
                                // Deploy-URLs (Backend, SPA, MPA)
                                "https://carpoolbff-c576f25b03e8.herokuapp.com",
                                "https://carpool-spa-dc2811d7ff92.herokuapp.com",
                                "https://carpool-mpa-b2ab41ee1e9d.herokuapp.com"
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                        .allowedHeaders("*")
                        .allowCredentials(false);

            }
        };
    }
}
