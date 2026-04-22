package com.sportspulse.leagues.config.constants;

 
public final class ApiPaths {

  private ApiPaths() {}

  /** Leagues API endpoints. */
  public static final class Leagues {
    public static final String BASE = "/api/leagues";

    private Leagues() {}
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
