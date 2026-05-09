package com.sportspulse.leagues.exceptions;

/**
 * CustomBadGatewayException
 *
 * <p>Exception type representing a "Bad Gateway" error scenario.
 *
 * <p>Used to indicate that the server, acting as a gateway or proxy, received an invalid response
 * from an upstream server.
 */
public class CustomBadGatewayException extends RuntimeException {
  public CustomBadGatewayException(String message) {
    super(message);
  }
}
