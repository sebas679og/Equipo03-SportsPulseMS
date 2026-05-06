package com.sportspulse.leagues.controller;

import com.sportspulse.leagues.AbstractIntegrationTest;
import com.sportspulse.leagues.integrations.msauth.AuthClient;
import com.sportspulse.leagues.integrations.msauth.dto.UserResponse;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

class LeaguesControllerTest extends AbstractIntegrationTest {

  private static final String VALID_TOKEN = "valid-test-token";

  private static final UserResponse VALID_USER =
      new UserResponse(
          true, UUID.fromString("550e8400-e29b-41d4-a716-446655440000"), "javier_ruiz", "USER");

  @MockitoBean private AuthClient authClient;

  @Autowired private WebTestClient webTestClient;

  @BeforeEach
  void setUp() {
    Mockito.when(authClient.isTokenValid(VALID_TOKEN)).thenReturn(VALID_USER);
  }

  @AfterEach
  void tearDown() {
    Mockito.reset(authClient);
  }

  @Test
  void shouldReturnLeaguesByCountry_whenValidTokenAndCountryProvided() {
    leaguesStub.stubByCountry("spain"); // ← accede directo al campo del padre

    webTestClient
        .get()
        .uri("/api/leagues?country=spain")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + VALID_TOKEN)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.data")
        .isArray()
        .jsonPath("$.data[0].name")
        .isEqualTo("La Liga");
  }
}
