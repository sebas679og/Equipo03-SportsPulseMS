package com.sportspulse.leagues.controller;

import com.sportspulse.leagues.config.constants.ApiPaths;
import com.sportspulse.leagues.dto.responses.LeagueDetailResponse;
import com.sportspulse.leagues.dto.responses.LeagueErrorResponse;
import com.sportspulse.leagues.dto.responses.LeagueSummaryResponse;
import com.sportspulse.leagues.services.LeaguesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Controller for football leagues endpoints. */
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.Leagues.BASE)
@Tag(name = "Leagues", description = "Endpoints to query football leagues information")
public class LeaguesController {

  private final LeaguesService leaguesService;

    @Operation(
      summary = "List leagues",
      description =
        "Returns available leagues. Optional filters: country and season.")
    @ApiResponses({
    @ApiResponse(
      responseCode = "200",
      description = "Leagues returned successfully",
      content =
        @Content(
          mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = LeagueSummaryResponse.class))),
    @ApiResponse(
      responseCode = "401",
      description = "Unauthorized - Missing or invalid JWT token",
      content =
        @Content(
          mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = LeagueErrorResponse.class)))
    })
  @GetMapping
  public ResponseEntity<List<LeagueSummaryResponse>> getLeagues(
      @RequestParam(required = false) String country,
      @RequestParam(required = false) Integer season) {
    return ResponseEntity.ok(leaguesService.getLeagues(country, season));
  }

    @Operation(
      summary = "Get league details by ID",
      description =
        "Returns detailed information for a league including available seasons and current season.")
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
          schema = @Schema(implementation = LeagueErrorResponse.class))),
    @ApiResponse(
      responseCode = "404",
      description = "League not found",
      content =
        @Content(
          mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = LeagueErrorResponse.class)))
    })
  @GetMapping("/{leagueId}")
  public ResponseEntity<LeagueDetailResponse> getLeagueById(@PathVariable Integer leagueId) {
    return ResponseEntity.ok(leaguesService.getLeagueById(leagueId));
  }
}
