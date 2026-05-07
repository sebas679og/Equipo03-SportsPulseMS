package com.sportspulse.leagues.exceptions;

/**
 * CustomTooManyRequestsException
 *
 * <p>Exception type representing a "Too Many Requests" error scenario.
 *
 * <p>Used to indicate that a client has sent too many requests in a given amount of time, exceeding
 * the server's rate limits.
 */
public class CustomTooManyRequestsException extends RuntimeException {
  public CustomTooManyRequestsException(String message) {
    super(message);
  }
}
