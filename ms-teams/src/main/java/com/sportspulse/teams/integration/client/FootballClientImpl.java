package com.sportspulse.teams.integration.client;

import com.sportspulse.teams.integration.dto.response.teamid.ApiFootballTeamResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * FootballClientImpl Implementation of the {@link FootballClient} interface. Provides methods to
 * interact with the Football API and retrieve team-related information.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FootballClientImpl implements FootballClient {

  @Override
  public ApiFootballTeamResponse getApiFootballTeamById(int teamId) {
    return null;
  }
}
