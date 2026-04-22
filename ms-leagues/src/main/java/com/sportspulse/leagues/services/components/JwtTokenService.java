package com.sportspulse.leagues.services.components;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Parses and validates JWT tokens. */
@Service
@RequiredArgsConstructor
public class JwtTokenService {

  private final SecretKey signingKey;

  public Claims validateAndExtract(String token) {
    return Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
  }
}
