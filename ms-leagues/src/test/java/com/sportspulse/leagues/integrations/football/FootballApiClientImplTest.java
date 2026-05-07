package com.sportspulse.leagues.integrations.football;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportspulse.leagues.exceptions.CustomServiceUnavailableException;
import com.sportspulse.leagues.integrations.football.dto.ApiCountry;
import com.sportspulse.leagues.integrations.football.dto.ApiCoverage;
import com.sportspulse.leagues.integrations.football.dto.ApiFixtures;
import com.sportspulse.leagues.integrations.football.dto.ApiLeague;
import com.sportspulse.leagues.integrations.football.dto.ApiLeagueResponse;
import com.sportspulse.leagues.integrations.football.dto.ApiPaging;
import com.sportspulse.leagues.integrations.football.dto.ApiResponse;
import com.sportspulse.leagues.integrations.football.dto.ApiSeason;
import java.util.List;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@DisplayName("FootballApiClientImpl Unit Tests")
class FootballApiClientImplTest {

  // One server instance per test method — prevents cached responses
  // from a previous test being served to the next one.
  private MockWebServer mockWebServer;
  private FootballApiClientImpl footballApiClient;
  private final ObjectMapper objectMapper = new ObjectMapper();

  // ─────────────────────────────────────────────
  // Lifecycle — fresh server + fresh client per test
  // ─────────────────────────────────────────────

  @BeforeEach
  void setUp() throws Exception {
    mockWebServer = new MockWebServer();
    mockWebServer.start();

    // Build a plain WebClient — no Spring context, so @Cacheable is NOT active.
    // This is intentional: we want every call to actually hit the mock server
    // so we can assert the exact request that was sent.
    WebClient webClient =
        WebClient.builder()
            .baseUrl(mockWebServer.url("/").toString())
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();

    footballApiClient = new FootballApiClientImpl(webClient);
  }

  @AfterEach
  void tearDown() throws Exception {
    mockWebServer.shutdown();
  }

  // ─────────────────────────────────────────────
  // Fixtures
  // ─────────────────────────────────────────────

  private ApiLeagueResponse buildLeagueResponse() {
    return new ApiLeagueResponse(
        "leagues",
        List.of(),
        List.of(),
        1,
        new ApiPaging(1, 1),
        List.of(
            new ApiResponse(
                new ApiLeague(39, "Premier League", "League", "https://logo.url"),
                new ApiCountry("England", "GB", "https://flag.url"),
                List.of(
                    new ApiSeason(
                        2023,
                        "2023-08-11",
                        "2024-05-19",
                        true,
                        new ApiCoverage(
                            new ApiFixtures(true, true, true, true),
                            true,
                            true,
                            true,
                            true,
                            true,
                            true,
                            true,
                            true))))));
  }

  private MockResponse jsonResponse(Object body) throws Exception {
    return new MockResponse()
        .setResponseCode(200)
        .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .setBody(objectMapper.writeValueAsString(body));
  }

  private MockResponse emptyResponse(int statusCode) {
    return new MockResponse().setResponseCode(statusCode);
  }

  // ─────────────────────────────────────────────
  // getLeaguesCountryAndSeason
  // ─────────────────────────────────────────────

  @Nested
  @DisplayName("getLeaguesCountryAndSeason()")
  class GetLeaguesCountryAndSeason {

    @Test
    @DisplayName("Returns response when country and season are both provided")
    void shouldReturnResponse_whenCountryAndSeasonProvided() throws Exception {
      ApiLeagueResponse expected = buildLeagueResponse();
      mockWebServer.enqueue(jsonResponse(expected));

      ApiLeagueResponse result = footballApiClient.getLeaguesCountryAndSeason("England", 2023);

      assertThat(result).isNotNull();
      assertThat(result.results()).isEqualTo(1);
      assertThat(result.response()).hasSize(1);
      assertThat(result.response().getFirst().league().name()).isEqualTo("Premier League");
    }

    @Test
    @DisplayName("Sends correct query params when country and season are provided")
    void shouldSendCorrectQueryParams_whenCountryAndSeasonProvided() throws Exception {
      mockWebServer.enqueue(jsonResponse(buildLeagueResponse()));

      footballApiClient.getLeaguesCountryAndSeason("England", 2023);

      RecordedRequest request = mockWebServer.takeRequest();
      assertThat(request.getPath()).contains("country=England").contains("season=2023");
    }

    @Test
    @DisplayName("Omits country param when country is null")
    void shouldOmitCountryParam_whenCountryIsNull() throws Exception {
      mockWebServer.enqueue(jsonResponse(buildLeagueResponse()));

      footballApiClient.getLeaguesCountryAndSeason(null, 2023);

      RecordedRequest request = mockWebServer.takeRequest();
      assertThat(request.getPath()).doesNotContain("country=").contains("season=2023");
    }

    @Test
    @DisplayName("Omits season param when season is null")
    void shouldOmitSeasonParam_whenSeasonIsNull() throws Exception {
      mockWebServer.enqueue(jsonResponse(buildLeagueResponse()));

      footballApiClient.getLeaguesCountryAndSeason("Spain", null);

      RecordedRequest request = mockWebServer.takeRequest();
      assertThat(request.getPath()).contains("country=Spain").doesNotContain("season=");
    }

