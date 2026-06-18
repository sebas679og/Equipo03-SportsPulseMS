package com.sportspulse.fixtures.integration.msauth;

import com.sportspulse.fixtures.integration.msauth.dto.UserResponse;

public interface AuthClient {

    UserResponse isTokenValid(String token);
}
