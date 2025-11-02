package com.carpool.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.server.WebGraphQlInterceptor;

import java.util.Arrays;
import java.util.List;

@Configuration
public class GraphQLInterceptorConfig {

    // Öffentliche Queries/Mutations (wie REST ohne Token)
    private static final List<String> PUBLIC_OPERATIONS = Arrays.asList(
            "register",           // User registrieren
            "login",              // User login
            "searchRides",        // Fahrten suchen (wie REST /api/ride/search)
            "getAllRides",        // Alle Fahrten (wie REST /api/ride)
            "getRideById",        // Einzelne Fahrt
            "getTrainConnections" // Zugverbindungen (wie REST /api/trains)
    );

    @Bean
    public WebGraphQlInterceptor addAuthHeaderToContext() {
        return (request, chain) -> {
            String authHeader = request.getHeaders().getFirst("Authorization");
            String operationName = request.getOperationName();

            // Prüfe ob Operation öffentlich ist
            boolean isPublic = operationName != null && PUBLIC_OPERATIONS.contains(operationName);

            if (authHeader != null) {
                // Token in den GraphQL-Kontext legen
                request.configureExecutionInput((executionInput, builder) ->
                        builder.graphQLContext(ctx -> {
                            ctx.put("Authorization", authHeader);
                            ctx.put("isPublic", isPublic);
                        }).build());
            } else {
                // Kein Token vorhanden, aber Operation könnte öffentlich sein
                request.configureExecutionInput((executionInput, builder) ->
                        builder.graphQLContext(ctx -> {
                            ctx.put("isPublic", isPublic);
                        }).build());
            }

            return chain.next(request);
        };
    }
}
