package com.sportspulse.fixtures.exceptions;

public class CustomBadGatewayException extends RuntimeException {
    public CustomBadGatewayException(String message) {
        super(message);
    }
}
