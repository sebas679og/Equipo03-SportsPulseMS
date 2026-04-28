package com.sportspulse.standings.controllers;

import com.sportspulse.standings.config.constants.ApiPaths;
import com.sportspulse.standings.dtos.request.LeagueAndSeasonRequest;
import com.sportspulse.standings.dtos.responses.ErrorResponse;
import com.sportspulse.standings.dtos.responses.StandingsLeagueAndSeasonResponse;
import com.sportspulse.standings.dtos.responses.TeamStandingLeagueAndSeasonResponse;
import com.sportspulse.standings.services.StandingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * StandingsController REST controller responsible for exposing endpoints related to football
 * standings.
 *
 * <p>Provides HTTP GET endpoints for retrieving league standings by season and team-specific
 * standings within a league. Delegates business logic to the {@link StandingsService}.
 *
 * <p>Built as a Spring-managed REST controller with Lombok annotations {@link
 * RequiredArgsConstructor} for constructor-based dependency injection.
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(
    name = "Standings Controller",
    description =
        "Endpoints related to football standings, allowing clients to retrieve "
            + "information about league standings and team positions from API-Football.")
public class StandingsController {

  private final StandingsService standingsService;

  @Operation(
      summary = "Standings query by league and season",
      description =
          """
                    Endpoint in charge of querying the API-Football
                    for the standings of a league in a specific season
                    and returning a filtered response.
                   """,
      security = {@SecurityRequirement(name = "BearerAuth")})
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Successful response - standings information retrieved successfully",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = StandingsLeagueAndSeasonResponse.class))),
    @ApiResponse(
        responseCode = "400",
        description = "Validation error - Invalid path parameter",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized - Invalid API key or missing authentication",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Not Found - team not found for the provided ID",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "429",
        description = "Too Many Requests - API-Football rate limit exceeded",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "502",
        description = "Bad Gateway - Error communicating with API-Football or ms-auth",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "503",
        description = "Service Unavailable - API-Football or ms-auth service is down",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
  })
  @GetMapping(ApiPaths.Standings.CLASSIFICATION_LEAGUE_IN_A_SEASON)
  public ResponseEntity<StandingsLeagueAndSeasonResponse> getStandingsByLeagueAndSeason(
      @ParameterObject @Valid @ModelAttribute LeagueAndSeasonRequest request) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(standingsService.getStandingsByLeagueAndSeason(request));
  }

  @Operation(
      summary = "Team standings query by league and season",
      description =
          """
                    Endpoint in charge of querying the API-Football
                    for the standings of a league in a specific
                    season and returning the position of a team.
                   """,
      security = {@SecurityRequirement(name = "BearerAuth")})
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Successful response - team standings information retrieved successfully",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = TeamStandingLeagueAndSeasonResponse.class))),
    @ApiResponse(
        responseCode = "400",
        description = "Validation error - Invalid path parameter",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized - Invalid API key or missing authentication",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Not Found - team not found for the provided ID",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "429",
        description = "Too Many Requests - API-Football rate limit exceeded",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "502",
        description = "Bad Gateway - Error communicating with API-Football or ms-auth",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "503",
        description = "Service Unavailable - API-Football or ms-auth service is down",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
  })
  @GetMapping(ApiPaths.Standings.POSITION_TEAM_IN_THE_STANDINGS)
  public ResponseEntity<TeamStandingLeagueAndSeasonResponse> getTeamStandingsByLeagueAndSeason(
      @Parameter(
              required = true,
              name = "teamId",
              description =
                  "The unique identifier of the team for which to retrieve standings information",
              example = "33")
          @PathVariable
          int teamId,
      @ParameterObject @Valid @ModelAttribute LeagueAndSeasonRequest request) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(standingsService.getTeamStandingsByLeagueAndSeason(request, teamId));
  }
}
