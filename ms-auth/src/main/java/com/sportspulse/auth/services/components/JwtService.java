package com.sportspulse.auth.services.components;

import com.sportspulse.auth.config.JwtProperties;
import com.sportspulse.auth.config.SecurityConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** JwtService Provides operations for generating and validating JWT tokens. */
@Service
@RequiredArgsConstructor
public class JwtService {

  private final JwtProperties jwtProperties;
  private final SecurityConfig config;

  /**
   * Generates a JWT token for a given user.
   *
   * @param userId unique identifier of the user
   * @param username username of the user
   * @param role role assigned to the user
   * @return a signed JWT token string
   */
  public String generateToken(UUID userId, String username, String role) {
    Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    return Jwts.builder()
        .subject(userId.toString())
        .claim("username", username)
        .claim("role", role)
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusSeconds(jwtProperties.getExpiration())))
        .signWith(config.getSigningKey())
        .compact();
  }

  /**
   * Validates a JWT token and extracts its claims.
   *
   * @param token the JWT token to validate
   * @return the claims contained in the token
   */
  public Claims validateAndExtract(String token) {
    return Jwts.parser()
        .verifyWith(config.getSigningKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }
}
