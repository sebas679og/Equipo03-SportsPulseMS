package com.sportspulse.auth.controllers;

import static com.jayway.jsonpath.JsonPath.read;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportspulse.auth.AbstractIntegrationTest;
import com.sportspulse.auth.UserDataProvider;
import com.sportspulse.auth.config.constants.ApiPaths;
import com.sportspulse.auth.config.properties.JwtProperties;
import com.sportspulse.auth.dto.requests.LoginRequest;
import com.sportspulse.auth.dto.requests.RegisterRequest;
import com.sportspulse.auth.models.UserEntity;
import com.sportspulse.auth.repositories.UserRepository;
import com.sportspulse.auth.utils.enums.UserRole;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests for the AuthController, covering both registration and login functionalities.
 */
public class AuthControllerTest extends AbstractIntegrationTest {

  @Autowired private UserRepository userRepository;
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private JwtProperties jwtProperties;
  @Autowired private PasswordEncoder passwordEncoder;

  private UserEntity user;

  @BeforeEach
  void setUp() throws Exception {
    userRepository.deleteAll();
    UserEntity rawUser =
        UserEntity.builder()
            .username(UserDataProvider.VALID_USERNAME)
            .email(UserDataProvider.VALID_EMAIL)
            .password(passwordEncoder.encode(UserDataProvider.VALID_PASSWORD))
            .role(UserRole.USER)
            .build();
    user = userRepository.save(rawUser);
  }

