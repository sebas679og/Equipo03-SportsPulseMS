package com.sportspulse.fixtures.exceptions;

/**
 * Custom exception representing a 404 Not Found scenario specific to match data.
 *
 * <p>Thrown when a requested fixture ID does not exist in the database or cannot be retrieved from
 * the upstream provider. This allows the global exception handler to return a targeted error
 * message to the client.
 */
public class FixtureNotFoundException extends RuntimeException {
  public FixtureNotFoundException(String message) {
    super(message);
  }
}
