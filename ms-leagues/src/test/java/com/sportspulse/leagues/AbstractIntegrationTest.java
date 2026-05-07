package com.sportspulse.leagues;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * AbstractIntegrationTest
 *
 * <p>Base class for integration tests within the application.
 *
 * <p>Configures a Spring Boot test environment with a random port, activates the "test" profile,
 * and auto-configures a WebTestClient for HTTP-based testing.
 *
 * <p>Provides a {@link WireMockExtension} to simulate external API interactions, overriding
 * properties to redirect API calls to the WireMock server. Serves as a foundation for concrete
 * integration test classes.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
public class AbstractIntegrationTest {

  @RegisterExtension
  static WireMockExtension wireMockExtension =
      WireMockExtension.newInstance()
          .options(wireMockConfig().dynamicPort().usingFilesUnderClasspath("src/test/resources"))
          .build();

  protected ApiFootballLeaguesStub leaguesStub;

  @DynamicPropertySource
  static void overrideProperties(DynamicPropertyRegistry registry) {
    registry.add("sportspulse.leagues.api.football.base-url", wireMockExtension::baseUrl);
  }

  @BeforeEach
  void setUpBase() {
    leaguesStub = new ApiFootballLeaguesStub(wireMockExtension);
  }

  @AfterEach
  void tearDown() {
    leaguesStub.reset();
  }
}
