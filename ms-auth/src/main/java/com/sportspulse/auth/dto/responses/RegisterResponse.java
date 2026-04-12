package com.sportspulse.auth.dto.responses;

import com.sportspulse.auth.utils.enums.UserRole;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

/** Response DTO to user registration request. */
@Value
@Builder
public class RegisterResponse {
  UUID id;
  String username;
  String email;
  UserRole role;
  Instant createdAt;
}
