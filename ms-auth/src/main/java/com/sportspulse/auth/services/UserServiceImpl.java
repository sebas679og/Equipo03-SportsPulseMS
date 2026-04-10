package com.sportspulse.auth.services;

import com.sportspulse.auth.dto.requests.RegisterRequest;
import com.sportspulse.auth.dto.responses.RegisterResponse;
import com.sportspulse.auth.exceptions.ResourceConflictException;
import com.sportspulse.auth.models.UserEntity;
import com.sportspulse.auth.repositories.UserRepository;
import com.sportspulse.auth.services.processors.ProcessorRegisterUser;
import com.sportspulse.auth.utils.enums.UserRole;
import com.sportspulse.auth.utils.mappers.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/** User Service Coordinator. */
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final ProcessorRegisterUser processorRegisterUser;
  private final UserMapper userMapper;

  @Override
  public RegisterResponse registerUser(RegisterRequest request) {
    validateUniqueEmailAndUsername(request.getEmail(), request.getUsername());
    UserEntity userEntity =
        processorRegisterUser.userRegister(
            request.getUsername(),
            request.getEmail(),
            passwordEncoder.encode(request.getPassword()),
            UserRole.USER);
    return userMapper.toRegisterResponse(userEntity);
  }

  /** Method for validating the existence of a user and email. */
  private void validateUniqueEmailAndUsername(String email, String username) {
    if (userRepository.existsByEmail(email)) {
      throw new ResourceConflictException("Email is already in use.");
    }
    if (userRepository.existsByUsername(username)) {
      throw new ResourceConflictException("Username is already in use.");
    }
  }
}
