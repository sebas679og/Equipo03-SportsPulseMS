package com.sportspulse.teams.controllers;

import com.sportspulse.teams.constants.ApiPaths;
import com.sportspulse.teams.constants.CacheConstants;
import com.sportspulse.teams.dto.response.ErrorResponse;
import com.sportspulse.teams.dto.response.TeamResponse;
import com.sportspulse.teams.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for querying football teams.
 *
 * <p>Exposes endpoints to retrieve team information by league and season,
 * sourced from the API-Football external service.
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Teams", description = "Endpoints for querying football teams by league and season.")
public class TeamController {

    private final TeamService teamService;

    /**
     * Retrieves the list of teams participating in a league during a specific season.
     *
     * @param league the league identifier (e.g. 39 for Premier League).
     * @param season the season year (e.g. 2023).
     * @return a {@link ResponseEntity} containing the list of {@link TeamResponse}.
     */
    @Operation(
            summary = "Get teams by league and season",
            description = "Returns all teams participating in a given league and season. " +
                    "Requires a valid JWT token.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Teams retrieved successfully.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TeamResponse.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Missing or invalid required parameters: league or season.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid JWT token.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected error or API-Football service unavailable.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "BearerAuth")
    @Cacheable(value = CacheConstants.TEAMS_CACHE, key = CacheConstants.TEAMS_CACHE_LEAGUE_SEASON_KEY)
    @GetMapping(ApiPaths.Team.BASE)
    public ResponseEntity<List<TeamResponse>> getTeams(
            @Parameter(description = "League identifier (e.g. 39 for Premier League).", required = true)
            @RequestParam Integer league,
            @Parameter(description = "Season year (e.g. 2023).", required = true)
            @RequestParam Integer season) {

        return ResponseEntity.ok(teamService.getTeams(league, season));
    }
}