    @Test
    @DisplayName("Sends request with no query params when both country and season are null")
    void shouldSendNoQueryParams_whenBothParamsAreNull() throws Exception {
      mockWebServer.enqueue(jsonResponse(buildLeagueResponse()));

      footballApiClient.getLeaguesCountryAndSeason(null, null);

      RecordedRequest request = mockWebServer.takeRequest();
      assertThat(request.getPath()).doesNotContain("country=").doesNotContain("season=");
    }

    @Test
    @DisplayName("Throws CustomServiceUnavailableException on HTTP 204")
    void shouldThrowServiceUnavailable_on204() {
      mockWebServer.enqueue(emptyResponse(204));

      assertThatThrownBy(() -> footballApiClient.getLeaguesCountryAndSeason("England", 2023))
          .isInstanceOf(CustomServiceUnavailableException.class)
          .hasMessageContaining("Api-Football is not currently available");
    }

    @Test
    @DisplayName("Throws CustomServiceUnavailableException on HTTP 499")
    void shouldThrowServiceUnavailable_on499() {
      mockWebServer.enqueue(emptyResponse(499));

      assertThatThrownBy(() -> footballApiClient.getLeaguesCountryAndSeason("England", 2023))
          .isInstanceOf(CustomServiceUnavailableException.class)
          .hasMessageContaining("Api-Football is not currently available");
    }

    @Test
    @DisplayName("Throws CustomServiceUnavailableException on HTTP 500")
    void shouldThrowServiceUnavailable_on500() {
      mockWebServer.enqueue(emptyResponse(500));

      assertThatThrownBy(() -> footballApiClient.getLeaguesCountryAndSeason("England", 2023))
          .isInstanceOf(CustomServiceUnavailableException.class)
          .hasMessageContaining("Api-Football is not currently available");
    }

    @Test
    @DisplayName("Throws CustomServiceUnavailableException when body is null")
    void shouldThrowServiceUnavailable_whenBodyIsNull() {
      mockWebServer.enqueue(
          new MockResponse()
              .setResponseCode(200)
              .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
              .setBody("null"));

      assertThatThrownBy(() -> footballApiClient.getLeaguesCountryAndSeason("England", 2023))
          .isInstanceOf(CustomServiceUnavailableException.class)
          .hasMessageContaining("Api-Football is not currently available");
    }
  }

  // ─────────────────────────────────────────────
  // getLeagueById
  // ─────────────────────────────────────────────

  @Nested
  @DisplayName("getLeagueById()")
  class GetLeagueById {

    @Test
    @DisplayName("Returns response for a valid league ID")
    void shouldReturnResponse_whenLeagueIdIsValid() throws Exception {
      ApiLeagueResponse expected = buildLeagueResponse();
      mockWebServer.enqueue(jsonResponse(expected));

      ApiLeagueResponse result = footballApiClient.getLeagueById(39);

      assertThat(result).isNotNull();
      assertThat(result.results()).isEqualTo(1);
      assertThat(result.response().getFirst().league().id()).isEqualTo(39);
    }

    @Test
    @DisplayName("Sends correct id query param")
    void shouldSendCorrectIdQueryParam() throws Exception {
      mockWebServer.enqueue(jsonResponse(buildLeagueResponse()));

      footballApiClient.getLeagueById(39);

      RecordedRequest request = mockWebServer.takeRequest();
      assertThat(request.getPath()).contains("/leagues").contains("id=39");
    }

    @Test
    @DisplayName("Throws CustomServiceUnavailableException on HTTP 204")
    void shouldThrowServiceUnavailable_on204() {
      mockWebServer.enqueue(emptyResponse(204));

      assertThatThrownBy(() -> footballApiClient.getLeagueById(39))
          .isInstanceOf(CustomServiceUnavailableException.class)
          .hasMessageContaining("Api-Football is not currently available");
    }

    @Test
    @DisplayName("Throws CustomServiceUnavailableException on HTTP 499")
    void shouldThrowServiceUnavailable_on499() {
      mockWebServer.enqueue(emptyResponse(499));

      assertThatThrownBy(() -> footballApiClient.getLeagueById(39))
          .isInstanceOf(CustomServiceUnavailableException.class)
          .hasMessageContaining("Api-Football is not currently available");
    }

    @Test
    @DisplayName("Throws CustomServiceUnavailableException on HTTP 500")
    void shouldThrowServiceUnavailable_on500() {
      mockWebServer.enqueue(emptyResponse(500));

      assertThatThrownBy(() -> footballApiClient.getLeagueById(39))
          .isInstanceOf(CustomServiceUnavailableException.class)
          .hasMessageContaining("Api-Football is not currently available");
    }

    @Test
    @DisplayName("Throws CustomServiceUnavailableException when body is null")
    void shouldThrowServiceUnavailable_whenBodyIsNull() {
      mockWebServer.enqueue(
          new MockResponse()
              .setResponseCode(200)
              .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
              .setBody("null"));

      assertThatThrownBy(() -> footballApiClient.getLeagueById(39))
          .isInstanceOf(CustomServiceUnavailableException.class)
          .hasMessageContaining("Api-Football is not currently available");
    }
  }
}
