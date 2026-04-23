package com.sportspulse.leagues.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sportspulse.leagues.LeaguesTestDataProvider;
import com.sportspulse.leagues.dto.responses.LeagueDetailResponse;
import com.sportspulse.leagues.dto.responses.LeagueSummaryResponse;
import com.sportspulse.leagues.exceptions.LeagueNotFoundException;
import com.sportspulse.leagues.integration.client.FootballApiClient;
import com.sportspulse.leagues.integration.dto.ApiFootballLeagueWrapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("LeaguesServiceImpl Tests")
class LeaguesServiceImplTest {

  @Mock private FootballApiClient footballApiClient;

  @InjectMocks private LeaguesServiceImpl leaguesService;

  @Test
  @DisplayName("getLeagues should trim country and map summary fields")
  void getLeagues_shouldNormalizeCountryAndMapResponse() {
    ApiFootballLeagueWrapper wrapper = LeaguesTestDataProvider.apiLeagueWrapper();
    when(footballApiClient.getLeagues("Spain", 2024, null)).thenReturn(List.of(wrapper));

    List<LeagueSummaryResponse> response = leaguesService.getLeagues("  Spain  ", 2024);

    assertThat(response).hasSize(1);
    assertThat(response.getFirst().getId()).isEqualTo(LeaguesTestDataProvider.LEAGUE_ID);
    assertThat(response.getFirst().getCountry()).isEqualTo(LeaguesTestDataProvider.COUNTRY);
    assertThat(response.getFirst().getCurrentSeason())
        .isEqualTo(LeaguesTestDataProvider.CURRENT_SEASON);
    verify(footballApiClient).getLeagues("Spain", 2024, null);
  }

  @Test
  @DisplayName("getLeagueById should map detail response including seasons")
  void getLeagueById_shouldMapDetailResponse() {
    ApiFootballLeagueWrapper wrapper = LeaguesTestDataProvider.apiLeagueWrapper();
    when(footballApiClient.getLeagues(null, null, 140)).thenReturn(List.of(wrapper));

    LeagueDetailResponse response = leaguesService.getLeagueById(140);

    assertThat(response.getId()).isEqualTo(140);
    assertThat(response.getName()).isEqualTo("La Liga");
    assertThat(response.getSeasons()).containsExactly(2023, 2024);
    assertThat(response.getCurrentSeason()).isNotNull();
    assertThat(response.getCurrentSeason().getYear()).isEqualTo(2024);
    verify(footballApiClient).getLeagues(null, null, 140);
  }

  @Test
  @DisplayName("getLeagueById should throw LeagueNotFoundException when id does not exist")
  void getLeagueById_shouldThrowWhenLeagueIsMissing() {
    when(footballApiClient.getLeagues(null, null, 999)).thenReturn(List.of());

    assertThatThrownBy(() -> leaguesService.getLeagueById(999))
        .isInstanceOf(LeagueNotFoundException.class)
        .hasMessageContaining("No existe una liga");

    verify(footballApiClient).getLeagues(null, null, 999);
  }
}
