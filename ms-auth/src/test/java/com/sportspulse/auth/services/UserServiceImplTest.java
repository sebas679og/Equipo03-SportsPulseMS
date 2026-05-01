package com.sportspulse.auth.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sportspulse.auth.config.properties.JwtProperties;
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
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Unit tests for coordination and validation class. */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl")
class UserServiceImplTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private ProcessorRegisterUser processorRegisterUser;
  @Mock private ProcessorLoginUser processorLoginUser;
  @Mock private JwtService jwtService;
  @Mock private UserMapper userMapper;
  @Mock private JwtProperties jwtProperties;

  @InjectMocks private UserServiceImpl userService;

  // -------------------------------------------------------------------------
  // Fixtures
  // -------------------------------------------------------------------------
  private static final String EMAIL = "john@example.com";
  private static final String USERNAME = "johndoe";
  private static final String PASSWORD = "RawPass@1";
  private static final String ENCODED = "encoded-password";

  private RegisterRequest buildRequest() {
    return RegisterRequest.builder().email(EMAIL).username(USERNAME).password(PASSWORD).build();
  }

  private UserEntity buildUserEntity() {
    return UserEntity.builder()
        .email(EMAIL)
        .username(USERNAME)
        .password(ENCODED)
        .role(UserRole.USER)
        .build();
  }

  // =========================================================================
  // registerUser
  // =========================================================================
  @Nested
  @DisplayName("registerUser()")
  class RegisterUser {

    @Nested
    @DisplayName("when the request is valid")
    class WhenRequestIsValid {

      @Test
      @DisplayName("should encode the password before persisting")
      void shouldEncodePasswordBeforePersisting() {
        final RegisterRequest request = buildRequest();
        UserEntity entity = buildUserEntity();

        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(userRepository.existsByUsername(USERNAME)).thenReturn(false);
        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED);
        when(processorRegisterUser.userRegister(USERNAME, EMAIL, ENCODED, UserRole.USER))
            .thenReturn(entity);
        when(userMapper.toRegisterResponse(entity)).thenReturn(RegisterResponse.builder().build());

        userService.registerUser(request);

        verify(passwordEncoder).encode(PASSWORD);
        verify(processorRegisterUser).userRegister(USERNAME, EMAIL, ENCODED, UserRole.USER);
      }

      @Test
      @DisplayName("should always register with role USER")
      void shouldRegisterWithRoleUser() {
        final RegisterRequest request = buildRequest();
        UserEntity entity = buildUserEntity();

        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(userRepository.existsByUsername(USERNAME)).thenReturn(false);
        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED);
        when(processorRegisterUser.userRegister(any(), any(), any(), any())).thenReturn(entity);
        when(userMapper.toRegisterResponse(entity)).thenReturn(RegisterResponse.builder().build());

        userService.registerUser(request);

        verify(processorRegisterUser)
            .userRegister(anyString(), anyString(), anyString(), eq(UserRole.USER));
      }

      @Test
      @DisplayName("should return the response mapped from the created entity")
      void shouldReturnMappedRegisterResponse() {
        RegisterRequest request = buildRequest();
        UserEntity entity = buildUserEntity();
        RegisterResponse expectedResponse =
            RegisterResponse.builder().username(USERNAME).email(EMAIL).build();

        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(userRepository.existsByUsername(USERNAME)).thenReturn(false);
        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED);
        when(processorRegisterUser.userRegister(USERNAME, EMAIL, ENCODED, UserRole.USER))
            .thenReturn(entity);
        when(userMapper.toRegisterResponse(entity)).thenReturn(expectedResponse);

        RegisterResponse actual = userService.registerUser(request);

        assertThat(actual).isEqualTo(expectedResponse);
      }

      @Test
      @DisplayName("should delegate entity creation to processorRegisterUser")
      void shouldDelegateCreationToProcessor() {
        final RegisterRequest request = buildRequest();
        UserEntity entity = buildUserEntity();

        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(userRepository.existsByUsername(USERNAME)).thenReturn(false);
        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED);
        when(processorRegisterUser.userRegister(USERNAME, EMAIL, ENCODED, UserRole.USER))
            .thenReturn(entity);
        when(userMapper.toRegisterResponse(entity)).thenReturn(RegisterResponse.builder().build());

        userService.registerUser(request);

        verify(processorRegisterUser, times(1))
            .userRegister(USERNAME, EMAIL, ENCODED, UserRole.USER);
        verify(userMapper, times(1)).toRegisterResponse(entity);
      }
    }

    // -------------------------------------------------------------------------
    // Email conflict
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("when the email is already in use")
    class WhenEmailAlreadyExists {

      @Test
      @DisplayName("should throw ResourceConflictException with descriptive message")
      void shouldThrowResourceConflictException() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser(buildRequest()))
            .isInstanceOf(ResourceConflictException.class)
            .hasMessage("Email is already in use.");
      }

      @Test
      @DisplayName("should not check username when email is already taken")
      void shouldNotCheckUsernameWhenEmailConflicts() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser(buildRequest()))
            .isInstanceOf(ResourceConflictException.class);

        verify(userRepository, never()).existsByUsername(anyString());
      }

      @Test
      @DisplayName("should not encode password or persist when email conflicts")
      void shouldNotPersistWhenEmailConflicts() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser(buildRequest()))
            .isInstanceOf(ResourceConflictException.class);

        verify(passwordEncoder, never()).encode(anyString());
        verify(processorRegisterUser, never()).userRegister(any(), any(), any(), any());
        verify(userMapper, never()).toRegisterResponse(any());
      }
    }

    // -------------------------------------------------------------------------
    // Username conflict
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("when the username is already in use")
    class WhenUsernameAlreadyExists {

      @Test
      @DisplayName("should throw ResourceConflictException with descriptive message")
      void shouldThrowResourceConflictException() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(userRepository.existsByUsername(USERNAME)).thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser(buildRequest()))
            .isInstanceOf(ResourceConflictException.class)
            .hasMessage("Username is already in use.");
      }

      @Test
      @DisplayName("should check email before username (email has priority)")
      void shouldCheckEmailBeforeUsername() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(userRepository.existsByUsername(USERNAME)).thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser(buildRequest()))
            .isInstanceOf(ResourceConflictException.class);

        // Email check must happen before username check
        InOrder inOrder = inOrder(userRepository);
        inOrder.verify(userRepository).existsByEmail(EMAIL);
        inOrder.verify(userRepository).existsByUsername(USERNAME);
      }

      @Test
      @DisplayName("should not encode password or persist when username conflicts")
      void shouldNotPersistWhenUsernameConflicts() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(userRepository.existsByUsername(USERNAME)).thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser(buildRequest()))
            .isInstanceOf(ResourceConflictException.class);

        verify(passwordEncoder, never()).encode(anyString());
        verify(processorRegisterUser, never()).userRegister(any(), any(), any(), any());
        verify(userMapper, never()).toRegisterResponse(any());
      }
    }
  }

  @Nested
  @DisplayName("loginUser()")
  class LoginUser {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String EMAIL = "john@example.com";
    private static final String PASSWORD = "RawPass@1";
    private static final String USERNAME = "johndoe";
    private static final String TOKEN = "signed.jwt.token";
    private static final String TOKEN_TYPE = "Bearer";
    private static final long EXPIRES_IN = 3600L;

    private LoginRequest buildRequest() {
      return LoginRequest.builder().email(EMAIL).password(PASSWORD).build();
    }

    private UserEntity buildUserEntity() {
      return UserEntity.builder()
          .id(USER_ID)
          .email(EMAIL)
          .username(USERNAME)
          .password("encoded")
          .role(UserRole.USER)
          .build();
    }

    @Nested
    @DisplayName("when credentials are valid")
    class WhenCredentialsAreValid {

      @Test
      @DisplayName("should return a non-null LoginResponse")
      void shouldReturnNonNullLoginResponse() {
        UserEntity user = buildUserEntity();
        when(processorLoginUser.authenticate(EMAIL, PASSWORD)).thenReturn(user);
        when(jwtService.generateToken(USER_ID, USERNAME, UserRole.USER.name())).thenReturn(TOKEN);
        when(jwtProperties.getTokenType()).thenReturn(TOKEN_TYPE);
        when(jwtProperties.getExpiration()).thenReturn(EXPIRES_IN);

        LoginResponse result = userService.loginUser(buildRequest());

        assertThat(result).isNotNull();
      }

      @Test
      @DisplayName("should return the token generated by jwtService")
      void shouldReturnGeneratedToken() {
        UserEntity user = buildUserEntity();
        when(processorLoginUser.authenticate(EMAIL, PASSWORD)).thenReturn(user);
        when(jwtService.generateToken(USER_ID, USERNAME, UserRole.USER.name())).thenReturn(TOKEN);
        when(jwtProperties.getTokenType()).thenReturn(TOKEN_TYPE);
        when(jwtProperties.getExpiration()).thenReturn(EXPIRES_IN);

        LoginResponse result = userService.loginUser(buildRequest());

        assertThat(result.getToken()).isEqualTo(TOKEN);
      }

      @Test
      @DisplayName("should return the token type from jwtProperties")
      void shouldReturnTokenTypeFromProperties() {
        UserEntity user = buildUserEntity();
        when(processorLoginUser.authenticate(EMAIL, PASSWORD)).thenReturn(user);
        when(jwtService.generateToken(USER_ID, USERNAME, UserRole.USER.name())).thenReturn(TOKEN);
        when(jwtProperties.getTokenType()).thenReturn(TOKEN_TYPE);
        when(jwtProperties.getExpiration()).thenReturn(EXPIRES_IN);

        LoginResponse result = userService.loginUser(buildRequest());

        assertThat(result.getTokenType()).isEqualTo(TOKEN_TYPE);
      }

      @Test
      @DisplayName("should return the expiration from jwtProperties")
      void shouldReturnExpirationFromProperties() {
        UserEntity user = buildUserEntity();
        when(processorLoginUser.authenticate(EMAIL, PASSWORD)).thenReturn(user);
        when(jwtService.generateToken(USER_ID, USERNAME, UserRole.USER.name())).thenReturn(TOKEN);
        when(jwtProperties.getTokenType()).thenReturn(TOKEN_TYPE);
        when(jwtProperties.getExpiration()).thenReturn(EXPIRES_IN);

        LoginResponse result = userService.loginUser(buildRequest());

        assertThat(result.getExpiresIn()).isEqualTo(EXPIRES_IN);
      }

      @Test
      @DisplayName("should return the userId from the authenticated entity")
      void shouldReturnUserIdFromAuthenticatedEntity() {
        UserEntity user = buildUserEntity();
        when(processorLoginUser.authenticate(EMAIL, PASSWORD)).thenReturn(user);
        when(jwtService.generateToken(USER_ID, USERNAME, UserRole.USER.name())).thenReturn(TOKEN);
        when(jwtProperties.getTokenType()).thenReturn(TOKEN_TYPE);
        when(jwtProperties.getExpiration()).thenReturn(EXPIRES_IN);

        LoginResponse result = userService.loginUser(buildRequest());

        assertThat(result.getUserId()).isEqualTo(USER_ID);
      }

      @Test
      @DisplayName("should call authenticate with the email and password from the request")
      void shouldCallAuthenticateWithRequestCredentials() {
        UserEntity user = buildUserEntity();
        when(processorLoginUser.authenticate(EMAIL, PASSWORD)).thenReturn(user);
        when(jwtService.generateToken(USER_ID, USERNAME, UserRole.USER.name())).thenReturn(TOKEN);
        when(jwtProperties.getTokenType()).thenReturn(TOKEN_TYPE);
        when(jwtProperties.getExpiration()).thenReturn(EXPIRES_IN);

        userService.loginUser(buildRequest());

        verify(processorLoginUser, times(1)).authenticate(EMAIL, PASSWORD);
      }

      @Test
      @DisplayName("should generate token using the authenticated user's id, username and role")
      void shouldGenerateTokenWithUserIdUsernameAndRole() {
        UserEntity user = buildUserEntity();
        when(processorLoginUser.authenticate(EMAIL, PASSWORD)).thenReturn(user);
        when(jwtService.generateToken(USER_ID, USERNAME, UserRole.USER.name())).thenReturn(TOKEN);
        when(jwtProperties.getTokenType()).thenReturn(TOKEN_TYPE);
        when(jwtProperties.getExpiration()).thenReturn(EXPIRES_IN);

        userService.loginUser(buildRequest());

        verify(jwtService, times(1)).generateToken(USER_ID, USERNAME, UserRole.USER.name());
      }

      @Test
      @DisplayName("should pass the role name as a string to jwtService")
      void shouldPassRoleNameAsStringToJwtService() {
        UserEntity user = buildUserEntity();
        when(processorLoginUser.authenticate(EMAIL, PASSWORD)).thenReturn(user);
        when(jwtService.generateToken(USER_ID, USERNAME, "USER")).thenReturn(TOKEN);
        when(jwtProperties.getTokenType()).thenReturn(TOKEN_TYPE);
        when(jwtProperties.getExpiration()).thenReturn(EXPIRES_IN);

        userService.loginUser(buildRequest());

        verify(jwtService).generateToken(USER_ID, USERNAME, "USER");
      }
    }

    @Nested
    @DisplayName("when authentication fails")
    class WhenAuthenticationFails {

      @Test
      @DisplayName("should propagate UnauthorizedException from processorLoginUser")
      void shouldPropagateUnauthorizedException() {
        when(processorLoginUser.authenticate(EMAIL, PASSWORD))
            .thenThrow(new UnauthorizedException("Invalid credentials"));

        assertThatThrownBy(() -> userService.loginUser(buildRequest()))
            .isInstanceOf(UnauthorizedException.class)
            .hasMessage("Invalid credentials");
      }

      @Test
      @DisplayName("should not call jwtService when authentication fails")
      void shouldNotCallJwtServiceWhenAuthenticationFails() {
        when(processorLoginUser.authenticate(EMAIL, PASSWORD))
            .thenThrow(new UnauthorizedException("Invalid credentials"));

        assertThatThrownBy(() -> userService.loginUser(buildRequest()))
            .isInstanceOf(UnauthorizedException.class);

        verify(jwtService, never()).generateToken(any(), anyString(), anyString());
      }

      @Test
      @DisplayName("should not call jwtProperties when authentication fails")
      void shouldNotCallJwtPropertiesWhenAuthenticationFails() {
        when(processorLoginUser.authenticate(EMAIL, PASSWORD))
            .thenThrow(new UnauthorizedException("Invalid credentials"));

        assertThatThrownBy(() -> userService.loginUser(buildRequest()))
            .isInstanceOf(UnauthorizedException.class);

        verify(jwtProperties, never()).getTokenType();
        verify(jwtProperties, never()).getExpiration();
      }
    }
  }

  @Nested
  @DisplayName("valiadteToken()")
  class ValidateToken {

    @Test
    @DisplayName("tokenValidate() throws UnauthorizedException when Authorization header is null")
    void tokenValidate_throwsUnauthorizedException_whenHeaderIsNull() {
      assertThatThrownBy(() -> userService.tokenValidate(null))
          .isInstanceOf(UnauthorizedException.class)
          .hasMessageContaining("Missing or malformed Authorization header");
    }

    @Test
    @DisplayName(
        "tokenValidate() throws UnauthorizedException when "
            + "header does not start with token type prefix")
    void tokenValidate_throwsUnauthorizedException_whenHeaderIsMalformed() {
      when(jwtProperties.getTokenType()).thenReturn("Bearer");

      assertThatThrownBy(() -> userService.tokenValidate("Basic sometoken"))
          .isInstanceOf(UnauthorizedException.class)
          .hasMessageContaining("Missing or malformed Authorization header");
    }

    @Test
    @DisplayName(
        "tokenValidate() throws UnauthorizedException when header has correct prefix but no token")
    void tokenValidate_throwsUnauthorizedException_whenHeaderHasOnlyPrefix() {
      when(jwtProperties.getTokenType()).thenReturn("Bearer");

      assertThatThrownBy(() -> userService.tokenValidate("Bearer"))
          .isInstanceOf(UnauthorizedException.class)
          .hasMessageContaining("Missing or malformed Authorization header");
    }

    @Test
    @DisplayName("tokenValidate() strips the prefix and delegates to jwtService with the raw token")
    void tokenValidate_stripsPrefix_andDelegatesToJwtService() {
      String rawToken = "eyJhbGciOiJIUzI1NiJ9.payload.signature";
      String header = "Bearer " + rawToken;
      TokenValidationResponse expected =
          TokenValidationResponse.builder()
              .valid(true)
              .userId(UUID.randomUUID())
              .username("john.doe")
              .role(UserRole.USER)
              .build();

      when(jwtProperties.getTokenType()).thenReturn("Bearer");
      when(jwtService.extractUserInfo(rawToken)).thenReturn(expected);

      TokenValidationResponse result = userService.tokenValidate(header);

      assertThat(result).isEqualTo(expected);
      verify(jwtService).extractUserInfo(rawToken);
    }

    @Test
    @DisplayName("tokenValidate() propagates JwtException thrown by jwtService")
    void tokenValidate_propagatesJwtException_whenTokenIsInvalid() {
      String rawToken = "invalid.token.here";
      String header = "Bearer " + rawToken;

      when(jwtProperties.getTokenType()).thenReturn("Bearer");
      when(jwtService.extractUserInfo(rawToken))
          .thenThrow(new io.jsonwebtoken.JwtException("bad token"));

      assertThatThrownBy(() -> userService.tokenValidate(header))
          .isInstanceOf(io.jsonwebtoken.JwtException.class)
          .hasMessageContaining("bad token");
    }
  }
}
