package com.sportspulse.teams.dto.response;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Represents a standardized error response returned by the API.
 *
 * @param error     short error identifier
 * @param message   detailed error description
 * @param timestamp time when the error occurred
 */
public record ErrorResponse(String error, String message, Instant timestamp) {

  /**
   * Creates an {@code ErrorResponse} using the current timestamp truncated to milliseconds.
   *
   * @param error   short error identifier
   * @param message detailed error description
   * @return a new {@code ErrorResponse} instance
   */
  public static ErrorResponse of(String error, String message) {
    return new ErrorResponse(error, message, Instant.now().truncatedTo(ChronoUnit.MILLIS));
  }
}
