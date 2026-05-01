package com.sportspulse.auth.dto.responses;

import com.sportspulse.auth.utils.enums.UserRole;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

/** TokenValidationResponse Represents the result of validating a JWT token. */
@Value
@Builder
public class TokenValidationResponse {

  boolean valid;
  UUID userId;
  String username;
  UserRole role;
}
