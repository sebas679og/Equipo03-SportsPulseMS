package com.sportspulse.leagues.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.sportspulse.leagues.dto.requests.CountryAndSeasonRequest;
import com.sportspulse.leagues.dto.responses.LeagueCurrentSeason;
import com.sportspulse.leagues.dto.responses.LeagueDetailResponse;
import com.sportspulse.leagues.dto.responses.LeagueSummary;
import com.sportspulse.leagues.dto.responses.LeaguesResponse;
import com.sportspulse.leagues.exceptions.CustomBadGatewayException;
import com.sportspulse.leagues.exceptions.CustomBadRequestException;
import com.sportspulse.leagues.exceptions.CustomNotFoundException;
import com.sportspulse.leagues.exceptions.CustomTooManyRequestsException;
import com.sportspulse.leagues.integrations.football.FootballApiClient;
import com.sportspulse.leagues.integrations.football.dto.ApiCountry;
import com.sportspulse.leagues.integrations.football.dto.ApiCoverage;
import com.sportspulse.leagues.integrations.football.dto.ApiFixtures;
import com.sportspulse.leagues.integrations.football.dto.ApiLeague;
import com.sportspulse.leagues.integrations.football.dto.ApiLeagueResponse;
import com.sportspulse.leagues.integrations.football.dto.ApiPaging;
import com.sportspulse.leagues.integrations.football.dto.ApiResponse;
import com.sportspulse.leagues.integrations.football.dto.ApiSeason;
import com.sportspulse.leagues.utils.mappers.LeaguesMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("LeaguesServiceImpl Unit Tests")
class LeaguesServiceImplTest {

  @Mock private FootballApiClient footballApiClient;
  @Mock private LeaguesMapper leaguesMapper;

  @InjectMocks private LeaguesServiceImpl leaguesService;

  // ─────────────────────────────────────────────
  // Fixtures
  // ─────────────────────────────────────────────

  private CountryAndSeasonRequest requestWith(String country, String season) {
    return CountryAndSeasonRequest.builder().country(country).season(season).build();
  }

  private ApiLeagueResponse apiResponseWithNoErrors(List<ApiResponse> responses) {
    return new ApiLeagueResponse(
        "leagues", List.of(), List.of(), responses.size(), new ApiPaging(1, 1), responses);
  }

  private ApiLeagueResponse apiResponseWithErrors(List<Object> errors) {
    return new ApiLeagueResponse("leagues", List.of(), errors, 0, new ApiPaging(1, 1), List.of());
  }

