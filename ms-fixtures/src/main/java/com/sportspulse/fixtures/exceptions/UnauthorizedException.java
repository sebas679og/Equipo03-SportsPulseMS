package com.sportspulse.fixtures.exceptions;

/**
 * Custom exception representing a 401 Unauthorized error.
 *
 * <p>Thrown when a request fails due to missing, invalid, or expired authentication credentials. In
 * the context of SportsPulse, this typically occurs if the API Key for the upstream football data
 * provider is invalid or if the client fails our own security checks.
 */
public class UnauthorizedException extends RuntimeException {
  public UnauthorizedException(String message) {
    super(message);
  }
}
