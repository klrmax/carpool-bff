package com.carpool.demo.config;

import com.carpool.demo.security.JwtAuthenticationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class SecurityConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;

    public SecurityConfig(RateLimitInterceptor rateLimitInterceptor) {
        this.rateLimitInterceptor = rateLimitInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/users/register", "/api/users/login");
    }

    // Passwort-Encoder (wird auch für GraphQL verwendet)
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Filter nur für REST-Endpunkte
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilter(JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(filter);
        registrationBean.addUrlPatterns("/api/*"); // GraphQL nutzt stattdessen GraphQLContext
        return registrationBean;
    }

    // Rate Limiting Interceptor registrieren
    @Bean
    public WebMvcConfigurer webMvcConfigurer(RateLimitInterceptor rateLimitInterceptor) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(rateLimitInterceptor);
            }
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CORS aktivieren (siehe Bean unten)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // CSRF deaktivieren (für APIs & GraphQL nicht notwendig)
                .csrf(csrf -> csrf.disable())
                // Zugriff regeln
                .authorizeHttpRequests(auth -> auth
                        // --- Öffentlich zugängliche REST-Endpunkte ---
                        .requestMatchers(
                                "/api/users/register",
                                "/api/users/login",
                                "/api/ride",
                                "/api/ride/search",
                                "/api/ride/search-async",
                                "/api/trains/**"
                        ).permitAll()

                        // --- Öffentlich zugängliche GraphQL-Endpunkte ---
                        .requestMatchers("/graphql").permitAll()

                        // --- Alles andere zunächst offen lassen (kann später .authenticated() werden) ---
                        .anyRequest().permitAll()
                )
                // Kein Standard-Login-Formular oder HTTP Basic Auth
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form.disable());

        return http.build();
    }


    // CORS für REST + GraphQL (kombiniert)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://127.0.0.1:3000",
                "http://localhost:4200",
                "http://127.0.0.1:5501",
                "http://localhost:5501",
                "https://carpoolbff-c576f25b03e8.herokuapp.com",
                "https://carpool-spa-dc2811d7ff92.herokuapp.com",
                "https://carpool-mpa-b2ab41ee1e9d.herokuapp.com"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true); // wichtig für Authorization Header
        config.setMaxAge(3600L); // 1 Stunde Cache

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
