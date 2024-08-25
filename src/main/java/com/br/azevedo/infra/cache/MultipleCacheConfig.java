package com.br.azevedo.infra.cache;

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

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
@EnableCaching
@ConditionalOnProperty(
        value = {"cache.enabled"},
        havingValue = "true",
        matchIfMissing = true
)
public class MultipleCacheConfig {

    @Primary
    @Bean(
            name = {"CompositeCacheManager"}
    )
    public CacheManager getCompositeCacheManager(List<CacheManager> existingCacheManagers) {
        CompositeCacheManager compositeCacheManager = new CompositeCacheManager();
        List<CacheManager> cacheManagers = new ArrayList<>();
        if (!CollectionUtils.isEmpty(existingCacheManagers)) {
            log.info("MultipleCacheConfig.getCompositeCacheManager:Start");
            existingCacheManagers.forEach(cacheManager -> log.info("Caches habilitados: {}", StringUtils.join(cacheManager.getCacheNames(), ", ")));
            cacheManagers.addAll(existingCacheManagers);
            log.info("MultipleCacheConfig.getCompositeCacheManager:End");
        }

        cacheManagers.add(new NoOpCacheManager());
        compositeCacheManager.setCacheManagers(cacheManagers);
        return compositeCacheManager;
    }
}
