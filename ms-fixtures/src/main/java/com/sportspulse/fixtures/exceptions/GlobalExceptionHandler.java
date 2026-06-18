package com.sportspulse.fixtures.exceptions;

import com.sportspulse.fixtures.dtos.responses.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomUnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleCustomUnauthorizedException(CustomUnauthorizedException ex){
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
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
