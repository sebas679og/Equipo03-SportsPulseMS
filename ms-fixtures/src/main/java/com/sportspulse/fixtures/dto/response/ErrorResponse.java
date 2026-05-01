package com.sportspulse.fixtures.dto.response;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.Builder;

/**
 * Data Transfer Object (DTO) used to provide a standardized error structure across the API.
 *
 * <p>This record ensures that when an exception occurs, the client receives a consistent response
 * containing the error type, a descriptive message, and a precise timestamp. It includes a compact
 * constructor to ensure the timestamp is never null and is stored with millisecond precision.
 *
 * @param error The high-level error category or code (e.g., "Not Found", "Validation Failed").
 * @param message A human-readable explanation of what went wrong.
 * @param timestamp The exact moment the error occurred, defaulted to the current time if not
 *     provided.
 */
@Builder
public record ErrorResponse(String error, String message, Instant timestamp) {
  /** Initializes the record, ensuring the timestamp is set to the current time if null. */
  public ErrorResponse {
    if (timestamp == null) {
      timestamp = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    }
  }
}
