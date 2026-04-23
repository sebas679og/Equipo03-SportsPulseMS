package com.sportspulse.leagues.services.components;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("JwtTokenService Tests")
class JwtTokenServiceTest {

  private static final SecretKey SIGNING_KEY =
      Keys.hmacShaKeyFor("this-is-a-test-secret-key-with-enough-length-1234".getBytes(StandardCharsets.UTF_8));

  private final JwtTokenService jwtTokenService = new JwtTokenService(SIGNING_KEY);

  @Test
  @DisplayName("validateAndExtract should return claims for a valid token")
  void validateAndExtract_shouldReturnClaims() {
    String token =
        Jwts.builder().claim("username", "ana").claim("role", "USER").signWith(SIGNING_KEY).compact();

    Claims claims = jwtTokenService.validateAndExtract(token);

    assertThat(claims.get("username", String.class)).isEqualTo("ana");
    assertThat(claims.get("role", String.class)).isEqualTo("USER");
  }

  @Test
  @DisplayName("validateAndExtract should throw for an invalid token")
  void validateAndExtract_shouldThrowForInvalidToken() {
    assertThatThrownBy(() -> jwtTokenService.validateAndExtract("invalid.jwt.token"))
        .isInstanceOf(RuntimeException.class);
  }
}
