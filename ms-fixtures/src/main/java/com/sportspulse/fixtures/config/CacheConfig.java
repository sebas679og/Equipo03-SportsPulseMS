package com.sportspulse.fixtures.config;

import static com.sportspulse.fixtures.config.constants.CacheConstants.FIXTURES_CACHE;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sportspulse.fixtures.config.properties.CacheProperties;
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
 * Configuration class for the application's caching layer.
 *
 * <p>This class enables Spring's annotation-driven cache management and configures a {@link
 * CacheManager} using Caffeine. If caching is disabled via properties, it defaults to a {@link
 * NoOpCacheManager} to bypass all caching logic.
 */
@Configuration
@EnableCaching
@RequiredArgsConstructor
public class CacheConfig {

  private final CacheProperties properties;

  /**
   * Configures the cache manager based on the application properties.
   *
   * @return a {@link SimpleCacheManager} with pre-configured Caffeine caches if enabled, otherwise
   *     a {@link NoOpCacheManager}.
   */
  @Bean
  public CacheManager cacheManager() {
    if (!properties.isEnabled()) {
      return new NoOpCacheManager();
    }

    SimpleCacheManager manager = new SimpleCacheManager();
    manager.setCaches(List.of(buildCache(FIXTURES_CACHE)));
    return manager;
  }

  /**
   * Builds an individual {@link CaffeineCache} instance with specific parameters.
   *
   * <p>Caches are configured with:
   *
   * <ul>
   *   <li><b>TTL:</b> Defined by {@code properties.getTtlMinutes()}
   *   <li><b>Max Size:</b> Defined by {@code properties.getMaxSize()}
   *   <li><b>Stats:</b> Enabled for monitoring cache performance
   * </ul>
   *
   * @param name The unique name of the cache to be created.
   * @return a configured {@link CaffeineCache} instance.
   */
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
