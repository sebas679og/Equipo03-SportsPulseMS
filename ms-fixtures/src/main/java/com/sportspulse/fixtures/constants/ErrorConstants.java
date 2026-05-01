package com.sportspulse.fixtures.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Utility class that centralizes error codes and descriptive messages for the application.
 *
 * <p>This class is divided into two main categories:
 *
 * <ul>
 *   <li>{@link Code}: Machine-readable strings used for programmatic error identification.
 *   <li>{@link Message}: Human-readable descriptions providing context about the failure.
 * </ul>
 *
 * <p>These constants ensure consistency in error responses across the microservice.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ErrorConstants {

  /** Unique identifiers for specific error types. */
  public static final class Code {
    public static final String FIXTURE_NOT_FOUND = "FIXTURE_NOT_FOUND";
    public static final String INVALID_PARAMETER = "INVALID_PARAMETER";
    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    public static final String RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND";
    public static final String EXTERNAL_API_ERROR = "EXTERNAL_API_ERROR";
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
  }

  /** Human-readable messages providing details about the error. */
  public static final class Message {
    public static final String SERVICE_EMPTY_OR_NULL = "Auth Service returned empty or null";
    public static final String INVALID_OR_EXPIRED = "Invalid or expired token, please log in again";
    public static final String SESSION_REJECTED = "Session validation service rejected the request";
    public static final String SESSION_NOT_AVAILABLE =
        "Session validation service is not available at this time";
    public static final String RESOURCE_NOT_FOUND = "The requested resource does not exist";
    public static final String INTERNAL_ERROR = "Internal server error";
    public static final String NO_DATA_FOUND = "No data found for: ";
    public static final String AUTHENTICATION_NOT_OBTAINED = "Authentication could not be obtained";
    public static final String AUTHENTICATION_REQUIRED = "Authentication required";
    public static final String NO_FIXTURE_FOUND = "No fixture was found with the ID: ";
    public static final String API_FOOTBALL_NOT_AVAILABLE =
        "Api-Football is not currently available, please try again";
  }
}
