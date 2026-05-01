package com.sportspulse.auth;

import com.sportspulse.auth.models.UserEntity;
import com.sportspulse.auth.utils.enums.UserRole;

/** Utility class for creating mock user data for testing purposes. */
public class UserDataProvider {

  public static final String VALID_USERNAME = "johndoe";
  public static final String VALID_EMAIL = "john@example.com";
  public static final String VALID_PASSWORD = "Secure@123";

  /** Static mock of fake user data. */
  public static UserEntity createMockUser() {
    return UserEntity.builder()
        .username("test-user")
        .email("test@email.com")
        .password("Test1234!")
        .role(UserRole.USER)
        .build();
  }

  /** Static mock of fake user data. */
  public static UserEntity createAnotherMockUser() {
    return UserEntity.builder()
        .username("test2-username")
        .email("test2@email.com")
        .password("Test12345!")
        .role(UserRole.USER)
        .build();
  }
}
