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
  }

  /** API documentation endpoints. */
  public static final class Docs {
    public static final String SWAGGER_UI = "/swagger-ui/**";
    public static final String API_DOCS = "/v3/api-docs/**";
  }
}
