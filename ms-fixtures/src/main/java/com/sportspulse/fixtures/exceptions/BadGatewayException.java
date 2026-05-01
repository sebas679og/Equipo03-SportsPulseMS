package com.sportspulse.fixtures.exceptions;

/**
 * Custom exception representing a 502 Bad Gateway error.
 *
 * <p>This exception is typically thrown by the service layer when an upstream data provider (such
 * as an external Sports API) returns an invalid response or is unreachable, preventing the
 * application from fulfilling the request.
 *
 * @see com.sportspulse.fixtures.dto.response.ErrorResponse
 */
public class BadGatewayException extends RuntimeException {
  public BadGatewayException(String message) {
    super(message);
  }
}
