package com.sportspulse.leagues.exceptions;

/** Exception used when league ID does not exist. */
public class CustomBadRequestException extends RuntimeException {
  public CustomBadRequestException(String message) {
    super(message);
  }
}
