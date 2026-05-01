package com.sportspulse.gateway.controller;

import com.sportspulse.gateway.config.constants.ApiPaths;
import com.sportspulse.gateway.dto.responses.HealthResponse;
import com.sportspulse.gateway.services.HealthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * HealthController REST controller that exposes the health check endpoint. Delegates to {@link
 * HealthService} to retrieve the aggregated health status of the gateway and its dependent
 * services.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.HEALTH)
@Tag(
    name = "Health",
    description = "Endpoints related to the health status of the application and its dependencies")
public class HealthController {

  private final HealthService healthService;

  @Operation(
      summary = "Application health",
      description =
          """
        Endpoint responsible for querying the diagnosis of the application's services.
        """)
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Returns the health status of the system and its corresponding microservices",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = HealthResponse.class),
                examples =
                    @ExampleObject(
                        name = "Health Response Example",
                        value =
                            """
                    {
                      "gateway": "UP",
                      "timestamp": "2025-01-15T10:30:000Z",
                      "dependencies": "UP",
                      "services": {
                        "ms-auth": "UP",
                        "ms-leagues": "UP",
                        "ms-teams": "UP",
                        "ms-fixtures": "UP",
                        "ms-standings": "UP",
                        "ms-notifications": "UP",
                        "ms-dashboard": "UP"
                      }
                    }
                    """)))
  })
  @GetMapping
  public Mono<HealthResponse> getHealthServices() {
    return healthService.getHeathStatusServices();
  }
}
