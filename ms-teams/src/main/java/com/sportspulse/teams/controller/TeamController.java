package com.sportspulse.teams.controller;

import com.sportspulse.teams.config.constants.ApiPaths;
import com.sportspulse.teams.dto.responses.ErrorResponse;
import com.sportspulse.teams.dto.responses.TeamByIdResponse;
import com.sportspulse.teams.services.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
@Tag(
    name = "Team Controller",
    description =
        "Endpoints related to teams, allowing clients to retrieve "
            + "information about teams from API-Football.")
public class TeamController {

  private final TeamService teamService;

  @Operation(
      summary = "Equipment query by id",
      description =
          """
                   Endpoint in charge of querying the API-Football
                   for the team and returning a filtered response.
                   """,
      security = {
        @SecurityRequirement(name = "BearerAuth"),
        @SecurityRequirement(name = "InternalApiKey")
      })
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Successful response - team information retrieved successfully",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = TeamByIdResponse.class))),
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
  @GetMapping(ApiPaths.Teams.TEAM_BY_ID)
  public ResponseEntity<TeamByIdResponse> getTeamById(@PathVariable int teamId) {
    return ResponseEntity.status(HttpStatus.OK).body(teamService.getTeamById(teamId));
  }
}
