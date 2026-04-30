package com.sportspulse.leagues.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sportspulse.leagues.config.properties.CacheProperties;
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
 * Cache configuration for the leagues service.
 *
 * <p>Enables caching only when configured and defines explicit cache regions following the same
 * pattern used across the other microservices.
 */
@Configuration
@EnableCaching
@RequiredArgsConstructor
public class CacheConfig {

  private static final String LEAGUES_BY_ID = "leaguesById";
  private static final String LEAGUES_BY_FILTERS = "leaguesByFilters";

  private final CacheProperties properties;

  /**
   * Creates and configures the {@link CacheManager} for the application. If caching is disabled in
   * {@link Cache}, a {@link NoOpCacheManager} is returned. Otherwise, a {@link SimpleCacheManager}
   * is initialized with a {@link CaffeineCache} for teams data.
   *
   * @return the configured {@link CacheManager} instance
   */
  @Bean
  public CacheManager cacheManager() {
    if (!properties.isEnabled()) {
      return new NoOpCacheManager();
    }

    SimpleCacheManager manager = new SimpleCacheManager();
    manager.setCaches(List.of(buildCache(LEAGUES_BY_FILTERS), buildCache(LEAGUES_BY_ID)));
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
