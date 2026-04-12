package com.sportspulse.auth.services;

import com.sportspulse.auth.dto.requests.RegisterRequest;
import com.sportspulse.auth.dto.responses.RegisterResponse;

/** Application service interface responsible for managing operations related to users. */
public interface UserService {

  RegisterResponse registerUser(RegisterRequest request);
}
