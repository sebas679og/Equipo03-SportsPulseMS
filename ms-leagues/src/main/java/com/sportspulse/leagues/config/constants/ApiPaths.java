package com.sportspulse.leagues.config.constants;

/** API route constants used by the leagues service. */
public final class ApiPaths {

  private ApiPaths() {}

  /**
   * AuthService Defines API endpoint paths related to the authentication service. Provides
   * constants to ensure consistency when building requests.
   */
  public static final class AuthService {
    public static final String VALIDATE_TOKEN = "/api/auth/validate";
  }

  /** Leagues API endpoints. */
  public static final class Leagues {
    public static final String BASE = "/api/leagues";
  }

  /** API documentation endpoints. */
  public static final class Docs {
    public static final String SWAGGER_UI = "/swagger-ui/**";
    public static final String API_DOCS = "/v3/api-docs/**";
  }

  /** Actuator endpoints for monitoring and health checks. */
  public static final class State {
    public static final String HEALTH = "/actuator/health";
  }
}
