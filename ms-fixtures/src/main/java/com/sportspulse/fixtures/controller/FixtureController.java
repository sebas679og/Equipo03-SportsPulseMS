package com.sportspulse.fixtures.controller;

import com.sportspulse.fixtures.constants.ApiPaths;
import com.sportspulse.fixtures.dto.request.FixtureFilterRequest;
import com.sportspulse.fixtures.dto.response.ErrorResponse;
import com.sportspulse.fixtures.dto.response.FixtureResponse;
import com.sportspulse.fixtures.dto.response.event.FixtureEventResponse;
import com.sportspulse.fixtures.dto.response.live.FixtureLiveResponse;
import com.sportspulse.fixtures.service.FixtureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller providing endpoints for football fixture management.
 *
 * <p>This controller serves as the primary entry point for clients to query match information,
 * including filtered fixture lists, live updates, and match-specific events. It integrates with
 * {@link FixtureService} to process business logic and handles OpenAPI documentation through
 * Swagger annotations.
 *
 * @author Sportspulse Team
 * @version 1.0
 */
@RestController
@RequiredArgsConstructor
@Tag(
    name = "Fixture Controller",
    description =
        "Endpoints related to fixtures, allowing clients to retrieve "
            + "information about matches from API-Football.")
public class FixtureController {

  private final FixtureService fixtureService;

  /**
   * Retrieves a list of fixtures filtered by the criteria provided in the request.
   *
   * <p>Filters can include league, team, specific date, or match status. If no parameters are
   * provided, the system defaults to fetching fixtures for the current day.
   *
   * @param request {@link FixtureFilterRequest} object containing the query filters.
   * @return A {@link ResponseEntity} containing a list of {@link FixtureResponse}.
   */
  @Operation(
      summary = "Get fixtures by filters",
      description =
          """
            Returns a list of fixtures filtered by league, team, date or status.
            If no filter is provided, returns today's fixtures.
            """,
      security = @SecurityRequirement(name = "BearerAuth"))
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Successful response - fixtures retrieved successfully",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = FixtureResponse.class))),
    @ApiResponse(
        responseCode = "400",
        description = "Validation error - Invalid query parameter",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized - Invalid or missing authentication",
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
        description = "Service Unavailable - API-Football or ms-auth is down",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping(ApiPaths.Fixtures.FIXTURES)
  public ResponseEntity<List<FixtureResponse>> getFixtures(
      @ParameterObject @Valid @ModelAttribute FixtureFilterRequest request) {
    return ResponseEntity.ok(fixtureService.getFixtures(request));
  }

  /**
   * Retrieves all football matches currently in progress.
   *
   * <p>The response includes real-time data such as the current elapsed minute and live scores for
   * ongoing fixtures.
   *
   * @return A {@link ResponseEntity} containing a list of {@link FixtureLiveResponse}.
   */
  @Operation(
      summary = "Get live fixtures",
      description = "Returns all fixtures currently in progress with the current elapsed minute.",
      security = @SecurityRequirement(name = "BearerAuth"))
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Successful response - live fixtures retrieved successfully",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = FixtureLiveResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized - Invalid or missing authentication",
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
        description = "Service Unavailable - API-Football or ms-auth is down",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping(ApiPaths.Fixtures.FIXTURE_LIVE)
  public ResponseEntity<List<FixtureLiveResponse>> getLiveFixtures() {
    return ResponseEntity.ok(fixtureService.getLiveFixtures());
  }

  /**
   * Retrieves a detailed list of events for a specific match.
   *
   * <p>Events include key match incidents such as goals, yellow/red cards, and player
   * substitutions.
   *
   * @param fixtureId The unique identifier of the fixture to query.
   * @return A {@link ResponseEntity} containing a list of {@link FixtureEventResponse}.
   */
  @Operation(
      summary = "Get fixture events",
      description = "Returns all events (goals, cards, substitutions) for a specific fixture.",
      security = @SecurityRequirement(name = "BearerAuth"))
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Successful response - fixture events retrieved successfully",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = FixtureEventResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized - Invalid or missing authentication",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Not Found - fixture not found for the provided ID",
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
        description = "Service Unavailable - API-Football or ms-auth is down",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping(ApiPaths.Fixtures.FIXTURE_EVENTS)
  public ResponseEntity<List<FixtureEventResponse>> getFixtureEvents(@PathVariable Long fixtureId) {
    return ResponseEntity.ok(fixtureService.getFixtureEvents(fixtureId));
  }
}
