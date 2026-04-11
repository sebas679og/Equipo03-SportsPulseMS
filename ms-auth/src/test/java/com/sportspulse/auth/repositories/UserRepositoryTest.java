package com.sportspulse.auth.repositories;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sportspulse.auth.AbstractIntegrationTest;
import com.sportspulse.auth.UserDataProvider;
import com.sportspulse.auth.models.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** Integration tests for verification of data access methods. */
public class UserRepositoryTest extends AbstractIntegrationTest {

  @Autowired UserRepository userRepository;

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();
  }

  @Test
  public void shouldReturnTrueWhenEmailAlreadyExists() {
    UserEntity savedUser = userRepository.save(UserDataProvider.createMockUser());
    assertTrue(userRepository.existsByEmail(savedUser.getEmail()));
  }

  @Test
  public void shouldReturnFalseWhenEmailIsNotRegistered() {
    userRepository.save(UserDataProvider.createMockUser());
    assertFalse(userRepository.existsByEmail("email@not-registred.test"));
  }

  @Test
  public void shouldReturnTrueWhenUsernameAlreadyExists() {
    UserEntity savedUser = userRepository.save(UserDataProvider.createMockUser());
    assertTrue(userRepository.existsByUsername(savedUser.getUsername()));
  }

  @Test
  public void shouldReturnFalseWhenUsernameIsNotRegistered() {
    userRepository.save(UserDataProvider.createMockUser());
    assertFalse(userRepository.existsByUsername("username-not-registred"));
  }
}
