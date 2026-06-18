package com.sportspulse.fixtures.exceptions;

public class CustomServiceUnavailableException extends RuntimeException {
    public CustomServiceUnavailableException(String message) {
        super(message);
    }
}
