package com.sportspulse.auth.services.processors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sportspulse.auth.exceptions.UnauthorizedException;
import com.sportspulse.auth.models.UserEntity;
import com.sportspulse.auth.repositories.UserRepository;
import com.sportspulse.auth.utils.enums.UserRole;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessorLoginUser")
class ProcessorLoginUserTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private ProcessorLoginUser processorLoginUser;

  // -------------------------------------------------------------------------
  // Fixtures
  // -------------------------------------------------------------------------
  private static final String EMAIL = "john@example.com";
  private static final String RAW_PASSWORD = "RawPass@1";
  private static final String ENCODED = "encoded-password";

  private UserEntity buildUserEntity() {
    return UserEntity.builder().email(EMAIL).password(ENCODED).role(UserRole.USER).build();
  }

  // =========================================================================
  // authenticate()
  // =========================================================================
  @Nested
  @DisplayName("authenticate()")
  class Authenticate {

    @Nested
    @DisplayName("when credentials are valid")
    class WhenCredentialsAreValid {

      @Test
      @DisplayName("should return the authenticated UserEntity")
      void shouldReturnAuthenticatedUser() {
        UserEntity user = buildUserEntity();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(RAW_PASSWORD, ENCODED)).thenReturn(true);

        UserEntity result = processorLoginUser.authenticate(EMAIL, RAW_PASSWORD);

        assertThat(result).isNotNull().isEqualTo(user);
      }

      @Test
      @DisplayName("should return the entity with all original fields intact")
      void shouldReturnEntityWithOriginalFieldsIntact() {
        UserEntity user = buildUserEntity();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(RAW_PASSWORD, ENCODED)).thenReturn(true);

        UserEntity result = processorLoginUser.authenticate(EMAIL, RAW_PASSWORD);

        assertThat(result.getEmail()).isEqualTo(EMAIL);
        assertThat(result.getPassword()).isEqualTo(ENCODED);
        assertThat(result.getRole()).isEqualTo(UserRole.USER);
      }

      @Test
      @DisplayName("should look up the user by the provided email")
      void shouldLookUpUserByEmail() {
        UserEntity user = buildUserEntity();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(RAW_PASSWORD, ENCODED)).thenReturn(true);

        processorLoginUser.authenticate(EMAIL, RAW_PASSWORD);

        verify(userRepository, times(1)).findByEmail(EMAIL);
      }

      @Test
      @DisplayName("should validate the raw password against the stored encoded password")
      void shouldValidateRawPasswordAgainstEncodedPassword() {
        UserEntity user = buildUserEntity();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(RAW_PASSWORD, ENCODED)).thenReturn(true);

        processorLoginUser.authenticate(EMAIL, RAW_PASSWORD);

        verify(passwordEncoder, times(1)).matches(RAW_PASSWORD, ENCODED);
      }
    }

    @Nested
    @DisplayName("when the user does not exist")
    class WhenUserDoesNotExist {

      @Test
      @DisplayName("should throw UnauthorizedException when email is not found")
      void shouldThrowUnauthorizedExceptionWhenEmailNotFound() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> processorLoginUser.authenticate(EMAIL, RAW_PASSWORD))
            .isInstanceOf(UnauthorizedException.class)
            .hasMessage("Invalid credentials");
      }

      @Test
      @DisplayName("should not invoke passwordEncoder when user is not found")
      void shouldNotInvokePasswordEncoderWhenUserNotFound() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> processorLoginUser.authenticate(EMAIL, RAW_PASSWORD))
            .isInstanceOf(UnauthorizedException.class);

        verify(passwordEncoder, never()).matches(anyString(), anyString());
      }
    }

    @Nested
    @DisplayName("when the password is wrong")
    class WhenPasswordIsWrong {

      @Test
      @DisplayName("should throw UnauthorizedException when password does not match")
      void shouldThrowUnauthorizedExceptionWhenPasswordDoesNotMatch() {
        UserEntity user = buildUserEntity();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(RAW_PASSWORD, ENCODED)).thenReturn(false);

        assertThatThrownBy(() -> processorLoginUser.authenticate(EMAIL, RAW_PASSWORD))
            .isInstanceOf(UnauthorizedException.class)
            .hasMessage("Invalid credentials");
      }

      @Test
      @DisplayName("should still look up the user even when the password is wrong")
      void shouldStillLookUpUserWhenPasswordIsWrong() {
        UserEntity user = buildUserEntity();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(RAW_PASSWORD, ENCODED)).thenReturn(false);

        assertThatThrownBy(() -> processorLoginUser.authenticate(EMAIL, RAW_PASSWORD))
            .isInstanceOf(UnauthorizedException.class);

        verify(userRepository, times(1)).findByEmail(EMAIL);
      }

      @Test
      @DisplayName("should invoke passwordEncoder even when the password is wrong")
      void shouldInvokePasswordEncoderWhenPasswordIsWrong() {
        UserEntity user = buildUserEntity();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(RAW_PASSWORD, ENCODED)).thenReturn(false);

        assertThatThrownBy(() -> processorLoginUser.authenticate(EMAIL, RAW_PASSWORD))
            .isInstanceOf(UnauthorizedException.class);

        verify(passwordEncoder, times(1)).matches(RAW_PASSWORD, ENCODED);
      }
    }

    @Nested
    @DisplayName("security invariants")
    class SecurityInvariants {

      @Test
      @DisplayName(
          "should throw the same exception for missing user and "
              + "wrong password (no user enumeration)")
      void shouldThrowSameExceptionForMissingUserAndWrongPassword() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> processorLoginUser.authenticate(EMAIL, RAW_PASSWORD))
            .isInstanceOf(UnauthorizedException.class)
            .hasMessage("Invalid credentials");

        UserEntity user = buildUserEntity();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(RAW_PASSWORD, ENCODED)).thenReturn(false);

        assertThatThrownBy(() -> processorLoginUser.authenticate(EMAIL, RAW_PASSWORD))
            .isInstanceOf(UnauthorizedException.class)
            .hasMessage("Invalid credentials");
      }

      @Test
      @DisplayName("should never return null — always throws or returns a valid entity")
      void shouldNeverReturnNull() {
        UserEntity user = buildUserEntity();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(RAW_PASSWORD, ENCODED)).thenReturn(true);

        UserEntity result = processorLoginUser.authenticate(EMAIL, RAW_PASSWORD);

        assertThat(result).isNotNull();
      }
    }
  }
}
