package com.sportspulse.teams.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** Constants related to HTTP headers. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HttpHeaders {
  public static final String AUTHORIZATION = "Authorization";
  public static final String X_API_SPORTS_KEY = "x-apisports-key";
  public static final String X_INTERNAL_API_KEY = "X-Internal-API-Key";
}
