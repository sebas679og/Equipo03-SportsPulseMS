package com.sportspulse.auth.exceptions;

/**
 * UnauthorizedException. Represents an exception thrown when a user is not authorized to perform an
 * action.
 */
public class UnauthorizedException extends RuntimeException {
  public UnauthorizedException(String message) {
    super(message);
  }
}
