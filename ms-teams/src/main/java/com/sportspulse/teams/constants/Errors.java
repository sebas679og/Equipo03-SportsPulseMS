package com.sportspulse.teams.constants;

import com.sportspulse.teams.dto.response.ErrorResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Errors {

  /** Constants for error codes used in {@link ErrorResponse}. */
  public static class Code {
    public static final String MISSING_PARAMETER = "MISSING_PARAMETER";
    public static final String INVALID_PARAMETER = "INVALID_PARAMETER";
    public static final String EXTERNAL_SERVICE_ERROR = "EXTERNAL_SERVICE_ERROR";
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
    public static final String MISSING_TOKEN = "MISSING_TOKEN";
    public static final String INVALID_TOKEN = "INVALID_TOKEN";
    public static final String TOKEN_VALIDATION_ERROR = "TOKEN_VALIDATION_ERROR";
  }

  /** Constants for error messages used in {@link ErrorResponse}. */
  public static class Message {
    public static final String MISSING_PARAMETER = "Required parameter is missing: ";
    public static final String INVALID_PARAMETER = "Invalid value for parameter: ";
    public static final String EXTERNAL_SERVICE_ERROR = "API-Football service returned an error: ";
    public static final String INTERNAL_ERROR = "An unexpected error occurred.";
    public static final String MISSING_TOKEN = "Authorization header is missing or invalid.";
    public static final String INVALID_TOKEN = "The provided token is not valid.";
    public static final String TOKEN_VALIDATION_ERROR = "Could not validate token.";
  }
}
