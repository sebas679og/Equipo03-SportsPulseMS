package com.sportspulse.fixtures.constants;

import java.util.Set;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Utility class that defines constant values for HTTP headers, query parameters, and security roles
 * used throughout the application.
 *
 * <p>This class centralizes metadata for:
 *
 * <ul>
 *   <li><b>External API:</b> Headers and parameters required by the Football API provider.
 *   <li><b>Authentication:</b> Internal security keys, JWT prefixes, and Spring Security roles.
 *   <li><b>Live Match Tracking:</b> Status codes representing active play.
 * </ul>
 *
 * @author Sportspulse Team
 * @version 1.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpHeaders {

  /**
   * Set of status codes representing that a match is currently live.
   *
   * <ul>
   *   <li><b>1H:</b> First Half
   *   <li><b>HT:</b> Halftime
   *   <li><b>2H:</b> Second Half
   * </ul>
   */
  public static final Set<String> LIVE_STATUS = Set.of("1H", "HT", "2H");

  /** Constants specifically for the Api-Football external service. */
  public static final class ApiFootball {

    /** HTTP Header names required by the provider. */
    public static final class Headers {
      public static final String KEY = "x-apisports-key";
    }

    /** Query parameter names used in URI building. */
    public static final class Params {
      public static final String FIXTURE = "fixture";
      public static final String LEAGUE = "league";
      public static final String TEAM = "team";
      public static final String DATE = "date";
      public static final String STATUS = "status";
      public static final String LIVE = "live";
    }

    /** Fixed values for specific query parameters. */
    public static final class Values {
      public static final String ALL = "all";
    }
  }

  /** Constants for internal and external security management. */
  public static final class Auth {
    public static final String AUTH_INTERNAL_KEY = "X-Internal-API-Key";
    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER = "Bearer ";
    public static final String ROLE_AUTH_JWT = "AUTH_JWT";
    public static final String ROLE_AUTH_INTERNAL = "AUTH_INTERNAL";
    public static final String ROLE_USER = "USER";
    public static final String PREFIX_ROLE = "ROLE_";
  }
}
