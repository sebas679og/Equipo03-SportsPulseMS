package com.sportspulse.teams.exceptions;

/**
 * CustomBadGatewayException Exception type representing a bad gateway error scenario. Used to
 * indicate failures when the application cannot properly communicate with an upstream service or
 * external API.
 */
public class CustomBadGatewayException extends RuntimeException {
  public CustomBadGatewayException(String message) {
    super(message);
  }
}
