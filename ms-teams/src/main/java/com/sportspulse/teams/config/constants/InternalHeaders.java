package com.sportspulse.teams.config.constants;

/**
 * InternalHeaders Defines internal HTTP header constants used for communication between services
 * and external APIs. Provides centralized keys to ensure consistency and avoid duplication.
 */
public final class InternalHeaders {

  private InternalHeaders() {}

  /** ApiFootball Contains header constants specific to the Football API integration. */
  public static final class ApiFootball {
    public static final String API_FOOTBALL_KEY = "x-apisports-key";
  }

  /** MsAuth Contains header constants specific to internal authentication between microservices. */
  public static final class MsAuth {
    public static final String MS_AUTH_KEY = "X-Internal-API-Key";
    public static final String TYPE_TOKEN = "Bearer";
  }
}
