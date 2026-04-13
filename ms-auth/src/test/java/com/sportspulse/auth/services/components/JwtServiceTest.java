package com.sportspulse.auth.services.components;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.sportspulse.auth.config.properties.JwtProperties;
import com.sportspulse.auth.config.SecurityConfig;
import com.sportspulse.auth.dto.responses.TokenValidationResponse;
import com.sportspulse.auth.utils.enums.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtService")
class JwtServiceTest {

  @Mock private JwtProperties jwtProperties;
  @Mock private SecurityConfig config;

  @InjectMocks private JwtService jwtService;

  // -------------------------------------------------------------------------
  // Fixtures
  // -------------------------------------------------------------------------
  private static final UUID USER_ID = UUID.randomUUID();
  private static final String USERNAME = "johndoe";
  private static final String ROLE = "USER";
  private static final long EXPIRATION_SECONDS = 3600L;

  // Valid Base64-encoded 256-bit key
  private static final String SECRET_BASE64 =
      Base64.getEncoder()
          .encodeToString(
              "super-secret-key-for-testing-purposes-only!!!!".getBytes(StandardCharsets.UTF_8));

  private SecretKey signingKey;

  @BeforeEach
  void setUp() {
    byte[] keyBytes = Decoders.BASE64.decode(SECRET_BASE64);
    signingKey = Keys.hmacShaKeyFor(keyBytes);
    // lenient: only consumed by tests that call generateToken or validateAndExtract
    lenient().when(config.getSigningKey()).thenReturn(signingKey);
    lenient().when(jwtProperties.getExpiration()).thenReturn(EXPIRATION_SECONDS);
  }

  // =========================================================================
  // generateToken()
  // =========================================================================
  @Nested
  @DisplayName("generateToken()")
  class GenerateToken {

    @Nested
    @DisplayName("when all parameters are valid")
    class WhenParametersAreValid {

      @Test
      @DisplayName("should return a non-null, non-blank token")
      void shouldReturnNonNullNonBlankToken() {
        String token = jwtService.generateToken(USER_ID, USERNAME, ROLE);

        assertThat(token).isNotNull().isNotBlank();
      }

      @Test
      @DisplayName("should return a token with three JWT parts (header.payload.signature)")
      void shouldReturnWellFormedJwtStructure() {
        String token = jwtService.generateToken(USER_ID, USERNAME, ROLE);

        assertThat(token.split("\\.")).hasSize(3);
      }

      @Test
      @DisplayName("should embed the userId as the subject claim")
      void shouldEmbedUserIdAsSubject() {
        String token = jwtService.generateToken(USER_ID, USERNAME, ROLE);
        Claims claims = jwtService.validateAndExtract(token);

        assertThat(claims.getSubject()).isEqualTo(USER_ID.toString());
      }

      @Test
      @DisplayName("should embed the username claim correctly")
      void shouldEmbedUsernameClaim() {
        String token = jwtService.generateToken(USER_ID, USERNAME, ROLE);
        Claims claims = jwtService.validateAndExtract(token);

        assertThat(claims.get("username", String.class)).isEqualTo(USERNAME);
      }

      @Test
      @DisplayName("should embed the role claim correctly")
      void shouldEmbedRoleClaim() {
        String token = jwtService.generateToken(USER_ID, USERNAME, ROLE);
        Claims claims = jwtService.validateAndExtract(token);

        assertThat(claims.get("role", String.class)).isEqualTo(ROLE);
      }

      @Test
      @DisplayName("should set issuedAt to approximately now")
      void shouldSetIssuedAtToNow() {
        long before = Instant.now().truncatedTo(ChronoUnit.SECONDS).toEpochMilli();
        String token = jwtService.generateToken(USER_ID, USERNAME, ROLE);
        long after = System.currentTimeMillis();

        Claims claims = jwtService.validateAndExtract(token);
        long issuedAt = claims.getIssuedAt().getTime();

        assertThat(issuedAt).isBetween(before, after);
      }

