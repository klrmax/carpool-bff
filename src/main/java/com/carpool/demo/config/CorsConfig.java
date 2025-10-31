package com.carpool.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // gilt für alle Endpunkte
                        .allowedOrigins("http://127.0.0.1:5501", 
                                        "http://localhost:5501", 
                                        "https://carpoolbff-c576f25b03e8.herokuapp.com", 
                                        "http://localhost:4200", "http://127.0.0.1:3000", 
                                        "https://carpool-spa-dc2811d7ff92.herokuapp.com/rides",
                                        "https://carpool-mpa-b2ab41ee1e9d.herokuapp.com/"
                                       )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(false);
                        // TODO: Frontend URL hier einfügen wenn deployed
            }
        };
    }
}
