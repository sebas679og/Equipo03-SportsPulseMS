package com.sportspulse.teams.exceptions;

/**
 * CustomBadRequestException Exception type representing a bad request error scenario.
 *
 * <p>Used to indicate that a client has sent an invalid or malformed request that cannot be
 * processed by the server.
 */
public class CustomBadRequestException extends RuntimeException {
  public CustomBadRequestException(String message) {
    super(message);
  }
}
