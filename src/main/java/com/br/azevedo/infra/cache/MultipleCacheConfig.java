package com.br.azevedo.infra.cache;

import com.br.azevedo.infra.cache.ehCache.EhCacheFactory;
import com.br.azevedo.infra.cache.redis.RedisCacheFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@EnableCaching
@ConditionalOnProperty(
        value = "cache.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Configuration(proxyBeanMethods = false)
public class MultipleCacheConfig extends AbstractCacheConfig {

    private final Map<String, List<CacheConfigurationProperties>> caches;
    private final CacheProperties cacheProperties;

    public MultipleCacheConfig(CacheProperties cacheProperties, CacheProperties cacheProperties1) {
        this.caches = getCaches(cacheProperties);
        this.cacheProperties = cacheProperties1;
    }


    @Primary
    @Bean(
            name = {"compositeCacheManager"}
    )
    public CompositeCacheManager getCompositeCacheManager(
            List<CacheManager> existingCacheManagers) {
        log.info("MultipleCache: Start");

        CompositeCacheManager compositeCacheManager = new CompositeCacheManager();
        List<CacheManager> cacheManagers = new ArrayList<>();
        List<CacheConfigurationProperties> ehCache = caches.get("ehCache");
        if (!CollectionUtils.isEmpty(ehCache)) {
            cacheManagers.add(new EhCacheFactory().ehCacheManager(ehCache));
            compositeCacheManager.setCacheManagers(cacheManagers);
        }

        if (!CollectionUtils.isEmpty(existingCacheManagers)) {
            compositeCacheManager.setCacheManagers(existingCacheManagers);
            Set<CacheManager> collect = new HashSet<>(existingCacheManagers);
            log.info("Cache distribuidos habilitados: {}", StringUtils.join(collect.stream().map(CacheManager::getCacheNames).collect(Collectors.toSet()), ", "));
        }

        log.info("MultipleCache: End");
        return compositeCacheManager;
    }

    @Bean
    public CacheManager cacheManagerRedis() {
        if (!cacheProperties.isEnabledRedis()) {
            log.warn("Cache distribuido desabilitado");
            return new NoOpCacheManager();
        }
        return new RedisCacheFactory().cacheManagerRedis(caches.get("redis"), this.cacheProperties);
    }
}
