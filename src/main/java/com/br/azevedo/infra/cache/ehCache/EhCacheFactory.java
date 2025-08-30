package com.br.azevedo.infra.cache.ehCache;

import com.br.azevedo.infra.cache.CacheConfigurationProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.jsr107.Eh107Configuration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.util.CollectionUtils;

import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Configuration(proxyBeanMethods = false)
public class EhCacheFactory {

    @Lazy
    @Bean
    public CacheManager ehCacheManager(Map<String, List<CacheConfigurationProperties>> cachesConfig) {
        try {
            List<CacheConfigurationProperties> ehCacheProps = cachesConfig.get("ehCache");

            if (CollectionUtils.isEmpty(ehCacheProps)) {
                // Se não há caches EhCache configurados, retorna um manager que não faz nada.
                return new NoOpCacheManager();
            }

            CachingProvider cachingProvider = Caching.getCachingProvider();
            javax.cache.CacheManager cacheManager = cachingProvider.getCacheManager();
            addCaches(cacheManager, ehCacheProps);

            log.info("Cache local habilitados: [{}]", StringUtils.join(cacheManager.getCacheNames(), ", "));
            return new JCacheCacheManager(cacheManager);
        } catch (Exception e) {
            log.error("Erro ao configurar o EhCacheManager", e);
            return new NoOpCacheManager();
        }
    }

    private void addCaches(javax.cache.CacheManager cacheManager, List<CacheConfigurationProperties> cacheConfigurationProperties) {
        cacheConfigurationProperties.forEach(cache -> {
            if (this.isNewCache(cacheManager, cache)) {
                cacheManager.createCache(cache.getCacheName(), this.createCacheConfiguration(cache));
            }
        });
    }

    private boolean isNewCache(javax.cache.CacheManager cacheManagerFromEhCache, CacheConfigurationProperties cache) {
        return cacheManagerFromEhCache.getCache(cache.getCacheName()) == null;
    }

    private javax.cache.configuration.Configuration<Object, Object> createCacheConfiguration(CacheConfigurationProperties cacheConfiguration) {
        long entries = Objects.nonNull(cacheConfiguration.getHeapEntries()) ? cacheConfiguration.getHeapEntries().longValue() : 20L;
        return Eh107Configuration
                .fromEhcacheCacheConfiguration(
                        CacheConfigurationBuilder
                                .newCacheConfigurationBuilder(Object.class, Object.class, ResourcePoolsBuilder.heap(entries))
                                .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(cacheConfiguration.getExpiration())));
    }
}
