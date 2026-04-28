package com.sportspulse.gateway.config.constants;

/** ApiPathsServices Defines base API paths for service endpoints. */
public final class ApiPathsServices {

  private ApiPathsServices() {}

  public static final String ALL_PATH = "/**";

  /** Auth Contains API paths related to authentication services. */
  public static final class Auth {
    public static final String BASE = "/api/auth";
    public static final String ALL = String.join("", BASE, ALL_PATH);
    public static final String LOGIN = String.join("", BASE, "/login");
    public static final String VALIDATE_TOKEN = String.join("", BASE, "/validate");
  }

  /** Leagues Defines constant API paths related to league operations. */
  public static final class Leagues {
    public static final String BASE = "/api/leagues";
    public static final String ALL = String.join("", BASE, ALL_PATH);
  }

  /**
   * Teams Defines constant API paths related to team operations.
   *
   * <p>Provides base and wildcard paths for team-related endpoints, ensuring centralized and
   * consistent URL management across the application.
   */
  public static final class Teams {
    public static final String BASE = "/api/teams";
    public static final String ALL = String.join("", BASE, ALL_PATH);
  }

  /** Fixtures Defines constant API paths related to league operations. */
  public static final class Fixtures {
    public static final String BASE = "/api/fixtures";
    public static final String ALL = String.join("", BASE, ALL_PATH);
  }

  /**
   * Standings Defines constant API paths related to standings operations.
   *
   * <p>Provides base and wildcard paths for standings-related endpoints, ensuring centralized and
   * consistent URL management across the application.
   */
  public static final class Standings {
    public static final String BASE = "/api/standings";
    public static final String ALL = String.join("", BASE, ALL_PATH);
  }

  /** Notifications Defines constant API paths related to league operations. */
  public static final class Notifications {
    public static final String BASE = "/api/notifications";
    public static final String ALL = String.join("", BASE, ALL_PATH);
  }

  /** Dashboard Defines constant API paths related to league operations. */
  public static final class Dashboard {
    public static final String BASE = "/api/dashboard";
    public static final String ALL = String.join("", BASE, ALL_PATH);
  }

  /**
   * Health Defines constants related to health check endpoints. Provides the actuator health path
   * for monitoring service availability.
   */
  public static final class Health {
    public static final String ACTUATOR_HEALTH = "/actuator/health";
  }
}
