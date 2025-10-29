package com.carpool.demo.utils;

import org.springframework.stereotype.Component;

@Component
public class AuthUtils {

    /**
     * Extrahiert und bereinigt das JWT aus dem Authorization-Header.
     * Akzeptiert Header sowohl mit als auch ohne "Bearer".
     */
    public String extractToken(String authHeader) {
        if (authHeader == null || authHeader.isBlank()) {
            throw new IllegalArgumentException("Missing Authorization header");
        }

        System.out.println("🔹 Raw Authorization header: '" + authHeader + "'");

        // Entferne "Bearer" (egal ob groß/klein, mit/ohne Leerzeichen)
        String token = authHeader.replaceFirst("(?i)^Bearer\\s*", "");

        // Entferne ALLE Zeichen, die nicht in Base64URL erlaubt sind (z. B. Leerzeichen, Tabs, etc.)
        token = token.replaceAll("[^A-Za-z0-9\\-_\\.]", "");

        System.out.println("🔹 Cleaned token: [" + token + "]");
        return token;
    }
}
