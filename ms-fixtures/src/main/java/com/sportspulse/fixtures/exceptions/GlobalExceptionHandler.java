package com.sportspulse.fixtures.exceptions;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.sportspulse.fixtures.dtos.responses.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex) {
        return handleBindingErrors(ex.getBindingResult());
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException ex) {
        return handleBindingErrors(ex.getBindingResult());
    }

    private ResponseEntity<ErrorResponse> handleBindingErrors(BindingResult bindingResult) {
        String description =
                bindingResult.getAllErrors().stream()
                        .map(
                                error -> {
                                    if (error instanceof FieldError fieldError) {
                                        String field = fieldError.getField();
                                        Object rejectedValue = fieldError.getRejectedValue();

                                        if (fieldError.contains(TypeMismatchException.class)) {
                                            TypeMismatchException typeMismatch =
                                                    fieldError.unwrap(TypeMismatchException.class);
                                            Throwable cause = typeMismatch.getCause();
                                            if (cause instanceof IllegalArgumentException) {
                                                return cause.getMessage();
                                            }
                                            return "Field '%s': invalid value '%s'".formatted(field, rejectedValue);
                                        }

                                        String message = fieldError.getDefaultMessage();
                                        if (message != null) {
                                            message = message.replaceAll("\\s+", " ").trim();
                                        }
                                        return field + ": " + (message != null ? message : "invalid value");
                                    }
                                    return error.getDefaultMessage();
                                })
                        .filter(msg -> msg != null && !msg.isBlank())
                        .collect(Collectors.joining("; "));

        return badRequest(description);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> customHttpMessageNotReadableException(
            HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause();
        String message = "Invalid request payload";

        if (cause instanceof InvalidFormatException formatException) {
            String fieldName = formatException.getPath().getFirst().getFieldName();
            Object invalidValue = formatException.getValue();
            Class<?> targetType = formatException.getTargetType();

            if (targetType.isEnum()) {
                String allowedValues =
                        Arrays.stream(targetType.getEnumConstants())
                                .map(Object::toString)
                                .collect(Collectors.joining(", "));
                message =
                        String.format(
                                "%s: Invalid value '%s'. Allowed values: [%s]",
                                fieldName, invalidValue, allowedValues);
            } else if (targetType.equals(LocalDate.class)) {
                message =
                        String.format(
                                "%s: Invalid date '%s'. Expected format: yyyy-MM-dd",
                                fieldName, invalidValue);
            } else {
                message = String.format("%s: Invalid value '%s'", fieldName, invalidValue);
            }
        }

        return badRequest(message);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format("Invalid path parameter for: '%s'", ex.getName());
        return badRequest(message);
    }

    @ExceptionHandler(CustomUnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleCustomUnauthorizedException(CustomUnauthorizedException ex){
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(CustomNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCustomNotFoundException(CustomNotFoundException ex){
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(CustomTooManyRequestsException.class)
    public ResponseEntity<ErrorResponse> handleCustomTooManyRequestsException(CustomTooManyRequestsException ex){
        return buildErrorResponse(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        if (log.isErrorEnabled()) {
            log.error("Unexpected error", ex);
        }
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    @ExceptionHandler(CustomBadGatewayException.class)
    public ResponseEntity<ErrorResponse> handleCustomBadGatewayException(CustomBadGatewayException ex){
        return buildErrorResponse(HttpStatus.BAD_GATEWAY, ex.getMessage());
    }

    @ExceptionHandler(CustomServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleCustomServiceUnavailableException(CustomServiceUnavailableException ex){
        return buildErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }

    private ResponseEntity<ErrorResponse> badRequest(String message){
        return buildErrorResponse(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String description) {
        return ResponseEntity.status(status)
                .body(
                        ErrorResponse.builder()
                                .code(status.value())
                                .name(status.name())
                                .description(description)
                                .build());
    }
}