  @Test
  public void shouldReturn201WhenUserIsSuccessfullyCreated() throws Exception {
    userRepository.deleteAll();
    RegisterRequest user1 =
        RegisterRequest.builder()
            .username(user.getUsername())
            .email(user.getEmail())
            .password(user.getPassword())
            .build();
    mockMvc
        .perform(
            post(ApiPaths.Auth.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user1)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.username").value(user1.getUsername()))
        .andExpect(jsonPath("$.email").value(user1.getEmail()))
        .andExpect(jsonPath("$.role").value(UserRole.USER.name()))
        .andExpect(jsonPath("$.createdAt").exists())
        .andExpect(
            result -> {
              String json = result.getResponse().getContentAsString();
              String id = read(json, "$.id");
              UUID uuid = UUID.fromString(id);
              assertNotNull(uuid);
            })
        .andExpect(
            result -> {
              String json = result.getResponse().getContentAsString();
              String createdAt = read(json, "$.createdAt");
              java.time.Instant parsed = java.time.Instant.parse(createdAt);
              assertNotNull(parsed);
            });
  }

  @Test
  public void shouldReturn409WhenUsernameAlreadyExists() throws Exception {
    RegisterRequest user1 =
        RegisterRequest.builder()
            .username(user.getUsername())
            .email("email@email.com")
            .password(user.getPassword())
            .build();
    mockMvc
        .perform(
            post(ApiPaths.Auth.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user1)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value(HttpStatus.CONFLICT.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.CONFLICT.getReasonPhrase()))
        .andExpect(jsonPath("$.description").value(containsStringIgnoringCase("username")))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  public void shouldReturn409WhenEmailAlreadyExists() throws Exception {
    RegisterRequest user1 =
        RegisterRequest.builder()
            .username("other-user")
            .email(user.getEmail())
            .password(user.getPassword())
            .build();
    mockMvc
        .perform(
            post(ApiPaths.Auth.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user1)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value(HttpStatus.CONFLICT.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.CONFLICT.getReasonPhrase()))
        .andExpect(jsonPath("$.description").value(containsStringIgnoringCase("email")))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  public void shouldReturn400WhenAllFieldsAreNull() throws Exception {
    RegisterRequest user1 = RegisterRequest.builder().build();
    mockMvc
        .perform(
            post(ApiPaths.Auth.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user1)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
        .andExpect(jsonPath("$.description").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  public void shouldReturn400WhenAllFieldsAreBlank() throws Exception {
    RegisterRequest user1 = RegisterRequest.builder().username("").email("").password("").build();
    mockMvc
        .perform(
            post(ApiPaths.Auth.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user1)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
        .andExpect(jsonPath("$.description").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  public void shouldReturn400WhenUsernameIsBlank() throws Exception {
    RegisterRequest request =
        RegisterRequest.builder()
            .username("")
            .email(UserDataProvider.VALID_EMAIL)
            .password(UserDataProvider.VALID_PASSWORD)
            .build();

    mockMvc
        .perform(
            post(ApiPaths.Auth.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
        .andExpect(jsonPath("$.description").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  public void shouldReturn400WhenUsernameIsNull() throws Exception {
    RegisterRequest request =
        RegisterRequest.builder()
            .username(null)
            .email(UserDataProvider.VALID_EMAIL)
            .password(UserDataProvider.VALID_PASSWORD)
            .build();

    mockMvc
        .perform(
            post(ApiPaths.Auth.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
        .andExpect(jsonPath("$.description").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  public void shouldReturn400WhenUsernameContainsSpaces() throws Exception {
    RegisterRequest request =
        RegisterRequest.builder()
            .username("john doe")
            .email(UserDataProvider.VALID_EMAIL)
            .password(UserDataProvider.VALID_PASSWORD)
            .build();

    mockMvc
        .perform(
            post(ApiPaths.Auth.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
        .andExpect(jsonPath("$.description").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  public void shouldReturn400WhenUsernameHasLeadingSpace() throws Exception {
    RegisterRequest request =
        RegisterRequest.builder()
            .username(" johndoe")
            .email(UserDataProvider.VALID_EMAIL)
            .password(UserDataProvider.VALID_PASSWORD)
            .build();

    mockMvc
        .perform(
            post(ApiPaths.Auth.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
        .andExpect(jsonPath("$.description").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  public void shouldReturn400WhenUsernameHasTrailingSpace() throws Exception {
    RegisterRequest request =
        RegisterRequest.builder()
            .username("johndoe ")
            .email(UserDataProvider.VALID_EMAIL)
            .password(UserDataProvider.VALID_PASSWORD)
            .build();

    mockMvc
        .perform(
            post(ApiPaths.Auth.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
        .andExpect(jsonPath("$.description").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  public void shouldReturn400WhenUsernameContainsTab() throws Exception {
    RegisterRequest request =
        RegisterRequest.builder()
            .username("john\tdoe")
            .email(UserDataProvider.VALID_EMAIL)
            .password(UserDataProvider.VALID_PASSWORD)
            .build();

    mockMvc
        .perform(
            post(ApiPaths.Auth.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
        .andExpect(jsonPath("$.description").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  public void shouldReturn400WhenEmailHasNoAtSymbol() throws Exception {
    RegisterRequest request =
        RegisterRequest.builder()
            .username(UserDataProvider.VALID_USERNAME)
            .email("invalidemail.com")
            .password(UserDataProvider.VALID_PASSWORD)
            .build();

    mockMvc
        .perform(
            post(ApiPaths.Auth.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
        .andExpect(jsonPath("$.description").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  public void shouldReturn400WhenEmailHasNoDomain() throws Exception {
    RegisterRequest request =
        RegisterRequest.builder()
            .username(UserDataProvider.VALID_USERNAME)
            .email("user@")
            .password(UserDataProvider.VALID_PASSWORD)
            .build();

    mockMvc
        .perform(
            post(ApiPaths.Auth.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
        .andExpect(jsonPath("$.description").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  public void shouldReturn400WhenEmailHasNoLocalPart() throws Exception {
    RegisterRequest request =
        RegisterRequest.builder()
            .username(UserDataProvider.VALID_USERNAME)
            .email("@domain.com")
            .password(UserDataProvider.VALID_PASSWORD)
            .build();

    mockMvc
        .perform(
            post(ApiPaths.Auth.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
        .andExpect(jsonPath("$.description").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  public void shouldReturn400WhenEmailHasDoubleAt() throws Exception {
    RegisterRequest request =
        RegisterRequest.builder()
            .username(UserDataProvider.VALID_USERNAME)
            .email("user@@domain.com")
            .password(UserDataProvider.VALID_PASSWORD)
            .build();

    mockMvc
        .perform(
            post(ApiPaths.Auth.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
        .andExpect(jsonPath("$.description").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  public void shouldReturn400WhenEmailContainsSpaces() throws Exception {
    RegisterRequest request =
        RegisterRequest.builder()
            .username(UserDataProvider.VALID_USERNAME)
            .email("user name@domain.com")
            .password(UserDataProvider.VALID_PASSWORD)
            .build();

    mockMvc
        .perform(
            post(ApiPaths.Auth.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
        .andExpect(jsonPath("$.description").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  public void shouldReturn400WhenEmailIsPlainText() throws Exception {
    RegisterRequest request =
        RegisterRequest.builder()
            .username(UserDataProvider.VALID_USERNAME)
            .email("plaintext")
            .password(UserDataProvider.VALID_PASSWORD)
            .build();

    mockMvc
        .perform(
            post(ApiPaths.Auth.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
        .andExpect(jsonPath("$.description").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  public void shouldReturn400WhenPasswordHasNoUppercase() throws Exception {
    RegisterRequest request =
        RegisterRequest.builder()
            .username(UserDataProvider.VALID_USERNAME)
            .email(UserDataProvider.VALID_EMAIL)
            .password("nouppercase1!")
            .build();

    mockMvc
        .perform(
            post(ApiPaths.Auth.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
        .andExpect(jsonPath("$.description").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  public void shouldReturn400WhenPasswordHasNoLowercase() throws Exception {
    RegisterRequest request =
        RegisterRequest.builder()
            .username(UserDataProvider.VALID_USERNAME)
            .email(UserDataProvider.VALID_EMAIL)
            .password("NOLOWERCASE1!")
            .build();

    mockMvc
        .perform(
            post(ApiPaths.Auth.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
        .andExpect(jsonPath("$.description").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  public void shouldReturn400WhenPasswordHasNoDigit() throws Exception {
    RegisterRequest request =
        RegisterRequest.builder()
            .username(UserDataProvider.VALID_USERNAME)
            .email(UserDataProvider.VALID_EMAIL)
            .password("NoDigits!!")
            .build();

    mockMvc
        .perform(
            post(ApiPaths.Auth.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
        .andExpect(jsonPath("$.description").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  public void shouldReturn400WhenPasswordHasNoSpecialChar() throws Exception {
    RegisterRequest request =
        RegisterRequest.builder()
            .username(UserDataProvider.VALID_USERNAME)
            .email(UserDataProvider.VALID_EMAIL)
            .password("NoSpecial1")
            .build();

    mockMvc
        .perform(
            post(ApiPaths.Auth.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
        .andExpect(jsonPath("$.description").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  public void shouldReturn400WhenPasswordIsTooShort() throws Exception {
    RegisterRequest request =
        RegisterRequest.builder()
            .username(UserDataProvider.VALID_USERNAME)
            .email(UserDataProvider.VALID_EMAIL)
            .password("Sh@rt1")
            .build();

    mockMvc
        .perform(
            post(ApiPaths.Auth.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
        .andExpect(jsonPath("$.description").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  public void shouldReturn400WhenPasswordContainsWhitespace() throws Exception {
    RegisterRequest request =
        RegisterRequest.builder()
            .username(UserDataProvider.VALID_USERNAME)
            .email(UserDataProvider.VALID_EMAIL)
            .password("Has White1!")
            .build();

    mockMvc
        .perform(
            post(ApiPaths.Auth.REGISTER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
        .andExpect(jsonPath("$.description").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }

  // ===============================================================
  // User Login
  // ===============================================================

  @Test
  public void shouldReturn200WhenValidCredentialsAreProvided() throws Exception {
    LoginRequest request =
        LoginRequest.builder()
            .email(UserDataProvider.VALID_EMAIL)
            .password(UserDataProvider.VALID_PASSWORD)
            .build();

    mockMvc
        .perform(
            post(ApiPaths.Auth.LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").exists())
        .andExpect(jsonPath("$.tokenType").value(jwtProperties.getTokenType()))
        .andExpect(jsonPath("$.expiresIn").value(jwtProperties.getExpiration()))
        .andExpect(jsonPath("$.userId").exists())
        .andExpect(
            result -> {
              String json = result.getResponse().getContentAsString();
              String id = read(json, "$.userId");
              UUID uuid = UUID.fromString(id);
              assertNotNull(uuid);
            });
  }

  @Test
  public void shouldReturn401WhenEmailDoesNotExist() throws Exception {
    LoginRequest request =
        LoginRequest.builder()
            .email("test2@emil.com")
            .password(UserDataProvider.VALID_PASSWORD)
            .build();

    mockMvc
        .perform(
            post(ApiPaths.Auth.LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value(HttpStatus.UNAUTHORIZED.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.UNAUTHORIZED.getReasonPhrase()))
        .andExpect(jsonPath("$.description").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  public void shouldReturn401WhenPasswordIsInvalid() throws Exception {
    LoginRequest request =
        LoginRequest.builder()
            .email(UserDataProvider.VALID_EMAIL)
            .password("PasswordValid123!")
            .build();

    mockMvc
        .perform(
            post(ApiPaths.Auth.LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value(HttpStatus.UNAUTHORIZED.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.UNAUTHORIZED.getReasonPhrase()))
        .andExpect(jsonPath("$.description").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  public void shouldReturn400WhenAllFieldsAreBlankLogin() throws Exception {
    LoginRequest request = LoginRequest.builder().email("").password("").build();

    mockMvc
        .perform(
            post(ApiPaths.Auth.LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
        .andExpect(jsonPath("$.description").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  public void shouldReturn400WhenAllFieldsAreNullLogin() throws Exception {
    LoginRequest request = LoginRequest.builder().build();

    mockMvc
        .perform(
            post(ApiPaths.Auth.LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
        .andExpect(jsonPath("$.description").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }

  // =========================================================================
  // Email
  // =========================================================================
  @Test
  public void shouldReturn400WhenEmailIsBlankLogin() throws Exception {
    LoginRequest request =
        LoginRequest.builder().email("").password(UserDataProvider.VALID_PASSWORD).build();

    mockMvc
        .perform(
            post(ApiPaths.Auth.LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
        .andExpect(jsonPath("$.description").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  public void shouldReturn400WhenEmailIsNull() throws Exception {
    LoginRequest request =
        LoginRequest.builder().email(null).password(UserDataProvider.VALID_PASSWORD).build();

    mockMvc
        .perform(
            post(ApiPaths.Auth.LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
        .andExpect(jsonPath("$.description").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  public void shouldReturn400WhenEmailHasNoAtSymbolLogin() throws Exception {
    LoginRequest request =
        LoginRequest.builder()
            .email("invalidemail.com")
            .password(UserDataProvider.VALID_PASSWORD)
            .build();

    mockMvc
        .perform(
            post(ApiPaths.Auth.LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
        .andExpect(jsonPath("$.description").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  public void shouldReturn400WhenEmailHasNoDomainLogin() throws Exception {
    LoginRequest request =
        LoginRequest.builder().email("user@").password(UserDataProvider.VALID_PASSWORD).build();

    mockMvc
        .perform(
            post(ApiPaths.Auth.LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
        .andExpect(jsonPath("$.description").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  public void shouldReturn400WhenEmailHasNoLocalPartLogin() throws Exception {
    LoginRequest request =
        LoginRequest.builder()
            .email("@domain.com")
            .password(UserDataProvider.VALID_PASSWORD)
            .build();

    mockMvc
        .perform(
            post(ApiPaths.Auth.LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
        .andExpect(jsonPath("$.description").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  public void shouldReturn400WhenEmailHasDoubleAtLogin() throws Exception {
    LoginRequest request =
        LoginRequest.builder()
            .email("user@@domain.com")
            .password(UserDataProvider.VALID_PASSWORD)
            .build();

    mockMvc
        .perform(
            post(ApiPaths.Auth.LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
        .andExpect(jsonPath("$.description").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  public void shouldReturn400WhenEmailContainsSpacesLogin() throws Exception {
    LoginRequest request =
        LoginRequest.builder()
            .email("user name@domain.com")
            .password(UserDataProvider.VALID_PASSWORD)
            .build();

    mockMvc
        .perform(
            post(ApiPaths.Auth.LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
        .andExpect(jsonPath("$.description").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  public void shouldReturn400WhenEmailIsPlainTextLogin() throws Exception {
    LoginRequest request =
        LoginRequest.builder().email("plaintext").password(UserDataProvider.VALID_PASSWORD).build();

    mockMvc
        .perform(
            post(ApiPaths.Auth.LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
        .andExpect(jsonPath("$.description").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }

  // =========================================================================
  // Password
  // =========================================================================
  @Test
  public void shouldReturn400WhenPasswordIsBlankLogin() throws Exception {
    LoginRequest request =
        LoginRequest.builder().email(UserDataProvider.VALID_EMAIL).password("").build();

    mockMvc
        .perform(
            post(ApiPaths.Auth.LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
        .andExpect(jsonPath("$.description").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  public void shouldReturn400WhenPasswordIsNullLogin() throws Exception {
    LoginRequest request =
        LoginRequest.builder().email(UserDataProvider.VALID_EMAIL).password(null).build();

    mockMvc
        .perform(
            post(ApiPaths.Auth.LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
        .andExpect(jsonPath("$.description").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  public void shouldReturn400WhenPasswordContainsOnlySpacesLogin() throws Exception {
    LoginRequest request =
        LoginRequest.builder().email(UserDataProvider.VALID_EMAIL).password("   ").build();

    mockMvc
        .perform(
            post(ApiPaths.Auth.LOGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
        .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
        .andExpect(jsonPath("$.description").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }
}
