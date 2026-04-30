package com.sportspulse.leagues.exceptions;

/**
 * ExternalDataInconsistencyException
 *
 * <p>Exception type representing an inconsistency detected in external data sources.
 *
 * <p>Used to indicate that information retrieved from an external system or service does not match
 * the expected format, values, or integrity rules.
 */
public class ExternalDataInconsistencyException extends RuntimeException {
  public ExternalDataInconsistencyException(String message) {
    super(message);
  }
}
