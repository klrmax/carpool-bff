package com.carpool.demo.config;

import com.carpool.demo.exception.GraphQLRequestException;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.stereotype.Component;

@Component
public class GraphQLExceptionHandler extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        if (ex instanceof GraphQLRequestException gre) {
            return GraphqlErrorBuilder.newError()
                    .message(gre.getMessage())
                    .errorType(gre.getErrorType())
                    .path(env.getExecutionStepInfo().getPath())
                    .location(env.getField().getSourceLocation())
                    .build();
        }
        // Für alle anderen Fehler macht Spring GraphQL das Standard-Handling
        return null;
    }
}
