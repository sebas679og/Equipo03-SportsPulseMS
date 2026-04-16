package com.sportspulse.teams.integration.msauth;

import com.sportspulse.teams.integration.msauth.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * AuthClientImpl Implementation of the {@link AuthClient} interface. Provides methods to interact
 * with the authentication service and validate user tokens.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthClientImpl implements AuthClient {

  @Override
  public UserResponse isTokenValid(String token) {
    return null;
  }
}
