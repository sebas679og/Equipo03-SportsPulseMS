package com.sportspulse.teams.controller;

import com.sportspulse.teams.config.constants.ApiPaths;
import com.sportspulse.teams.dto.responses.TeamResponse;
import com.sportspulse.teams.services.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TeamController REST controller that exposes endpoints for team-related operations.
 *
 * <p>Provides an API to retrieve team information by ID, delegating the business logic to the
 * {@link TeamService}. Responses are returned as {@link ResponseEntity} objects with appropriate
 * HTTP status codes.
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
public class TeamController {

  private final TeamService teamService;

  @GetMapping(ApiPaths.Teams.TEAM_BY_ID)
  public ResponseEntity<TeamResponse> getTeamById(@PathVariable int teamId) {
    return ResponseEntity.status(HttpStatus.OK).body(teamService.getTeamById(teamId));
  }
}
