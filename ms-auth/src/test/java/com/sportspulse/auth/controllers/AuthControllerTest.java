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
import com.sportspulse.auth.config.ApiPaths;
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
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests for the AuthController, covering both registration and login functionalities.
 */
public class AuthControllerTest extends AbstractIntegrationTest {

  @Autowired private UserRepository userRepository;
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  private UserEntity user;

  @BeforeEach
  void setUp() throws Exception {
    userRepository.deleteAll();
    user = userRepository.save(UserDataProvider.createMockUser());
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
}
