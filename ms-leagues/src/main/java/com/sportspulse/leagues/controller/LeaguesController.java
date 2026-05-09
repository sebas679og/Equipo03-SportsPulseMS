package com.sportspulse.leagues.controller;

import com.sportspulse.leagues.config.constants.ApiPaths;
import com.sportspulse.leagues.dto.requests.CountryAndSeasonRequest;
import com.sportspulse.leagues.dto.responses.ErrorResponse;
import com.sportspulse.leagues.dto.responses.LeagueDetailResponse;
import com.sportspulse.leagues.dto.responses.LeagueSummary;
import com.sportspulse.leagues.dto.responses.LeaguesResponse;
import com.sportspulse.leagues.services.LeaguesService;
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

/** Controller for football leagues endpoints. */
@RestController
@RequiredArgsConstructor
@RequestMapping
@Tag(name = "Leagues", description = "Endpoints to query football leagues information")
public class LeaguesController {

  private final LeaguesService leaguesService;

  @Operation(
      summary = "List leagues",
      description = "Returns available leagues. Optional filters: country and season.",
      security = {@SecurityRequirement(name = "BearerAuth")})
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Leagues returned successfully",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = LeagueSummary.class))),
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized - Missing or invalid JWT token",
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
  @GetMapping(ApiPaths.Leagues.LEAGUES_BY_FILTER)
  public ResponseEntity<LeaguesResponse> getLeagues(
      @ParameterObject @Valid @ModelAttribute CountryAndSeasonRequest request) {
    return ResponseEntity.status(HttpStatus.OK).body(leaguesService.getLeagues(request));
  }

  @Operation(
      summary = "Get league details by ID",
      description =
          "Returns detailed information for a league including available seasons and "
              + "current season.",
      security = {@SecurityRequirement(name = "BearerAuth")})
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "League details returned successfully",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = LeagueDetailResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized - Missing or invalid JWT token",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "League not found",
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
  @GetMapping(ApiPaths.Leagues.LEAGUE_BY_ID)
  public ResponseEntity<LeagueDetailResponse> getLeagueById(
      @Parameter(
              required = true,
              name = "leagueId",
              description = "Unique identifier of a league",
              example = "33")
          @PathVariable
          int leagueId) {
    return ResponseEntity.ok(leaguesService.getLeagueById(leagueId));
  }
}
