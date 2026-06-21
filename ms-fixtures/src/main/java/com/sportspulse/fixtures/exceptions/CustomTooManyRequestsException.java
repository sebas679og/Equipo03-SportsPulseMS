package com.sportspulse.fixtures.exceptions;

public class CustomTooManyRequestsException extends RuntimeException {
    public CustomTooManyRequestsException(String message) {
        super(message);
    }
}
