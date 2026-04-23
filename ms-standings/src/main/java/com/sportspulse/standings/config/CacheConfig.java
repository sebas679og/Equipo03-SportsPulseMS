package com.sportspulse.standings.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sportspulse.standings.config.properties.TeamsCacheProperties;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * CacheConfig Provides configuration for application-level caching. Enables caching support and
 * defines cache settings for teams data, including conditional activation based on {@link
 * TeamsCacheProperties}.
 */
@Configuration
@EnableCaching
@RequiredArgsConstructor
public class CacheConfig {

  private static final String STANDINGS_CACHE = "standingsLeagueAndSeason";

  private final TeamsCacheProperties properties;

  /**
   * Creates and configures the {@link CacheManager} for the application. If caching is disabled in
   * {@link TeamsCacheProperties}, a {@link NoOpCacheManager} is returned. Otherwise, a {@link
   * SimpleCacheManager} is initialized with a {@link CaffeineCache} for teams data.
   *
   * @return the configured {@link CacheManager} instance
   */
  @Bean
  public CacheManager cacheManager() {
    if (!properties.isEnabled()) {
      return new NoOpCacheManager();
    }

    SimpleCacheManager manager = new SimpleCacheManager();
    manager.setCaches(List.of(buildCache(STANDINGS_CACHE)));
    return manager;
  }

  private CaffeineCache buildCache(String name) {
    Cache<Object, Object> cache =
        Caffeine.newBuilder()
            .expireAfterWrite(properties.getTtlMinutes(), TimeUnit.MINUTES)
            .maximumSize(properties.getMaxSize())
            .recordStats()
            .build();

    return new CaffeineCache(name, cache);
  }
}
