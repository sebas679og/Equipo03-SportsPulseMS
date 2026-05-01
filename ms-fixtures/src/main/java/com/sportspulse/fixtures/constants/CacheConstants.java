package com.sportspulse.fixtures.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Utility class that defines constant names for the application caches.
 *
 * <p>These constants are used as identifiers in the {@code @Cacheable} annotations and within the
 * {@link com.sportspulse.fixtures.config.CacheConfig} to ensure consistency across the caching
 * layer.
 *
 * @author Sportspulse Team
 * @version 1.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CacheConstants {
  public static final String FIXTURES_CACHE = "fixture";
  public static final String FIXTURES_EVENTS_CACHE = "fixtureEvents";
  public static final String FIXTURES_LIVE_CACHE = "fixtureLive";
}
