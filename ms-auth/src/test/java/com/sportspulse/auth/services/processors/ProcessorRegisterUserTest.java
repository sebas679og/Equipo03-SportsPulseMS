package com.sportspulse.auth.services.processors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sportspulse.auth.models.UserEntity;
import com.sportspulse.auth.repositories.UserRepository;
import com.sportspulse.auth.utils.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for user record processor in the database. */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessorRegisterUser")
class ProcessorRegisterUserTest {

  @Mock private UserRepository userRepository;

  @InjectMocks private ProcessorRegisterUser processorRegisterUser;

  // -------------------------------------------------------------------------
  // Fixtures
  // -------------------------------------------------------------------------
  private static final String USERNAME = "johndoe";
  private static final String EMAIL = "john@example.com";
  private static final String PASSWORD = "encoded-password";
  private static final UserRole ROLE = UserRole.USER;

  private UserEntity buildExpectedEntity() {
    return UserEntity.builder()
        .username(USERNAME)
        .email(EMAIL)
        .password(PASSWORD)
        .role(ROLE)
        .build();
  }

  // =========================================================================
  // userRegister()
  // =========================================================================
  @Nested
  @DisplayName("userRegister()")
  class UserRegister {

    @Nested
    @DisplayName("when all parameters are valid")
    class WhenParametersAreValid {

      @Test
      @DisplayName("should build the entity with all provided fields")
      void shouldBuildEntityWithAllProvidedFields() {
        UserEntity savedEntity = buildExpectedEntity();
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedEntity);

        processorRegisterUser.userRegister(USERNAME, EMAIL, PASSWORD, ROLE);

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());

        UserEntity captured = captor.getValue();
        assertThat(captured.getUsername()).isEqualTo(USERNAME);
        assertThat(captured.getEmail()).isEqualTo(EMAIL);
        assertThat(captured.getPassword()).isEqualTo(PASSWORD);
        assertThat(captured.getRole()).isEqualTo(ROLE);
      }

      @Test
      @DisplayName("should delegate persistence to userRepository.save()")
      void shouldDelegatePersistenceToRepository() {
        UserEntity savedEntity = buildExpectedEntity();
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedEntity);

        processorRegisterUser.userRegister(USERNAME, EMAIL, PASSWORD, ROLE);

        verify(userRepository, times(1)).save(any(UserEntity.class));
      }

      @Test
      @DisplayName("should return the entity returned by the repository")
      void shouldReturnEntityFromRepository() {
        UserEntity savedEntity = buildExpectedEntity();
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedEntity);

        UserEntity result = processorRegisterUser.userRegister(USERNAME, EMAIL, PASSWORD, ROLE);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(savedEntity);
      }

      @Test
      @DisplayName("should map each field correctly to the persisted entity")
      void shouldMapEachFieldCorrectlyToPersistedEntity() {
        UserEntity savedEntity = buildExpectedEntity();
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedEntity);

        UserEntity result = processorRegisterUser.userRegister(USERNAME, EMAIL, PASSWORD, ROLE);

        assertThat(result.getUsername()).isEqualTo(USERNAME);
        assertThat(result.getEmail()).isEqualTo(EMAIL);
        assertThat(result.getPassword()).isEqualTo(PASSWORD);
        assertThat(result.getRole()).isEqualTo(ROLE);
      }

      @Test
      @DisplayName("should save exactly one entity per call")
      void shouldSaveExactlyOneEntityPerCall() {
        when(userRepository.save(any(UserEntity.class))).thenReturn(buildExpectedEntity());

        processorRegisterUser.userRegister(USERNAME, EMAIL, PASSWORD, ROLE);
        processorRegisterUser.userRegister("jane", "jane@example.com", PASSWORD, ROLE);

        verify(userRepository, times(2)).save(any(UserEntity.class));
      }

      @Test
      @DisplayName("should support any UserRole value")
      void shouldSupportAnyUserRole() {
        for (UserRole role : UserRole.values()) {
          UserEntity entityForRole =
              UserEntity.builder()
                  .username(USERNAME)
                  .email(EMAIL)
                  .password(PASSWORD)
                  .role(role)
                  .build();

          when(userRepository.save(any(UserEntity.class))).thenReturn(entityForRole);

          processorRegisterUser.userRegister(USERNAME, EMAIL, PASSWORD, role);

          ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
          verify(userRepository, atLeastOnce()).save(captor.capture());
          assertThat(captor.getValue().getRole()).isEqualTo(role);

          clearInvocations(userRepository);
        }
      }
    }

    // -------------------------------------------------------------------------
    // Repository failure
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("when the repository throws an exception")
    class WhenRepositoryFails {

      @Test
      @DisplayName("should propagate DataIntegrityViolationException from repository")
      void shouldPropagateDataIntegrityViolationException() {
        when(userRepository.save(any(UserEntity.class)))
            .thenThrow(
                new org.springframework.dao.DataIntegrityViolationException(
                    "Unique constraint violated"));

        assertThatThrownBy(
                () -> processorRegisterUser.userRegister(USERNAME, EMAIL, PASSWORD, ROLE))
            .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class)
            .hasMessageContaining("Unique constraint violated");
      }

      @Test
      @DisplayName("should not swallow unexpected exceptions from repository")
      void shouldNotSwallowUnexpectedExceptions() {
        when(userRepository.save(any(UserEntity.class)))
            .thenThrow(new RuntimeException("Unexpected DB failure"));

        assertThatThrownBy(
                () -> processorRegisterUser.userRegister(USERNAME, EMAIL, PASSWORD, ROLE))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Unexpected DB failure");
      }
    }
  }
}
