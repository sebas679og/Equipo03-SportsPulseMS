package com.sportspulse.leagues.exceptions;

public class ExternalDataInconsistencyException extends RuntimeException {
  public ExternalDataInconsistencyException(String message) {
    super(message);
  }
}
