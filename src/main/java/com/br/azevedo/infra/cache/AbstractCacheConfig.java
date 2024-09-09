package com.br.azevedo.infra.cache;

import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.cache.annotation.Cacheable;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import static com.br.azevedo.utils.ConstantesUtils.PACKAGES;

public abstract class AbstractCacheConfig {

    protected Map<String, List<CacheConfigurationProperties>> getCaches(CacheProperties cacheProperties) {
        Map<String, List<CacheConfigurationProperties>> res = new HashMap<>();
        Set<String> methods = new Reflections(new ConfigurationBuilder()
                .forPackages(PACKAGES)
                .setScanners(new MethodAnnotationsScanner()))
                .getMethodsAnnotatedWith(Cacheable.class)
                .stream().map(method -> method.getAnnotation(Cacheable.class))
                .flatMap(cacheable -> Arrays.stream(cacheable.value()))
                .collect(Collectors.toSet());

        res.put("ehCache", getCachesTratados(cacheProperties, methods, "ehCache"));
        res.put("redis", getCachesTratados(cacheProperties, methods, "redis"));
        return res;
    }

    private List<CacheConfigurationProperties> getCachesTratados(CacheProperties cacheProperties, Set<String> allCacheNames, String typeCache) {

        // Lista de caches do EhCache
        List<CacheConfigurationProperties> ehCaches = cacheProperties.getEhCache().getCaches();
        Set<String> ehCacheNames = getEhCacheNames(ehCaches);

        // Lista de caches do Redis
        List<CacheConfigurationProperties> redisCaches = cacheProperties.getRedis().getCaches();

        // Remove caches duplicados (já existentes no EhCache)
        redisCaches.removeIf(cacheRepetido -> ehCacheNames.contains(cacheRepetido.getCacheName()));
        Set<String> redisCacheNames = getRedisCacheNames(redisCaches);

        // Identificar caches que não estão nem no EhCache nem no Redis
        Set<String> cachesFaltantes = getMissingCaches(allCacheNames, ehCacheNames, redisCacheNames);

        // Adicionar os caches faltantes ao cache apropriado
        addMissingCaches(cacheProperties, cachesFaltantes, ehCaches, redisCaches);

        return "ehCache".equals(typeCache) ? ehCaches : redisCaches;
    }

    private static Set<String> getEhCacheNames(List<CacheConfigurationProperties> ehCaches) {
        return ehCaches.stream()
                .map(CacheConfigurationProperties::getCacheName)
                .collect(Collectors.toSet());
    }

    private Set<String> getRedisCacheNames(List<CacheConfigurationProperties> redisCaches) {
        return redisCaches.stream()
                .map(CacheConfigurationProperties::getCacheName)
                .collect(Collectors.toSet());
    }

    private Set<String> getMissingCaches(Set<String> allCacheNames, Set<String> ehCacheNames, Set<String> redisCacheNames) {
        Set<String> missingCaches = new HashSet<>(allCacheNames);
        missingCaches.removeAll(ehCacheNames);
        missingCaches.removeAll(redisCacheNames);
        return missingCaches;
    }

    private void addMissingCaches(CacheProperties cacheProperties, Set<String> missingCaches,
                                  List<CacheConfigurationProperties> ehCaches,
                                  List<CacheConfigurationProperties> redisCaches) {
        missingCaches.forEach(cacheName -> {
            CacheConfigurationProperties cacheConfig = CacheConfigurationProperties.builder()
                    .cacheName(cacheName)
                    .heapEntries(20)
                    .expiration(Duration.ofDays(1L))
                    .build();

            if (cacheProperties.isEnabledRedis()) {
                redisCaches.add(cacheConfig);
            } else {
                ehCaches.add(cacheConfig);
            }
        });
    }
}
