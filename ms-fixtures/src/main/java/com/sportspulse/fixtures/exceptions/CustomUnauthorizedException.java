package com.sportspulse.fixtures.exceptions;

public class CustomUnauthorizedException extends RuntimeException {
    public CustomUnauthorizedException(String message) {
        super(message);
    }
}
