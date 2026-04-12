package com.sportspulse.auth.services;

import com.sportspulse.auth.config.JwtProperties;
import com.sportspulse.auth.dto.requests.LoginRequest;
import com.sportspulse.auth.dto.requests.RegisterRequest;
import com.sportspulse.auth.dto.responses.LoginResponse;
import com.sportspulse.auth.dto.responses.RegisterResponse;
import com.sportspulse.auth.dto.responses.TokenValidationResponse;
import com.sportspulse.auth.exceptions.ResourceConflictException;
import com.sportspulse.auth.exceptions.UnauthorizedException;
import com.sportspulse.auth.models.UserEntity;
import com.sportspulse.auth.repositories.UserRepository;
import com.sportspulse.auth.services.components.JwtService;
import com.sportspulse.auth.services.processors.ProcessorLoginUser;
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
  private final ProcessorLoginUser processorLoginUser;
  private final UserMapper userMapper;
  private final JwtService jwtService;
  private final JwtProperties jwtProperties;

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

  @Override
  public LoginResponse loginUser(LoginRequest request) {

    UserEntity user = processorLoginUser.authenticate(request.getEmail(), request.getPassword());

    String token =
        jwtService.generateToken(user.getId(), user.getUsername(), user.getRole().name());
    return LoginResponse.builder()
        .token(token)
        .tokenType(jwtProperties.getTokenType())
        .expiresIn(jwtProperties.getExpiration())
        .userId(user.getId())
        .build();
  }

  @Override
  public TokenValidationResponse tokenValidate(String authorizationHeader) {
    if (authorizationHeader == null
        || !authorizationHeader.startsWith(jwtProperties.getTokenType() + " ")) {
      throw new UnauthorizedException("Missing or malformed Authorization header");
    }
    String token = authorizationHeader.substring(jwtProperties.getTokenType().length() + 1);
    return jwtService.extractUserInfo(token);
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
