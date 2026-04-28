package com.sportspulse.standings.exceptions;

/**
 * CustomUnauthorizedException Exception type representing an unauthorized access error scenario.
 * Used to indicate that a request is missing valid authentication credentials or the provided
 * credentials are invalid.
 */
public class CustomUnauthorizedException extends RuntimeException {
  public CustomUnauthorizedException(String message) {
    super(message);
  }
}
