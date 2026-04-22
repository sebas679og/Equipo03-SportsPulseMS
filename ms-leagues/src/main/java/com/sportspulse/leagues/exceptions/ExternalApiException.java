package com.sportspulse.leagues.exceptions;

/** Exception thrown for API-Football integration failures. */
public class ExternalApiException extends RuntimeException {

  public ExternalApiException(String message, Throwable cause) {
    super(message, cause);
  }
}
