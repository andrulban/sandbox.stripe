package com.andrulban.sandbox.stripe.exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Date;
import java.util.Set;

@ControllerAdvice(annotations = RestController.class)
public class RestControllerExceptionHandler {
    private static final Logger LOGGER = LogManager.getLogger(RestControllerExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorInfo> handleApiException(ApiException ex) {
        LOGGER.error("ApiException has been handled. Exception message: {}", ex.getMessage());
        return new ResponseEntity<>(new ErrorInfo(new Date(), ex.getStatus().getStatusCode(), ex.getMessage()), HttpStatus.valueOf(ex.getStatus().getStatusCode()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ErrorInfo> handleConstraintViolation(ConstraintViolationException ex) {
        Set<ConstraintViolation<?>> constraintViolations = ex.getConstraintViolations();
        StringBuilder messageBuilder = new StringBuilder("Validation failure. Those parameters has erros: ");
        constraintViolations.forEach(constraintViolation -> messageBuilder.append(String.format("%s has value '%s'. %s", constraintViolation.getPropertyPath(),
                        constraintViolation.getInvalidValue(), constraintViolation.getMessage())));
        LOGGER.error("ConstraintViolationException has been handled. Exception message: {}", messageBuilder.toString());
        return new ResponseEntity<>(new ErrorInfo(new Date(), HttpStatus.BAD_REQUEST.value(), messageBuilder.toString()), HttpStatus.BAD_REQUEST);
    }
}
