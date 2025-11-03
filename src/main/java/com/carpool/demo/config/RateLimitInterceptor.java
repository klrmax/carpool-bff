package com.carpool.demo.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Einfaches Rate Limiting für Registrierung und Login
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final Map<String, long[]> requestMap = new HashMap<>();
    private static final int MAX_REGISTER = 5;
    private static final int MAX_LOGIN = 10;
    private static final long TIME_WINDOW = 60_000; // 1 Minute

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        String ip = request.getRemoteAddr();

        int limit = 0;
        if (path.contains("/api/users/register")) limit = MAX_REGISTER;
        else if (path.contains("/api/users/login")) limit = MAX_LOGIN;
        else return true;

        long now = System.currentTimeMillis();
        String key = ip + ":" + path;
        long[] data = requestMap.getOrDefault(key, new long[]{now, 0});

        // Zeitfenster abgelaufen?
        if (now - data[0] > TIME_WINDOW) {
            data[0] = now;
            data[1] = 1;
            requestMap.put(key, data);
            return true;
        }

        data[1]++;
        if (data[1] > limit) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Too many requests. Try again later.\"}");
            return false;
        }

        return true;
    }
}
