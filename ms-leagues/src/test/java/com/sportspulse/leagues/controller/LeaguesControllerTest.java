package com.sportspulse.leagues.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sportspulse.leagues.LeaguesTestDataProvider;
import com.sportspulse.leagues.dto.responses.LeagueDetailResponse;
import com.sportspulse.leagues.dto.responses.LeagueSummaryResponse;
import com.sportspulse.leagues.services.LeaguesService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
@DisplayName("LeaguesController Tests")
class LeaguesControllerTest {

  @Mock private LeaguesService leaguesService;

  @InjectMocks private LeaguesController leaguesController;

  @Test
  @DisplayName("getLeagues should return 200 with service response")
  void getLeagues_shouldReturnOkWithBody() {
    List<LeagueSummaryResponse> expected = List.of(LeaguesTestDataProvider.summaryResponse());
    when(leaguesService.getLeagues("Spain", 2024)).thenReturn(expected);

    ResponseEntity<List<LeagueSummaryResponse>> response = leaguesController.getLeagues("Spain", 2024);

    assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    assertThat(response.getBody()).isEqualTo(expected);
    verify(leaguesService).getLeagues("Spain", 2024);
  }

  @Test
  @DisplayName("getLeagueById should return 200 with detail response")
  void getLeagueById_shouldReturnOkWithBody() {
    LeagueDetailResponse expected = LeaguesTestDataProvider.detailResponse();
    when(leaguesService.getLeagueById(140)).thenReturn(expected);

    ResponseEntity<LeagueDetailResponse> response = leaguesController.getLeagueById(140);

    assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    assertThat(response.getBody()).isEqualTo(expected);
    verify(leaguesService).getLeagueById(140);
  }
}
