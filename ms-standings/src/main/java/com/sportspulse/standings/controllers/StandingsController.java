package com.sportspulse.standings.controllers;

import com.sportspulse.standings.config.constants.ApiPaths;
import com.sportspulse.standings.dtos.request.LeagueAndSeasonRequest;
import com.sportspulse.standings.dtos.responses.StandingsLeagueAndSeasonResponse;
import com.sportspulse.standings.dtos.responses.TeamStandingLeagueAndSeasonResponse;
import com.sportspulse.standings.services.StandingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Standings Controller. */
@RestController
@RequestMapping
@RequiredArgsConstructor
public class StandingsController {

  private final StandingsService standingsService;

  @GetMapping(ApiPaths.Standings.CLASSIFICATION_LEAGUE_IN_A_SEASON)
  public ResponseEntity<StandingsLeagueAndSeasonResponse> getStandingsByLeagueAndSeason(
      @ParameterObject @Valid @ModelAttribute LeagueAndSeasonRequest request) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(standingsService.getStandingsByLeagueAndSeason(request));
  }

  @GetMapping(ApiPaths.Standings.POSITION_TEAM_IN_THE_STANDINGS)
  public ResponseEntity<TeamStandingLeagueAndSeasonResponse> getTeamStandingsByLeagueAndSeason(
      @PathVariable int teamId,
      @ParameterObject @Valid @ModelAttribute LeagueAndSeasonRequest request) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(standingsService.getTeamStandingsByLeagueAndSeason(request, teamId));
  }
}
