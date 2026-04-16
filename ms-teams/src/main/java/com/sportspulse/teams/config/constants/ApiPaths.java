package com.sportspulse.teams.config.constants;

/**
 * ApiPaths Defines centralized API endpoint paths used across the application. Provides a single
 * source of truth to ensure consistency and avoid duplication.
 */
public final class ApiPaths {

  private ApiPaths() {}

  /**
   * AuthService Defines API endpoint paths related to the authentication service. Provides
   * constants to ensure consistency when building requests.
   */
  public static final class AuthService {
    public static final String VALIDATE_TOKEN = "/api/auth/validate";
  }

  /**
   * Teams Defines API endpoint paths related to team services. Provides constants to ensure
   * consistency when building requests for health checks and monitoring.
   */
  public static final class Teams {
    public static final String ACTUATOR_HEALTH = "/actuator/health";
  }
}
