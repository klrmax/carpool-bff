package com.carpool.demo.security;

import com.carpool.demo.utils.AuthUtils;
import com.carpool.demo.utils.JwtUtils;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter implements Filter {

    private final JwtUtils jwtUtils;
    private final AuthUtils authUtils;

    public JwtAuthenticationFilter(JwtUtils jwtUtils, AuthUtils authUtils) {
        this.jwtUtils = jwtUtils;
        this.authUtils = authUtils;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String authHeader = httpRequest.getHeader("Authorization");
        String requestUri = httpRequest.getRequestURI();

        boolean isPublicEndpoint =
                requestUri.startsWith("/api/users/login") ||
                requestUri.startsWith("/api/users/register") ||
                requestUri.startsWith("/api/ride/search") ||
                requestUri.equals("/api/ride") ||
                requestUri.startsWith("/api/trains/");

        if (isPublicEndpoint) {
            // Öffentlicher Endpunkt ohne token erlaubt
            chain.doFilter(request, response);
        } else if (authHeader != null) {
            try {
                String token = authUtils.extractToken(authHeader);
                if (jwtUtils.validateToken(token)) {
                    chain.doFilter(request, response);
                } else {
                    throw new ServletException("Invalid token");
                }
            } catch (Exception e) {
                HttpServletResponse httpResponse = (HttpServletResponse) response;
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                httpResponse.getWriter().write("{\"error\": \"Unauthorized: " + e.getMessage() + "\"}");
            }
        } else {
            // Geschützter Endpunkt ohne Auth-Header
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.getWriter().write("{\"error\": \"Unauthorized or invalid token\"}");
        }
    }
}
