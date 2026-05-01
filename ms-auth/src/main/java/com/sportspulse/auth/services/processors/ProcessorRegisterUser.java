package com.sportspulse.auth.services.processors;

import com.sportspulse.auth.models.UserEntity;
import com.sportspulse.auth.repositories.UserRepository;
import com.sportspulse.auth.utils.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** User registration processing class. */
@Service
@RequiredArgsConstructor
public class ProcessorRegisterUser {

  private final UserRepository userRepository;

  /** Build the user entity and save it in the database. */
  @Transactional
  public UserEntity userRegister(String username, String email, String password, UserRole role) {
    UserEntity userEntity =
        UserEntity.builder().username(username).email(email).password(password).role(role).build();

    return userRepository.save(userEntity);
  }
}
