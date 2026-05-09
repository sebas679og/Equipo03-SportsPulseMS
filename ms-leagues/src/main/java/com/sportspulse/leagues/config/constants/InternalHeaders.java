package com.sportspulse.leagues.config.constants;

/** Internal header constants. */
public final class InternalHeaders {

  private InternalHeaders() {}

  /** Header constants for the API-Football integration. */
  public static final class ApiFootball {
    public static final String API_FOOTBALL_KEY = "x-apisports-key";

    private ApiFootball() {}
  }

  /** Header constants for the ms-auth integration. */
  public static final class MsAuth {
    public static final String MS_AUTH_KEY = "X-Internal-API-Key";
    public static final String BEARER_HEADER = "Authorization";
    public static final String TYPE_TOKEN = "Bearer";

    private MsAuth() {}
  }
}
