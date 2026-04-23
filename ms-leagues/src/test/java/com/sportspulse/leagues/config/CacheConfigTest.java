package com.sportspulse.leagues.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

@DisplayName("CacheConfig Tests")
class CacheConfigTest {

  private final CacheConfig cacheConfig = new CacheConfig();

  @Test
  @DisplayName("cacheManager should expose configured caches")
  void cacheManager_shouldExposeExpectedCaches() {
    CacheManager cacheManager = cacheConfig.cacheManager();

    Cache leaguesByFilters = cacheManager.getCache(CacheConfig.LEAGUES_BY_FILTERS_CACHE);
    Cache leagueById = cacheManager.getCache(CacheConfig.LEAGUE_BY_ID_CACHE);

    assertThat(leaguesByFilters).isNotNull();
    assertThat(leagueById).isNotNull();
  }
}
