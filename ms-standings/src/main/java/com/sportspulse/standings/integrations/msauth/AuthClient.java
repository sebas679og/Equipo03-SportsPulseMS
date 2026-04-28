package com.sportspulse.standings.integrations.msauth;

import com.sportspulse.standings.integrations.msauth.dto.UserResponse;

/**
 * AuthClient Interface representing a client for handling authentication operations.
 *
 * <p>Serves as a contract for defining methods related to user authentication, authorization, and
 * token management. Implementations of this interface are responsible for communicating with
 * authentication providers or services to validate credentials and manage secure access.
 */
public interface AuthClient {

  /**
   * Validates the provided authentication token against the authentication service.
   *
   * @param token the authentication token to be validated
   * @return a {@link UserResponse} containing the validation result and user details
   */
  UserResponse isTokenValid(String token);
}
