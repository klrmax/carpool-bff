package com.carpool.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import reactor.core.publisher.Mono;

@Configuration
public class GraphQLInterceptorConfig {

    @Bean
    public WebGraphQlInterceptor addAuthHeaderToContext() {
        return (request, chain) -> {
            String authHeader = request.getHeaders().getFirst("Authorization");

            if (authHeader != null) {
                // Token in den GraphQL-Kontext legen
                request.configureExecutionInput((executionInput, builder) ->
                        builder.graphQLContext(ctx -> ctx.put("Authorization", authHeader)).build());
            }

            return chain.next(request);
        };
    }
}
