package com.sportspulse.auth.utils.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import com.sportspulse.auth.dto.responses.RegisterResponse;
import com.sportspulse.auth.models.UserEntity;
import com.sportspulse.auth.utils.enums.UserRole;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

/** Unit tests for UserMapper. */
@DisplayName("UserMapper")
class UserMapperTest {

  private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

  // -------------------------------------------------------------------------
  // Fixtures
  // -------------------------------------------------------------------------
  private static final UUID ID = UUID.randomUUID();
  private static final String USERNAME = "johndoe";
  private static final String EMAIL = "john@example.com";
  private static final String PASSWORD = "encoded-password";
  private static final UserRole ROLE = UserRole.USER;

  private UserEntity buildUserEntity() {
    return UserEntity.builder()
        .id(ID)
        .username(USERNAME)
        .email(EMAIL)
        .password(PASSWORD)
        .role(ROLE)
        .createdAt(Instant.now().truncatedTo(ChronoUnit.MILLIS))
        .updatedAt(Instant.now().truncatedTo(ChronoUnit.MILLIS))
        .build();
  }

  // =========================================================================
  // toRegisterResponse()
  // =========================================================================
  @Nested
  @DisplayName("toRegisterResponse()")
  class ToRegisterResponse {

    @Nested
    @DisplayName("when the entity is valid")
    class WhenEntityIsValid {

      @Test
      @DisplayName("should return a non-null response")
      void shouldReturnNonNullResponse() {
        RegisterResponse result = userMapper.toRegisterResponse(buildUserEntity());

        assertThat(result).isNotNull();
      }

      @Test
      @DisplayName("should map username correctly")
      void shouldMapUsernameCorrectly() {
        RegisterResponse result = userMapper.toRegisterResponse(buildUserEntity());

        assertThat(result.getUsername()).isEqualTo(USERNAME);
      }

      @Test
      @DisplayName("should map email correctly")
      void shouldMapEmailCorrectly() {
        RegisterResponse result = userMapper.toRegisterResponse(buildUserEntity());

        assertThat(result.getEmail()).isEqualTo(EMAIL);
      }

      @Test
      @DisplayName("should not expose the password in the response")
      void shouldNotExposePassword() {
        RegisterResponse result = userMapper.toRegisterResponse(buildUserEntity());

        // RegisterResponse must not carry the password field
        assertThat(result).doesNotHaveToString(PASSWORD);
      }

      @Test
      @DisplayName("should produce a consistent result for the same input")
      void shouldProduceConsistentResultForSameInput() {
        UserEntity entity = buildUserEntity();

        RegisterResponse first = userMapper.toRegisterResponse(entity);
        RegisterResponse second = userMapper.toRegisterResponse(entity);

        assertThat(first).usingRecursiveComparison().isEqualTo(second);
      }

      @Test
      @DisplayName("should map different entities to different responses")
      void shouldMapDifferentEntitiesToDifferentResponses() {
        UserEntity entityA = buildUserEntity();
        UserEntity entityB =
            UserEntity.builder()
                .id(UUID.randomUUID())
                .username("janedoe")
                .email("jane@example.com")
                .password(PASSWORD)
                .role(ROLE)
                .build();

        RegisterResponse responseA = userMapper.toRegisterResponse(entityA);
        RegisterResponse responseB = userMapper.toRegisterResponse(entityB);

        assertThat(responseA).usingRecursiveComparison().isNotEqualTo(responseB);
      }
    }

    // -------------------------------------------------------------------------
    // Null input
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("when the entity is null")
    class WhenEntityIsNull {

      @Test
      @DisplayName("should return null when entity is null (MapStruct default behavior)")
      void shouldReturnNullForNullEntity() {
        RegisterResponse result = userMapper.toRegisterResponse(null);

        assertThat(result).isNull();
      }
    }

    // -------------------------------------------------------------------------
    // Partial / edge-case entities
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("when the entity has null fields")
    class WhenEntityHasNullFields {

      @Test
      @DisplayName("should map gracefully when username is null")
      void shouldMapGracefullyWhenUsernameIsNull() {
        UserEntity entity = buildUserEntity();
        entity.setUsername(null);

        RegisterResponse result = userMapper.toRegisterResponse(entity);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isNull();
      }

      @Test
      @DisplayName("should map gracefully when email is null")
      void shouldMapGracefullyWhenEmailIsNull() {
        UserEntity entity = buildUserEntity();
        entity.setEmail(null);

        RegisterResponse result = userMapper.toRegisterResponse(entity);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isNull();
      }

      @Test
      @DisplayName("should map gracefully when all fields are null")
      void shouldMapGracefullyWhenAllFieldsAreNull() {
        UserEntity emptyEntity = UserEntity.builder().build();

        RegisterResponse result = userMapper.toRegisterResponse(emptyEntity);

        assertThat(result).isNotNull();
      }
    }
  }
}
