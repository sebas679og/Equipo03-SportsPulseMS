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

import com.sportspulse.auth.dto.requests.RegisterRequest;
import com.sportspulse.auth.dto.responses.RegisterResponse;
import com.sportspulse.auth.exceptions.ResourceConflictException;
import com.sportspulse.auth.models.UserEntity;
import com.sportspulse.auth.repositories.UserRepository;
import com.sportspulse.auth.services.processors.ProcessorRegisterUser;
import com.sportspulse.auth.utils.enums.UserRole;
import com.sportspulse.auth.utils.mappers.UserMapper;
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
  @Mock private UserMapper userMapper;

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
}
