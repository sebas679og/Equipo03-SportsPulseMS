package com.sportspulse.auth.services;

import com.sportspulse.auth.dto.requests.LoginRequest;
import com.sportspulse.auth.dto.requests.RegisterRequest;
import com.sportspulse.auth.dto.responses.LoginResponse;
import com.sportspulse.auth.dto.responses.RegisterResponse;
import com.sportspulse.auth.dto.responses.TokenValidationResponse;

/** Application service interface responsible for managing operations related to users. */
public interface UserService {

  RegisterResponse registerUser(RegisterRequest request);

  LoginResponse loginUser(LoginRequest request);

  TokenValidationResponse tokenValidate(String authorizationHeader);
}