      @Test
      @DisplayName("should set expiration to issuedAt + configured seconds")
      void shouldSetExpirationToIssuedAtPlusConfiguredSeconds() {
        String token = jwtService.generateToken(USER_ID, USERNAME, ROLE);
        Claims claims = jwtService.validateAndExtract(token);

        long issuedAt = claims.getIssuedAt().getTime();
        long expiration = claims.getExpiration().getTime();
        long diffSeconds = (expiration - issuedAt) / 1000;

        assertThat(diffSeconds).isEqualTo(EXPIRATION_SECONDS);
      }

      @Test
      @DisplayName("should generate different tokens for different userIds")
      void shouldGenerateDifferentTokensForDifferentUserIds() {
        String tokenA = jwtService.generateToken(UUID.randomUUID(), USERNAME, ROLE);
        String tokenB = jwtService.generateToken(UUID.randomUUID(), USERNAME, ROLE);

        assertThat(tokenA).isNotEqualTo(tokenB);
      }

      @Test
      @DisplayName("should generate different tokens for different usernames")
      void shouldGenerateDifferentTokensForDifferentUsernames() {
        String tokenA = jwtService.generateToken(USER_ID, "alice", ROLE);
        String tokenB = jwtService.generateToken(USER_ID, "bob", ROLE);

        assertThat(tokenA).isNotEqualTo(tokenB);
      }

      @Test
      @DisplayName("should generate different tokens for different roles")
      void shouldGenerateDifferentTokensForDifferentRoles() {
        String tokenA = jwtService.generateToken(USER_ID, USERNAME, "USER");
        String tokenB = jwtService.generateToken(USER_ID, USERNAME, "ADMIN");

        assertThat(tokenA).isNotEqualTo(tokenB);
      }
    }
  }

  // =========================================================================
  // validateAndExtract()
  // =========================================================================
  @Nested
  @DisplayName("validateAndExtract()")
  class ValidateAndExtract {

    @Nested
    @DisplayName("when the token is valid")
    class WhenTokenIsValid {

      @Test
      @DisplayName("should return non-null claims")
      void shouldReturnNonNullClaims() {
        String token = jwtService.generateToken(USER_ID, USERNAME, ROLE);

        assertThat(jwtService.validateAndExtract(token)).isNotNull();
      }

      @Test
      @DisplayName("should extract subject matching the original userId")
      void shouldExtractSubjectMatchingUserId() {
        String token = jwtService.generateToken(USER_ID, USERNAME, ROLE);

        assertThat(jwtService.validateAndExtract(token).getSubject()).isEqualTo(USER_ID.toString());
      }

      @Test
      @DisplayName("should extract username claim matching the original value")
      void shouldExtractUsernameClaimMatchingOriginalValue() {
        String token = jwtService.generateToken(USER_ID, USERNAME, ROLE);

        assertThat(jwtService.validateAndExtract(token).get("username", String.class))
            .isEqualTo(USERNAME);
      }

      @Test
      @DisplayName("should extract role claim matching the original value")
      void shouldExtractRoleClaimMatchingOriginalValue() {
        String token = jwtService.generateToken(USER_ID, USERNAME, ROLE);

        assertThat(jwtService.validateAndExtract(token).get("role", String.class)).isEqualTo(ROLE);
      }

      @Test
      @DisplayName("should extract a valid expiration date in the future")
      void shouldExtractExpirationInTheFuture() {
        String token = jwtService.generateToken(USER_ID, USERNAME, ROLE);

        assertThat(jwtService.validateAndExtract(token).getExpiration())
            .isAfter(new java.util.Date());
      }
    }

    @Nested
    @DisplayName("when the token is invalid")
    class WhenTokenIsInvalid {

      @Test
      @DisplayName("should throw SignatureException for a token signed with a different key")
      void shouldThrowSignatureExceptionForWrongKey() {
        SecretKey differentKey =
            Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(
                    Base64.getEncoder()
                        .encodeToString(
                            "another-secret-key-totally-different-32b!!"
                                .getBytes(StandardCharsets.UTF_8))));

