package com.sportspulse.auth;

import com.sportspulse.auth.models.UserEntity;
import com.sportspulse.auth.utils.enums.UserRole;

/** Utility class for creating mock user data for testing purposes. */
public class UserDataProvider {

  /** Static mock of fake user data. */
  public static UserEntity createMockUser() {
    return UserEntity.builder()
        .username("test-user")
        .email("test@email.com")
        .password("Test1234!")
        .role(UserRole.USER)
        .build();
  }
}
