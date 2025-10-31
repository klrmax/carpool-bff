package com.carpool.demo.exception;

import org.springframework.graphql.execution.ErrorType;

public class GraphQLRequestException extends RuntimeException {
    private final ErrorType errorType;

    public GraphQLRequestException(String message, ErrorType errorType) {
        super(message);
        this.errorType = errorType;
    }

    public ErrorType getErrorType() {
        return errorType;
    }
}
