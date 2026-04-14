package com.sportspulse.auth.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sportspulse.auth.AbstractIntegrationTest;
import com.sportspulse.auth.UserDataProvider;
import com.sportspulse.auth.config.constants.ApiPaths;
import com.sportspulse.auth.config.constants.InternalHeaders;
import com.sportspulse.auth.config.properties.JwtProperties;
import com.sportspulse.auth.config.properties.SecurityProperties;
import com.sportspulse.auth.models.UserEntity;
import com.sportspulse.auth.repositories.UserRepository;
import com.sportspulse.auth.services.components.JwtService;
import com.sportspulse.auth.utils.enums.UserRole;
import io.jsonwebtoken.Jwts;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

/** Integration tests for the JWT token validation endpoint. */
public class ValidateControllerTest extends AbstractIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private UserRepository userRepository;
  @Autowired private JwtService jwtService;
  @Autowired private JwtProperties jwtProperties;
  @Autowired private SecurityProperties securityProperties;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private SecretKey signingKey;

  private UserEntity user;
  private String validToken;

  @BeforeEach
  void setUp() throws Exception {
    userRepository.deleteAll();
    user =
        userRepository.save(
            UserEntity.builder()
                .username(UserDataProvider.VALID_USERNAME)
                .email(UserDataProvider.VALID_EMAIL)
                .password(passwordEncoder.encode(UserDataProvider.VALID_PASSWORD))
                .role(UserRole.USER)
                .build());
    validToken = jwtService.generateToken(user.getId(), user.getUsername(), user.getRole().name());
  }

  @Test
  public void shouldReturn200WhenTokenIsValid() throws Exception {
    mockMvc
        .perform(
            get(ApiPaths.Validate.TOKEN)
                .header(
                    InternalHeaders.AUTHORIZATION, jwtProperties.getTokenType() + " " + validToken)
                .header(InternalHeaders.INTERNAL_API_KEY, securityProperties.getInternalApiKey()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.valid").value(true))
        .andExpect(jsonPath("$.userId").value(user.getId().toString()))
        .andExpect(jsonPath("$.username").value(user.getUsername()))
        .andExpect(jsonPath("$.role").value(user.getRole().name()));
  }

  @Test
  public void shouldReturn401WhenTokenIsInvalid() throws Exception {
    mockMvc
        .perform(
            get(ApiPaths.Validate.TOKEN)
                .header(
                    InternalHeaders.AUTHORIZATION,
                    jwtProperties.getTokenType() + " invalid.token.string")
                .header(InternalHeaders.INTERNAL_API_KEY, securityProperties.getInternalApiKey()))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value(HttpStatus.UNAUTHORIZED.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.UNAUTHORIZED.getReasonPhrase()))
        .andExpect(jsonPath("$.description").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  public void shouldReturn401WhenTokenIsMissing() throws Exception {
    mockMvc
        .perform(
            get(ApiPaths.Validate.TOKEN)
                .header(InternalHeaders.INTERNAL_API_KEY, securityProperties.getInternalApiKey()))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value(HttpStatus.UNAUTHORIZED.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.UNAUTHORIZED.getReasonPhrase()))
        .andExpect(jsonPath("$.description").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  public void shouldReturn403WhenInternalServiceAuthorizationIsInvalid() throws Exception {
    mockMvc
        .perform(
            get(ApiPaths.Validate.TOKEN)
                .header(
                    InternalHeaders.AUTHORIZATION, jwtProperties.getTokenType() + " " + validToken)
                .header(InternalHeaders.INTERNAL_API_KEY, "invalid-api-key"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value(HttpStatus.FORBIDDEN.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.FORBIDDEN.getReasonPhrase()))
        .andExpect(jsonPath("$.description").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  public void shouldReturn401WhenInternalServiceAuthorizationIsMissing() throws Exception {
    mockMvc
        .perform(
            get(ApiPaths.Validate.TOKEN)
                .header(
                    InternalHeaders.AUTHORIZATION, jwtProperties.getTokenType() + " " + validToken))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value(HttpStatus.UNAUTHORIZED.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.UNAUTHORIZED.getReasonPhrase()))
        .andExpect(jsonPath("$.description").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  public void shouldReturn401WhenTokenIsExpired() throws Exception {
    Instant past = Instant.now().minusSeconds(3600).truncatedTo(ChronoUnit.MILLIS);
    String expiredToken =
        Jwts.builder()
            .subject(user.getId().toString())
            .claim("username", user.getUsername())
            .claim("role", user.getRole().name())
            .issuedAt(Date.from(past))
            .expiration(Date.from(past.plusSeconds(1)))
            .signWith(signingKey)
            .compact();

    mockMvc
        .perform(
            get(ApiPaths.Validate.TOKEN)
                .header(
                    InternalHeaders.AUTHORIZATION,
                    jwtProperties.getTokenType() + " " + expiredToken)
                .header(InternalHeaders.INTERNAL_API_KEY, securityProperties.getInternalApiKey()))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value(HttpStatus.UNAUTHORIZED.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.UNAUTHORIZED.getReasonPhrase()))
        .andExpect(jsonPath("$.description").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }
}
