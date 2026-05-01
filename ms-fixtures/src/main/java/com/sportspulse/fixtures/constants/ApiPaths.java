package com.sportspulse.fixtures.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Centralized utility class containing all API endpoint paths for the application.
 *
 * <p>This class is organized into static inner classes to categorize paths based on their target
 * service or domain (e.g., Auth, External API, Internal Fixtures). It is marked as {@code final}
 * and has a private constructor to prevent instantiation.
 *
 * @author Sportspulse Team
 * @version 1.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ApiPaths {

  /** Paths related to the internal Authentication Service. */
  public static final class Auth {
    public static final String VALIDATE = "/api/auth/validate";
  }

  /** Paths for the external Api-Football provider. */
  public static final class ApiFootball {
    public static final String FIXTURES = "/fixtures";
    public static final String FIXTURES_EVENTS = "/fixtures/events";
  }

  /** Internal REST controller endpoints and health checks. */
  public static final class Fixtures {
    public static final String FIXTURES = "/api/fixtures";
    public static final String FIXTURE_LIVE = "/api/fixtures/live";
    public static final String FIXTURE_EVENTS = "/api/fixtures/{fixtureId}/events";
    public static final String ACTUATOR_HEALTH = "/actuator/health";
  }

  /** Paths for API Documentation and Swagger UI access. */
  public static final class Docs {
    public static final String SWAGGER_UI = "/swagger-ui/**";
    public static final String API_DOCS = "/v3/api-docs/**";
  }
}
