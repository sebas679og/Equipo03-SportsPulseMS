package com.sportspulse.leagues.exceptions;

/**
 * CustomServiceUnavailableException Exception type representing a service unavailable error
 * scenario. Used to indicate that an upstream service or external API is temporarily unavailable or
 * cannot process the request.
 */
public class CustomServiceUnavailableException extends RuntimeException {
  public CustomServiceUnavailableException(String message) {
    super(message);
  }
}