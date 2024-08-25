package com.br.azevedo.infra.cache.ehCache;

import com.br.azevedo.infra.cache.CacheConfigurationProperties;
import com.br.azevedo.infra.cache.CacheProperties;
import lombok.extern.slf4j.Slf4j;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.jsr107.Eh107Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import java.util.Objects;

@Slf4j
@Configuration
@ConditionalOnBean(CompositeCacheManager.class)
@ConditionalOnProperty(
        value = "cache.ehCache.enabled",
        havingValue = "true"
)
public class EhCacheFactory {

    @Bean("EhCache")
    public CacheManager ehCacheManager(CacheProperties cacheProperties) {
        log.info("INICIO - Configuracao do cache local com EhCache");
        CachingProvider cachingProvider = Caching.getCachingProvider();
        javax.cache.CacheManager cacheManager = cachingProvider.getCacheManager();

        addNewCaches(cacheProperties, cacheManager);

        log.info("FIM - Configuracao do cache local com EhCache");
        return new JCacheCacheManager(cacheManager);
    }

    private void addNewCaches(CacheProperties cacheProperties, javax.cache.CacheManager cacheManager) {
        cacheProperties.getCaches().forEach(cache -> {
            if (this.isNewCache(cacheManager, cache)) {
                javax.cache.configuration.Configuration<Object, Object> ehCache3Config = this.createCacheConfiguration(cache);
                cacheManager.createCache(cache.getCacheName(), ehCache3Config);
            }
        });
    }

    private boolean isNewCache(javax.cache.CacheManager cacheManagerFromEhCache, CacheConfigurationProperties cache) {
        return cacheManagerFromEhCache.getCache(cache.getCacheName()) == null;
    }

    private javax.cache.configuration.Configuration<Object, Object> createCacheConfiguration(CacheConfigurationProperties cacheConfiguration) {
        long entries = Objects.nonNull(cacheConfiguration.getHeapEntries()) ? cacheConfiguration.getHeapEntries().longValue() : Long.valueOf(20);
        return Eh107Configuration
                .fromEhcacheCacheConfiguration(
                        CacheConfigurationBuilder
                                .newCacheConfigurationBuilder(Object.class, Object.class, ResourcePoolsBuilder.heap(entries))
                                .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(cacheConfiguration.getExpiration())));
    }
}
