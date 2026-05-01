package com.sportspulse.fixtures.exceptions;

/**
 * Custom exception representing a 503 Service Unavailable error.
 *
 * <p>Used when the application is technically reachable but unable to handle the request due to
 * temporary overloading or maintenance of a critical downstream dependency. This signals to the
 * client that they should potentially retry the request after a short delay.
 */
public class ServiceUnavailableException extends RuntimeException {
  public ServiceUnavailableException(String message) {
    super(message);
  }
}
