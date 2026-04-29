package com.sportspulse.leagues.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.sportspulse.leagues.config.properties.CacheProperties;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.support.NoOpCacheManager;
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

  private static final String LEAGUES_CACHE = "leagues";
  private static final String LEAGUES_BY_FILTERS_CACHE = "leaguesByFilters";

  private final CacheProperties cacheProperties;

  @Bean
  public CacheManager cacheManager() {
    if (!cacheProperties.isEnabled()) {
      return new NoOpCacheManager();
    }

    CaffeineCacheManager manager = new CaffeineCacheManager();
    manager.setCacheNames(List.of(LEAGUES_CACHE, LEAGUES_BY_FILTERS_CACHE));
    manager.setCaffeine(
        Caffeine.newBuilder()
            .expireAfterWrite(cacheProperties.getTtlMinutes(), TimeUnit.MINUTES)
            .maximumSize(cacheProperties.getMaxSize())
            .recordStats());
    return manager;
  }
}
