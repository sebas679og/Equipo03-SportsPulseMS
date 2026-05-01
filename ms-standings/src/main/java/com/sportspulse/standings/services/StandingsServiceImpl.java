package com.sportspulse.standings.services;

import com.sportspulse.standings.dtos.request.LeagueAndSeasonRequest;
import com.sportspulse.standings.dtos.responses.StandingsLeagueAndSeasonResponse;
import com.sportspulse.standings.dtos.responses.TeamStandingLeagueAndSeasonResponse;
import com.sportspulse.standings.exceptions.CustomBadRequestException;
import com.sportspulse.standings.exceptions.CustomNotFoundException;
import com.sportspulse.standings.integrations.football.dto.ApiLeague;
import com.sportspulse.standings.integrations.football.dto.ApiStanding;
import com.sportspulse.standings.integrations.football.dto.ApiStandingsResponse;
import com.sportspulse.standings.services.complements.ApiFootballStandingsFetcher;
import com.sportspulse.standings.utils.mappers.StandingsMapper;
import com.sportspulse.standings.utils.mappers.TeamStandingMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * StandingsServiceImpl Implementation of the {@link StandingsService} interface that provides
 * access to league standings data.
 *
 * <p>Acts as a Spring-managed service component, delegating requests to underlying clients or
 * mappers to retrieve and transform football standings information. Built with Lombok annotations
 * {@link Slf4j} and {@link RequiredArgsConstructor} for logging support and constructor-based
 * dependency injection.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StandingsServiceImpl implements StandingsService {

  private final ApiFootballStandingsFetcher apiFootballStandingsFetcher;
  private final StandingsMapper standingsMapper;
  private final TeamStandingMapper teamStandingMapper;

  @Override
  public StandingsLeagueAndSeasonResponse getStandingsByLeagueAndSeason(
      LeagueAndSeasonRequest request) {

    ApiStandingsResponse api =
        apiFootballStandingsFetcher.fetchValidatedStandings(
            request.getLeague(), Integer.parseInt(request.getSeason()));

    ApiLeague apiLeague = api.response().getFirst().league();

    return standingsMapper.toResponseFromLeague(apiLeague);
  }

  @Override
  public TeamStandingLeagueAndSeasonResponse getTeamStandingsByLeagueAndSeason(
      LeagueAndSeasonRequest request, int teamId) {
    if (teamId <= 0) {
      throw new CustomBadRequestException("teamId must be a positive number greater than 0.");
    }

    ApiStandingsResponse api =
        apiFootballStandingsFetcher.fetchValidatedStandings(
            request.getLeague(), Integer.parseInt(request.getSeason()));

    ApiLeague apiLeague = api.response().getFirst().league();

    ApiStanding apiStanding =
        apiLeague.standings().stream()
            .flatMap(List::stream)
            .filter(s -> s.team().id() == teamId)
            .findFirst()
            .orElseThrow(
                () ->
                    new CustomNotFoundException(
                        String.format(
                            "Team %d not found in standings for league %d and season %s",
                            teamId, request.getLeague(), request.getSeason())));

    return teamStandingMapper.toResponse(apiStanding, apiLeague);
  }
}
