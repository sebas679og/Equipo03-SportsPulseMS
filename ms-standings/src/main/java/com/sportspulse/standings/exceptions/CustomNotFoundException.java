package com.sportspulse.standings.exceptions;

/**
 * CustomNotFoundException Exception type representing a resource not found error scenario. Used to
 * indicate that a requested entity or resource could not be located within the system or an
 * external service.
 */
public class CustomNotFoundException extends RuntimeException {
  public CustomNotFoundException(String message) {
    super(message);
  }
}
