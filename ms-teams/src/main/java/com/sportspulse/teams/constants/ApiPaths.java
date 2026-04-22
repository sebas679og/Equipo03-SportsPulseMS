package com.sportspulse.teams.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** Contains ApiPaths constants used across the teams module. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiPaths {

  /** Authentication-related API endpoints. */
  public static final class Auth {
    public static final String VALIDATE = "/api/auth/validate";
  }

  /** Team-related API endpoints. */
  public static final class Team {
    public static final String BASE = "/api/teams";
  }

  /** Swagger and OpenAPI documentation endpoints. */
  public static final class Docs {
    public static final String SWAGGER_UI = "/swagger-ui/**";
    public static final String SWAGGER_UI_HTML = "/swagger-ui.html";
    public static final String API_DOCS = "/v3/api-docs/**";
  }

  /** External API-Football parameter and path constants. */
  public static final class ApiFootball {
    public static final String TEAMS_PATH = "/teams";
    public static final String LEAGUE_PARAM = "league";
    public static final String SEASON_PARAM = "season";
  }
}
