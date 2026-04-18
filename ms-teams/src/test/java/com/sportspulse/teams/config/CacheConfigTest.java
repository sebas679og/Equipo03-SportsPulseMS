package com.sportspulse.teams.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.sportspulse.teams.config.properties.TeamsCacheProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.cache.support.SimpleCacheManager;

@ExtendWith(MockitoExtension.class)
class CacheConfigTest {

  @Mock private TeamsCacheProperties properties;

  @InjectMocks private CacheConfig cacheConfig;

  private CacheManager buildEnabledCacheManager(long ttlMinutes, long maxSize) {
    given(properties.isEnabled()).willReturn(true);
    given(properties.getTtlMinutes()).willReturn(ttlMinutes);
    given(properties.getMaxSize()).willReturn(maxSize);

    CacheManager manager = cacheConfig.cacheManager();

    if (manager instanceof SimpleCacheManager simpleCacheManager) {
      simpleCacheManager.afterPropertiesSet();
    }

    return manager;
  }

  // -------------------------------------------------------------------------
  // cacheManager() — caching disabled
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("cacheManager() returns NoOpCacheManager when caching is disabled")
  void cacheManager_whenCachingDisabled_returnsNoOpCacheManager() {
    given(properties.isEnabled()).willReturn(false);

    CacheManager result = cacheConfig.cacheManager();

    assertThat(result).isInstanceOf(NoOpCacheManager.class);
  }

  @Test
  @DisplayName("cacheManager() returns no caches when caching is disabled")
  void cacheManager_whenCachingDisabled_returnsManagerWithNoCaches() {
    given(properties.isEnabled()).willReturn(false);

    CacheManager result = cacheConfig.cacheManager();

    assertThat(result.getCacheNames()).isEmpty();
  }

  // -------------------------------------------------------------------------
  // cacheManager() — caching enabled
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("cacheManager() returns SimpleCacheManager when caching is enabled")
  void cacheManager_whenCachingEnabled_returnsSimpleCacheManager() {
    CacheManager result = buildEnabledCacheManager(10L, 100L);

    assertThat(result).isInstanceOf(SimpleCacheManager.class);
  }

  @Test
  @DisplayName("cacheManager() registers the 'teams' cache when caching is enabled")
  void cacheManager_whenCachingEnabled_registersTeamsCache() {
    CacheManager result = buildEnabledCacheManager(10L, 100L);

    assertThat(result.getCacheNames()).containsExactly("teams");
  }

  @Test
  @DisplayName("cacheManager() returns a CaffeineCache for the 'teams' entry")
  void cacheManager_whenCachingEnabled_teamsCacheIsCaffeineCache() {
    CacheManager result = buildEnabledCacheManager(10L, 100L);

    assertThat(result.getCache("teams")).isInstanceOf(CaffeineCache.class);
  }

  @Test
  @DisplayName("cacheManager() registers exactly one cache when caching is enabled")
  void cacheManager_whenCachingEnabled_registersExactlyOneCache() {
    CacheManager result = buildEnabledCacheManager(10L, 100L);

    assertThat(result.getCacheNames()).hasSize(1);
  }

  // -------------------------------------------------------------------------
  // CaffeineCache — Caffeine policy verification
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("teams cache has statistics recording enabled")
  void teamsCache_hasStatsRecordingEnabled() {
    CacheManager manager = buildEnabledCacheManager(10L, 100L);
    CaffeineCache caffeineCache = (CaffeineCache) manager.getCache("teams");
    Assertions.assertNotNull(caffeineCache);
    com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache =
        caffeineCache.getNativeCache();

    nativeCache.get("any-key", k -> "value");

    assertThat(nativeCache.stats().requestCount()).isPositive();
  }

  @Test
  @DisplayName("teams cache enforces the configured maximum size")
  void teamsCache_enforcesConfiguredMaxSize() {
    long maxSize = 50L;
    CacheManager manager = buildEnabledCacheManager(5L, maxSize);
    CaffeineCache caffeineCache = (CaffeineCache) manager.getCache("teams");
    Assertions.assertNotNull(caffeineCache);
    com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache =
        caffeineCache.getNativeCache();

    for (int i = 0; i < maxSize * 2; i++) {
      nativeCache.put("key-" + i, "value-" + i);
    }
    nativeCache.cleanUp();

    assertThat(nativeCache.estimatedSize()).isLessThanOrEqualTo(maxSize);
  }

  @Test
  @DisplayName("teams cache does not return null for the 'teams' key")
  void teamsCache_isNotNull_forTeamsKey() {
    CacheManager result = buildEnabledCacheManager(10L, 100L);

    assertThat(result.getCache("teams")).isNotNull();
  }

  @Test
  @DisplayName("cacheManager() returns null for an unknown cache name")
  void cacheManager_returnsNull_forUnknownCacheName() {
    CacheManager result = buildEnabledCacheManager(10L, 100L);

    assertThat(result.getCache("unknown-cache")).isNull();
  }
}
