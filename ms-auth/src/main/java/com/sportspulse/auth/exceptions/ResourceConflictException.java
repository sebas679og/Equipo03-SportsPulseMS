package com.sportspulse.auth.exceptions;

/** Custom Exception of already registered resources. */
public class ResourceConflictException extends RuntimeException {

  public ResourceConflictException(String message) {
    super(message);
  }
}
