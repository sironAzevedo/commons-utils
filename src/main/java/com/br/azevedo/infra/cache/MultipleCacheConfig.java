package com.br.azevedo.infra.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
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

    private final CacheProperties cacheProperties;

    public MultipleCacheConfig(CacheProperties cacheProperties) {
        this.cacheProperties = cacheProperties;
    }

    @Lazy
    @Primary
    @Bean(name = "compositeCacheManager")
    public CompositeCacheManager getCompositeCacheManager(
            @Autowired(required = false) List<CacheManager> existingCacheManagers) {
        log.info("MultipleCache: Start");

        CompositeCacheManager compositeCacheManager = new CompositeCacheManager();

        // ✅ CORRIGIDO: O Spring injetará TODOS os CacheManagers (Redis e EhCache)
        // na lista 'existingCacheManagers'. Não é mais necessário criar manualmente.
        if (!CollectionUtils.isEmpty(existingCacheManagers)) {
            compositeCacheManager.setCacheManagers(existingCacheManagers);
            Set<CacheManager> collect = new HashSet<>(existingCacheManagers);
            String enabledCaches = collect.stream()
                    .map(CacheManager::getCacheNames)
                    .filter(cacheNames -> !cacheNames.isEmpty())
                    .flatMap(Collection::stream)                          // Achata as listas em uma única stream
                    .distinct()                                           // Garante nomes únicos
                    .collect(Collectors.joining(", "));                   // Une em uma única string

            if (!enabledCaches.isEmpty()) {
                log.info("Cache Managers habilitados: {}", enabledCaches);
            }
        }

        log.info("MultipleCache: End");
        return compositeCacheManager;
    }

    @Bean
    public Map<String, List<CacheConfigurationProperties>> cachesConfig() {
        return getCaches(this.cacheProperties);
    }
}