        String tokenWithWrongKey =
            Jwts.builder()
                .subject(USER_ID.toString())
                .claim("username", USERNAME)
                .claim("role", ROLE)
                .issuedAt(new java.util.Date())
                .expiration(new java.util.Date(System.currentTimeMillis() + 3600_000))
                .signWith(differentKey)
                .compact();

        assertThatThrownBy(() -> jwtService.validateAndExtract(tokenWithWrongKey))
            .isInstanceOf(SignatureException.class);
      }

      @Test
      @DisplayName("should throw MalformedJwtException for a malformed token")
      void shouldThrowMalformedJwtExceptionForMalformedToken() {
        assertThatThrownBy(() -> jwtService.validateAndExtract("not.a.jwt"))
            .isInstanceOf(MalformedJwtException.class);
      }

      @Test
      @DisplayName("should throw IllegalArgumentException for an empty string token")
      void shouldThrowIllegalArgumentExceptionForEmptyToken() {
        assertThatThrownBy(() -> jwtService.validateAndExtract(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cannot be null or empty");
      }

      @Test
      @DisplayName("should throw MalformedJwtException for a token with missing parts")
      void shouldThrowMalformedJwtExceptionForTokenWithMissingParts() {
        assertThatThrownBy(() -> jwtService.validateAndExtract("header.payload"))
            .isInstanceOf(MalformedJwtException.class);
      }

      @Test
      @DisplayName("should throw ExpiredJwtException for an already expired token")
      void shouldThrowExpiredJwtExceptionForExpiredToken() {
        when(jwtProperties.getExpiration()).thenReturn(-1L);

        String expiredToken = jwtService.generateToken(USER_ID, USERNAME, ROLE);

        assertThatThrownBy(() -> jwtService.validateAndExtract(expiredToken))
            .isInstanceOf(ExpiredJwtException.class);
      }
    }

    @Test
    @DisplayName("extractUserInfo() returns a valid TokenValidationResponse with correct user data")
    void extractUserInfo_returnsValidResponse_whenTokenIsValid() {
      UUID userId = UUID.randomUUID();
      String username = "john.doe";
      String role = UserRole.USER.name();

      String token = jwtService.generateToken(userId, username, role);
      TokenValidationResponse result = jwtService.extractUserInfo(token);

      assertThat(result.isValid()).isTrue();
      assertThat(result.getUserId()).isEqualTo(userId);
      assertThat(result.getUsername()).isEqualTo(username);
      assertThat(result.getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    @DisplayName("extractUserInfo() throws JwtException when token is tampered")
    void extractUserInfo_throwsJwtException_whenTokenIsTampered() {
      UUID userId = UUID.randomUUID();
      String token = jwtService.generateToken(userId, "john.doe", UserRole.USER.name());
      String tamperedToken = token.substring(0, token.lastIndexOf('.') + 1) + "invalidsignature";

      assertThatThrownBy(() -> jwtService.extractUserInfo(tamperedToken))
              .isInstanceOf(io.jsonwebtoken.JwtException.class);
    }

    @Test
    @DisplayName("extractUserInfo() throws JwtException when token is expired")
    void extractUserInfo_throwsJwtException_whenTokenIsExpired() {
      // Build an already-expired token manually
      Instant past = Instant.now().truncatedTo(ChronoUnit.MILLIS).minusSeconds(3600);
      String expiredToken =
              Jwts.builder()
                      .subject(UUID.randomUUID().toString())
                      .claim("username", "john.doe")
                      .claim("role", UserRole.USER.name())
                      .issuedAt(Date.from(past))
                      .expiration(Date.from(past.plusSeconds(1)))
                      .signWith(config.getSigningKey())
                      .compact();

      assertThatThrownBy(() -> jwtService.extractUserInfo(expiredToken))
              .isInstanceOf(io.jsonwebtoken.JwtException.class);
    }
  }
}
