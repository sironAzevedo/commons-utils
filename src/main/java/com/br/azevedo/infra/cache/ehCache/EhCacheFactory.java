package com.br.azevedo.infra.cache.ehCache;

import com.br.azevedo.infra.cache.CacheConfigurationProperties;
import com.br.azevedo.infra.cache.CacheProperties;
import lombok.extern.slf4j.Slf4j;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.jsr107.Eh107Configuration;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.br.azevedo.utils.ConstantesUtils.PACKAGES;

@Slf4j
@Configuration
@ConditionalOnBean(CompositeCacheManager.class)
@ConditionalOnExpression("'${cache.redis.enabled}'.equals('false')")
public class EhCacheFactory {

    @Bean("EhCache")
    public CacheManager ehCacheManager(CacheProperties cacheProperties) {
        log.info("INICIO - Configuracao do cache local com EhCache");
        CachingProvider cachingProvider = Caching.getCachingProvider();
        javax.cache.CacheManager cacheManager = cachingProvider.getCacheManager();

        addCacheDefault(cacheManager, cacheProperties.getCaches());
        addNewCaches(cacheProperties, cacheManager);

        log.info("FIM - Configuracao do cache local com EhCache");
        return new JCacheCacheManager(cacheManager);
    }

    private void addNewCaches(CacheProperties cacheProperties, javax.cache.CacheManager cacheManager) {
        cacheProperties.getCaches().forEach(cache -> {
            if (this.isNewCache(cacheManager, cache)) {
                cacheManager.createCache(cache.getCacheName(), this.createCacheConfiguration(cache));
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

    private void addCacheDefault(javax.cache.CacheManager cacheManager, List<CacheConfigurationProperties> cachesExistentes) {
        Set<String> methods = new Reflections(new ConfigurationBuilder()
                .forPackages(PACKAGES)
                .setScanners(new MethodAnnotationsScanner()))
                .getMethodsAnnotatedWith(Cacheable.class)
                .stream().map(method -> method.getAnnotation(Cacheable.class))
                .flatMap(cacheable -> Arrays.stream(cacheable.value()))
                .collect(Collectors.toSet());

        Set<String> existingCacheNames = cachesExistentes.stream()
                .map(CacheConfigurationProperties::getCacheName)
                .collect(Collectors.toSet());

        var newCache = methods.stream()
                .filter(cacheName -> !existingCacheNames.contains(cacheName))
                .collect(Collectors.toSet());


        newCache.forEach( c -> {
            CacheConfigurationProperties cache = CacheConfigurationProperties.builder()
                    .cacheName(c)
                    .heapEntries(20)
                    .expiration(Duration.ofDays(1L))
                    .build();

            if (this.isNewCache(cacheManager, cache)) {
                cacheManager.createCache(cache.getCacheName(), this.createCacheConfiguration(cache));
            }
        });
    }
}
