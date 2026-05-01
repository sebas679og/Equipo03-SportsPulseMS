package com.sportspulse.leagues.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.sportspulse.leagues.config.properties.CacheProperties;
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

    @Mock
    private CacheProperties properties;

    @InjectMocks
    private CacheConfig cacheConfig;

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
    @DisplayName("cacheManager() registers both 'leaguesByFilters' and 'leaguesById' caches when caching is enabled")
    void cacheManager_whenCachingEnabled_registersBothLeagueCaches() {
        CacheManager result = buildEnabledCacheManager(10L, 100L);

        assertThat(result.getCacheNames())
                .containsExactlyInAnyOrder("leaguesByFilters", "leaguesById");
    }

    @Test
    @DisplayName("cacheManager() returns a CaffeineCache for 'leaguesByFilters'")
    void cacheManager_whenCachingEnabled_leaguesByFiltersCacheIsCaffeineCache() {
        CacheManager result = buildEnabledCacheManager(10L, 100L);

        assertThat(result.getCache("leaguesByFilters")).isInstanceOf(CaffeineCache.class);
    }

    @Test
    @DisplayName("cacheManager() returns a CaffeineCache for 'leaguesById'")
    void cacheManager_whenCachingEnabled_leaguesByIdCacheIsCaffeineCache() {
        CacheManager result = buildEnabledCacheManager(10L, 100L);

        assertThat(result.getCache("leaguesById")).isInstanceOf(CaffeineCache.class);
    }

    @Test
    @DisplayName("cacheManager() registers exactly two caches when caching is enabled")
    void cacheManager_whenCachingEnabled_registersExactlyTwoCaches() {
        CacheManager result = buildEnabledCacheManager(10L, 100L);

        assertThat(result.getCacheNames()).hasSize(2);
    }

    // -------------------------------------------------------------------------
    // CaffeineCache — Caffeine policy verification
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("leaguesByFilters cache has statistics recording enabled")
    void leaguesByFiltersCache_hasStatsRecordingEnabled() {
        CacheManager manager = buildEnabledCacheManager(10L, 100L);
        CaffeineCache caffeineCache = (CaffeineCache) manager.getCache("leaguesByFilters");
        Assertions.assertNotNull(caffeineCache);
        com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache =
                caffeineCache.getNativeCache();

        nativeCache.get("any-key", k -> "value");

        assertThat(nativeCache.stats().requestCount()).isPositive();
    }

    @Test
    @DisplayName("leaguesById cache has statistics recording enabled")
    void leaguesByIdCache_hasStatsRecordingEnabled() {
        CacheManager manager = buildEnabledCacheManager(10L, 100L);
        CaffeineCache caffeineCache = (CaffeineCache) manager.getCache("leaguesById");
        Assertions.assertNotNull(caffeineCache);
        com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache =
                caffeineCache.getNativeCache();

        nativeCache.get("any-key", k -> "value");

        assertThat(nativeCache.stats().requestCount()).isPositive();
    }

    @Test
    @DisplayName("leaguesByFilters cache enforces the configured maximum size")
    void leaguesByFiltersCache_enforcesConfiguredMaxSize() {
        long maxSize = 50L;
        CacheManager manager = buildEnabledCacheManager(5L, maxSize);
        CaffeineCache caffeineCache = (CaffeineCache) manager.getCache("leaguesByFilters");
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
    @DisplayName("leaguesById cache enforces the configured maximum size")
    void leaguesByIdCache_enforcesConfiguredMaxSize() {
        long maxSize = 50L;
        CacheManager manager = buildEnabledCacheManager(5L, maxSize);
        CaffeineCache caffeineCache = (CaffeineCache) manager.getCache("leaguesById");
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
    @DisplayName("leaguesByFilters cache does not return null")
    void leaguesByFiltersCache_isNotNull() {
        CacheManager result = buildEnabledCacheManager(10L, 100L);

        assertThat(result.getCache("leaguesByFilters")).isNotNull();
    }

    @Test
    @DisplayName("leaguesById cache does not return null")
    void leaguesByIdCache_isNotNull() {
        CacheManager result = buildEnabledCacheManager(10L, 100L);

        assertThat(result.getCache("leaguesById")).isNotNull();
    }

    @Test
    @DisplayName("cacheManager() returns null for an unknown cache name")
    void cacheManager_returnsNull_forUnknownCacheName() {
        CacheManager result = buildEnabledCacheManager(10L, 100L);

        assertThat(result.getCache("unknown-cache")).isNull();
    }
}

