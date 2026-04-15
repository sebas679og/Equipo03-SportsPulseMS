package com.sportspulse.gateway.config.constants;

/** ApiPathsServices Defines base API paths for service endpoints. */
public final class ApiPathsServices {

  private ApiPathsServices() {}

  /** Auth Contains API paths related to authentication services. */
  public static final class Auth {
    public static final String BASE = "/api/auth";
    public static final String ALL = String.join("", BASE, "/**");
    public static final String LOGIN = String.join("", BASE, "/login");
    public static final String VALIDATE_TOKEN = String.join("", BASE, "/validate");
  }

  /**
   * Health Defines constants related to health check endpoints. Provides the actuator health path
   * for monitoring service availability.
   */
  public static final class Health {
    public static final String ACTUATOR_HEALTH = "/actuator/health";
  }
}
