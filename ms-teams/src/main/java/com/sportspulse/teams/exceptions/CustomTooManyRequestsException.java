package com.sportspulse.teams.exceptions;

/**
 * CustomTooManyRequestsException Exception type representing a too many requests error scenario.
 * Used to indicate that a client has exceeded the allowed number of requests within a given
 * timeframe, typically due to rate limiting.
 */
public class CustomTooManyRequestsException extends RuntimeException {
  public CustomTooManyRequestsException(String message) {
    super(message);
  }
}
