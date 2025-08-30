package com.br.azevedo.infra.cache.redis.repository.impl;

import com.br.azevedo.infra.cache.redis.repository.ICacheRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Repository;

import static com.br.azevedo.utils.JsonUtils.objectMapper;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CacheRepositoryImpl implements ICacheRepository {
    public static final String NÃO_FOI_LOCALIZADO_NENHUM_CACHE_COM_A_CHAVE = "Não foi localizado nenhum cache com a chave {}";
    private final CacheManager cacheManager;

    @Override
    public void removeCacheByNameAndKey(String cacheName, Object key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            log.warn(NÃO_FOI_LOCALIZADO_NENHUM_CACHE_COM_A_CHAVE, key);
            return;
        }
        cache.evictIfPresent(key);
    }

    @Override
    public <T> T getCacheByNameAndKey(String cacheName, Object key, Class<T> returnType) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            return cache.get(key, returnType);
        }
        return null;
    }

    @Override
    public <T> T getCacheByNameAndKey(String cacheName, Object key, TypeReference<T> returnType) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            Object value = cache.get(key, Object.class); // Recupera o valor bruto do cache
            if (value != null) {
                try {
                    // Converte o valor bruto para JSON e desserializa para o tipo esperado
                    return objectMapper().readValue(value.toString(), returnType);
                } catch (Exception e) {
                    throw new RuntimeException("Erro ao desserializar objeto do cache", e);
                }
            }
        }
        return null;
    }

    @Override
    public void saveCacheByNameAndKey(String cacheName, Object key, Object value) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.put(key, value);
        }
    }

    @Override
    public void clearCacheByName(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }
}
