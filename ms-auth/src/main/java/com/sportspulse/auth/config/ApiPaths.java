package com.sportspulse.auth.config;

/**
 * Centralized definition of API endpoint paths.
 *
 * <p>This class groups all route constants used across the application, avoiding duplicated string
 * literals and improving maintainability.
 */
public final class ApiPaths {

  private ApiPaths() {}

  /** Authentication-related endpoints. */
  public static final class Auth {
    public static final String REGISTER = "/api/auth/register";
    public static final String LOGIN = "/api/auth/login";
  }

  /** Endpoints related to token validation. */
  public static final class Validate {
    public static final String TOKEN = "/api/auth/validate";
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