  private ApiResponse buildApiResponse(int leagueId, String leagueName) {
    return new ApiResponse(
        new ApiLeague(leagueId, leagueName, "League", "https://logo.url"),
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
                    true))));
  }

  private LeagueSummary buildLeagueSummary(int id, String name) {
    return LeagueSummary.builder()
        .id(id)
        .name(name)
        .type("League")
        .country("England")
        .logo("https://logo.url")
        .currentSeason(2023)
        .startDate("2023-08-11")
        .endDate("2024-05-19")
        .build();
  }

  private LeagueDetailResponse buildLeagueDetailResponse(int id, String name) {
    return LeagueDetailResponse.builder()
        .id(id)
        .name(name)
        .type("League")
        .country("England")
        .logo("https://logo.url")
        .seasons(List.of(2023))
        .currentSeason(
            LeagueCurrentSeason.builder()
                .year(2023)
                .startDate("2023-08-11")
                .endDate("2024-05-19")
                .current(true)
                .build())
        .build();
  }

  // ─────────────────────────────────────────────
  // getLeagues()
  // ─────────────────────────────────────────────

  @Nested
  @DisplayName("getLeagues()")
  class GetLeagues {

    @Nested
    @DisplayName("Happy path")
    class HappyPath {

      @Test
      @DisplayName("Returns LeaguesResponse when country is provided")
      void shouldReturnLeagues_whenCountryIsProvided() {
        ApiResponse apiResponse = buildApiResponse(39, "Premier League");
        LeagueSummary summary = buildLeagueSummary(39, "Premier League");

        when(footballApiClient.getLeaguesCountryAndSeason("England", null))
            .thenReturn(apiResponseWithNoErrors(List.of(apiResponse)));
        when(leaguesMapper.toSummaryList(List.of(apiResponse))).thenReturn(List.of(summary));

        LeaguesResponse result = leaguesService.getLeagues(requestWith("England", null));

        assertThat(result).isNotNull();
        assertThat(result.getData()).hasSize(1);
        assertThat(result.getData().getFirst().getName()).isEqualTo("Premier League");
      }

      @Test
      @DisplayName("Returns LeaguesResponse when season is provided")
      void shouldReturnLeagues_whenSeasonIsProvided() {
        ApiResponse apiResponse = buildApiResponse(39, "Premier League");
        LeagueSummary summary = buildLeagueSummary(39, "Premier League");

        when(footballApiClient.getLeaguesCountryAndSeason(null, 2023))
            .thenReturn(apiResponseWithNoErrors(List.of(apiResponse)));
        when(leaguesMapper.toSummaryList(List.of(apiResponse))).thenReturn(List.of(summary));

        LeaguesResponse result = leaguesService.getLeagues(requestWith(null, "2023"));

        assertThat(result).isNotNull();
        assertThat(result.getData()).hasSize(1);
      }

      @Test
      @DisplayName("Returns LeaguesResponse when both country and season are provided")
      void shouldReturnLeagues_whenBothFiltersAreProvided() {
        ApiResponse apiResponse = buildApiResponse(39, "Premier League");
        LeagueSummary summary = buildLeagueSummary(39, "Premier League");

        when(footballApiClient.getLeaguesCountryAndSeason("England", 2023))
            .thenReturn(apiResponseWithNoErrors(List.of(apiResponse)));
        when(leaguesMapper.toSummaryList(List.of(apiResponse))).thenReturn(List.of(summary));

        LeaguesResponse result = leaguesService.getLeagues(requestWith("England", "2023"));

        assertThat(result.getData()).hasSize(1);
        assertThat(result.getData().getFirst().getId()).isEqualTo(39);
      }

      @Test
      @DisplayName("Passes season as Integer when season string is provided")
      void shouldParseSeasonStringToInteger_whenSeasonIsProvided() {
        ApiResponse apiResponse = buildApiResponse(39, "Premier League");

        when(footballApiClient.getLeaguesCountryAndSeason("England", 2023))
            .thenReturn(apiResponseWithNoErrors(List.of(apiResponse)));
        when(leaguesMapper.toSummaryList(any()))
            .thenReturn(List.of(buildLeagueSummary(39, "Premier League")));

        leaguesService.getLeagues(requestWith("England", "2023"));

        verify(footballApiClient).getLeaguesCountryAndSeason("England", 2023);
      }

      @Test
      @DisplayName("Passes null season to client when season is null")
      void shouldPassNullSeason_whenSeasonIsNull() {
        ApiResponse apiResponse = buildApiResponse(39, "Premier League");

        when(footballApiClient.getLeaguesCountryAndSeason("England", null))
            .thenReturn(apiResponseWithNoErrors(List.of(apiResponse)));
        when(leaguesMapper.toSummaryList(any()))
            .thenReturn(List.of(buildLeagueSummary(39, "Premier League")));

        leaguesService.getLeagues(requestWith("England", null));

        verify(footballApiClient).getLeaguesCountryAndSeason("England", null);
      }

      @Test
      @DisplayName("Returns multiple leagues when API returns multiple results")
      void shouldReturnMultipleLeagues_whenApiReturnsMultipleResults() {
        ApiResponse apiResponse1 = buildApiResponse(39, "Premier League");
        ApiResponse apiResponse2 = buildApiResponse(140, "La Liga");

        when(footballApiClient.getLeaguesCountryAndSeason(null, 2023))
            .thenReturn(apiResponseWithNoErrors(List.of(apiResponse1, apiResponse2)));
        when(leaguesMapper.toSummaryList(List.of(apiResponse1, apiResponse2)))
            .thenReturn(
                List.of(
                    buildLeagueSummary(39, "Premier League"), buildLeagueSummary(140, "La Liga")));

        LeaguesResponse result = leaguesService.getLeagues(requestWith(null, "2023"));

        assertThat(result.getData()).hasSize(2);
      }
    }

    @Nested
    @DisplayName("Validation errors")
    class ValidationErrors {

      @Test
      @DisplayName("Throws CustomBadRequestException when both country and season are null")
      void shouldThrowBadRequest_whenBothFiltersAreNull() {
        assertThatThrownBy(() -> leaguesService.getLeagues(requestWith(null, null)))
            .isInstanceOf(CustomBadRequestException.class)
            .hasMessageContaining("At least one filter is required");

        verifyNoInteractions(footballApiClient, leaguesMapper);
      }
    }

    @Nested
    @DisplayName("Not found")
    class NotFound {

      @Test
      @DisplayName("Throws CustomNotFoundException when API returns empty response list")
      void shouldThrowNotFound_whenApiResponseIsEmpty() {
        when(footballApiClient.getLeaguesCountryAndSeason("England", null))
            .thenReturn(apiResponseWithNoErrors(List.of()));

        assertThatThrownBy(() -> leaguesService.getLeagues(requestWith("England", null)))
            .isInstanceOf(CustomNotFoundException.class)
            .hasMessageContaining("No leagues found")
            .hasMessageContaining("England");

        verifyNoInteractions(leaguesMapper);
      }

      @Test
      @DisplayName("Throws CustomNotFoundException when API returns null response list")
      void shouldThrowNotFound_whenApiResponseIsNull() {
        ApiLeagueResponse nullResponse =
            new ApiLeagueResponse("leagues", List.of(), List.of(), 0, new ApiPaging(1, 1), null);

        when(footballApiClient.getLeaguesCountryAndSeason("England", null))
            .thenReturn(nullResponse);

        assertThatThrownBy(() -> leaguesService.getLeagues(requestWith("England", null)))
            .isInstanceOf(CustomNotFoundException.class)
            .hasMessageContaining("No leagues found");
      }
    }

    @Nested
    @DisplayName("API error handling")
    class ApiErrorHandling {

      @Test
      @DisplayName("Throws CustomTooManyRequestsException when errors contain 'requests' key")
      void shouldThrowTooManyRequests_whenErrorsContainRequestsKey() {
        Map<String, Object> error = Map.of("requests", "Requests limit reached");
        when(footballApiClient.getLeaguesCountryAndSeason("England", null))
            .thenReturn(apiResponseWithErrors(List.of(error)));

        assertThatThrownBy(() -> leaguesService.getLeagues(requestWith("England", null)))
            .isInstanceOf(CustomTooManyRequestsException.class)
            .hasMessageContaining("daily request limit");
      }

      @Test
      @DisplayName(
          "Throws CustomTooManyRequestsException with plan message when errors contain 'plan' key")
      void shouldThrowTooManyRequests_whenErrorsContainPlanKey() {
        String planMessage = "You have exceeded the limit of 100 requests per season";
        Map<String, Object> error = Map.of("plan", planMessage);
        when(footballApiClient.getLeaguesCountryAndSeason("England", null))
            .thenReturn(apiResponseWithErrors(List.of(error)));

        assertThatThrownBy(() -> leaguesService.getLeagues(requestWith("England", null)))
            .isInstanceOf(CustomTooManyRequestsException.class)
            .hasMessage(planMessage);
      }

      @Test
      @DisplayName("Throws CustomBadGatewayException when errors contain an unknown key")
      void shouldThrowBadGateway_whenErrorsContainUnknownKey() {
        Map<String, Object> error = Map.of("unknown", "some unexpected error");
        when(footballApiClient.getLeaguesCountryAndSeason("England", null))
            .thenReturn(apiResponseWithErrors(List.of(error)));

        assertThatThrownBy(() -> leaguesService.getLeagues(requestWith("England", null)))
            .isInstanceOf(CustomBadGatewayException.class)
            .hasMessageContaining("error occurred while processing");
      }

      @Test
      @DisplayName("Does not throw when errors list is empty")
      void shouldNotThrow_whenErrorsListIsEmpty() {
        ApiResponse apiResponse = buildApiResponse(39, "Premier League");

        when(footballApiClient.getLeaguesCountryAndSeason("England", null))
            .thenReturn(apiResponseWithNoErrors(List.of(apiResponse)));
        when(leaguesMapper.toSummaryList(any()))
            .thenReturn(List.of(buildLeagueSummary(39, "Premier League")));

        LeaguesResponse result = leaguesService.getLeagues(requestWith("England", null));

        assertThat(result).isNotNull();
      }
    }
  }

  // ─────────────────────────────────────────────
  // getLeagueById()
  // ─────────────────────────────────────────────

  @Nested
  @DisplayName("getLeagueById()")
  class GetLeagueById {

    @Nested
    @DisplayName("Happy path")
    class HappyPath {

      @Test
      @DisplayName("Returns LeagueDetailResponse for a valid league ID")
      void shouldReturnLeagueDetail_whenLeagueExists() {
        ApiResponse apiResponse = buildApiResponse(39, "Premier League");
        LeagueDetailResponse detail = buildLeagueDetailResponse(39, "Premier League");

        when(footballApiClient.getLeagueById(39))
            .thenReturn(apiResponseWithNoErrors(List.of(apiResponse)));
        when(leaguesMapper.toDetail(apiResponse)).thenReturn(detail);

        LeagueDetailResponse result = leaguesService.getLeagueById(39);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(39);
        assertThat(result.getName()).isEqualTo("Premier League");
        assertThat(result.getSeasons()).containsExactly(2023);
        assertThat(result.getCurrentSeason().getYear()).isEqualTo(2023);
      }

      @Test
      @DisplayName("Delegates mapping of first API response element to the mapper")
      void shouldPassFirstResponseElementToMapper() {
        ApiResponse apiResponse = buildApiResponse(39, "Premier League");
        LeagueDetailResponse detail = buildLeagueDetailResponse(39, "Premier League");

        when(footballApiClient.getLeagueById(39))
            .thenReturn(apiResponseWithNoErrors(List.of(apiResponse)));
        when(leaguesMapper.toDetail(apiResponse)).thenReturn(detail);

        leaguesService.getLeagueById(39);

        verify(leaguesMapper).toDetail(apiResponse);
      }
    }

    @Nested
    @DisplayName("Not found")
    class NotFound {

      @Test
      @DisplayName("Throws CustomNotFoundException when API returns empty response list")
      void shouldThrowNotFound_whenApiResponseIsEmpty() {
        when(footballApiClient.getLeagueById(999)).thenReturn(apiResponseWithNoErrors(List.of()));

        assertThatThrownBy(() -> leaguesService.getLeagueById(999))
            .isInstanceOf(CustomNotFoundException.class)
            .hasMessageContaining("No league found with id 999");

        verifyNoInteractions(leaguesMapper);
      }

      @Test
      @DisplayName("Throws CustomNotFoundException when API returns null response list")
      void shouldThrowNotFound_whenApiResponseIsNull() {
        ApiLeagueResponse nullResponse =
            new ApiLeagueResponse("leagues", List.of(), List.of(), 0, new ApiPaging(1, 1), null);

        when(footballApiClient.getLeagueById(999)).thenReturn(nullResponse);

        assertThatThrownBy(() -> leaguesService.getLeagueById(999))
            .isInstanceOf(CustomNotFoundException.class)
            .hasMessageContaining("No league found with id 999");
      }
    }

    @Nested
    @DisplayName("API error handling")
    class ApiErrorHandling {

      @Test
      @DisplayName("Throws CustomTooManyRequestsException when errors contain 'requests' key")
      void shouldThrowTooManyRequests_whenErrorsContainRequestsKey() {
        Map<String, Object> error = Map.of("requests", "Requests limit reached");
        when(footballApiClient.getLeagueById(39)).thenReturn(apiResponseWithErrors(List.of(error)));

        assertThatThrownBy(() -> leaguesService.getLeagueById(39))
            .isInstanceOf(CustomTooManyRequestsException.class)
            .hasMessageContaining("daily request limit");
      }

      @Test
      @DisplayName(
          "Throws CustomTooManyRequestsException with plan message when errors contain 'plan' key")
      void shouldThrowTooManyRequests_whenErrorsContainPlanKey() {
        String planMessage = "You have exceeded the limit of 100 requests per season";
        Map<String, Object> error = Map.of("plan", planMessage);
        when(footballApiClient.getLeagueById(39)).thenReturn(apiResponseWithErrors(List.of(error)));

        assertThatThrownBy(() -> leaguesService.getLeagueById(39))
            .isInstanceOf(CustomTooManyRequestsException.class)
            .hasMessage(planMessage);
      }

      @Test
      @DisplayName("Throws CustomBadGatewayException when errors contain an unknown key")
      void shouldThrowBadGateway_whenErrorsContainUnknownKey() {
        Map<String, Object> error = Map.of("token", "invalid");
        when(footballApiClient.getLeagueById(39)).thenReturn(apiResponseWithErrors(List.of(error)));

        assertThatThrownBy(() -> leaguesService.getLeagueById(39))
            .isInstanceOf(CustomBadGatewayException.class)
            .hasMessageContaining("error occurred while processing");
      }
    }
  }
}
