package com.sportspulse.leagues.exceptions;

public class CustomTooManyRequestsException extends RuntimeException {
  public CustomTooManyRequestsException(String message) {
    super(message);
  }
}
