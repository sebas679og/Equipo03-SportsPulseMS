package com.sportspulse.leagues.exceptions;

public class CustomBadGatewayException extends RuntimeException {
  public CustomBadGatewayException(String message) {
    super(message);
  }
}
