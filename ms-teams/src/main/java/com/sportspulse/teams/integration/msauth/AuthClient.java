package com.sportspulse.teams.integration.msauth;

import com.sportspulse.teams.integration.msauth.dto.UserResponse;

/**
 * AuthClient Defines the contract for interacting with the authentication service. Provides methods
 * to validate tokens and retrieve user-related information.
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
