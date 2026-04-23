package com.sportspulse.leagues.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Configures in-memory caches for leagues endpoints. */
@Configuration
@EnableCaching
public class CacheConfig {

  public static final String LEAGUES_BY_FILTERS_CACHE = "leaguesByFilters";
  public static final String LEAGUE_BY_ID_CACHE = "leagueById";

  /** Creates the cache manager with configured caches and eviction policy. */
  @Bean
  public CacheManager cacheManager() {
    CaffeineCacheManager manager = new CaffeineCacheManager();
    manager.setCacheNames(List.of(LEAGUES_BY_FILTERS_CACHE, LEAGUE_BY_ID_CACHE));
    manager.setCaffeine(Caffeine.newBuilder().expireAfterWrite(6, TimeUnit.HOURS).maximumSize(512));
    return manager;
  }
}
