package com.sportspulse.leagues.exceptions;

/**
 * CustomNotFoundException
 *
 * <p>Exception type representing a "Not Found" error scenario.
 *
 * <p>Used to indicate that a requested resource or entity could not be located within the system.
 */
public class CustomNotFoundException extends RuntimeException {
  public CustomNotFoundException(String message) {
    super(message);
  }
}
