package com.andrulban.sandbox.stripe.exception;

public enum ExceptionType {
    VALIDATION_ERROR(400),
    UNAUTHORIZED(401),
    NO_PERMISSION(403),
    NO_RESULT(404),
    ERROR(500);


    private Integer statusCode;

    ExceptionType(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public Integer getStatusCode() {
        return statusCode;
    }
}
