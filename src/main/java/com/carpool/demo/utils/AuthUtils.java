package com.carpool.demo.utils;

import com.carpool.demo.data.repository.UserRepository;
import com.carpool.demo.model.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthUtils {

    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;

    @Autowired
    public AuthUtils(JwtUtils jwtUtils, UserRepository userRepository) {
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
    }

    /**
     * Extrahiert und bereinigt das JWT aus dem Authorization-Header.
     * Akzeptiert Header sowohl mit als auch ohne "Bearer".
     */
    public String extractToken(String authHeader) {
        if (authHeader == null || authHeader.isBlank()) {
            throw new IllegalArgumentException("Missing Authorization header");
        }

        System.out.println("Raw Authorization header: '" + authHeader + "'");

        // Entferne "Bearer" (egal ob groß/klein, mit/ohne Leerzeichen)
        String token = authHeader.replaceFirst("(?i)^Bearer\\s*", "");

        // Entferne ALLE Zeichen, die nicht in Base64URL erlaubt sind (plus Punkt)
        token = token.replaceAll("[^A-Za-z0-9_\\.-]", "");

        System.out.println("Cleaned token: [" + token + "]");
        return token;
    }

    /**
     * Holt den Benutzer aus einem JWT-Token (wird in GraphQL genutzt).
     */
    public User getUserFromToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new RuntimeException("Token is missing");
        }

        // Entferne evtl. "Bearer "
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (!jwtUtils.validateToken(token)) {
            throw new RuntimeException("Invalid or expired token");
        }

        int userId = jwtUtils.extractUserId(token);

        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found for token"));
    }
}
