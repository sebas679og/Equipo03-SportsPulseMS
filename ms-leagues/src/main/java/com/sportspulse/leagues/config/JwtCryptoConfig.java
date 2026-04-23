package com.sportspulse.leagues.config;

import com.sportspulse.leagues.config.properties.JwtProperties;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Provides cryptographic beans used for JWT verification. */
@Configuration
@RequiredArgsConstructor
public class JwtCryptoConfig {

  private final JwtProperties jwtProperties;

  @Bean
  public SecretKey signingKey() {
    byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
    return Keys.hmacShaKeyFor(keyBytes);
  }
}
