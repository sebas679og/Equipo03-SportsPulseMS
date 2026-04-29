package com.sportspulse.leagues.integration.msauth;

import com.sportspulse.leagues.integration.msauth.dto.UserResponse;

/** Contract for validating tokens against ms-auth. */
public interface AuthClient {

  /**
   * Validates the provided authentication token against the authentication service.
   *
   * @param token raw Bearer token without prefix
   * @return validation result and user details
   */
  UserResponse isTokenValid(String token);
}
