package com.br.azevedo.infra.cache.redis.repository.impl;

import com.br.azevedo.infra.cache.redis.repository.ICacheRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

import static com.br.azevedo.utils.JsonUtils.objectMapper;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CacheRepositoryImpl implements ICacheRepository {
    public static final String NAO_FOI_LOCALIZADO_NENHUM_CACHE_COM_A_CHAVE = "Não foi localizado nenhum cache com a chave {}";
    private final CacheManager cacheManager;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void removeCacheByNameAndKey(String cacheName, Object key) {
        String fullKey = cacheName + "::" + key.toString();
        Cache cache = cacheManager.getCache(fullKey);
        if (cache == null) {
            log.warn(NAO_FOI_LOCALIZADO_NENHUM_CACHE_COM_A_CHAVE, key);
            return;
        }
        cache.evictIfPresent(fullKey);
    }

    @Override
    public <T> T getCacheByNameAndKey(String cacheName, Object key, Class<T> returnType) {
        String fullKey = cacheName + "::" + key.toString();
        Cache cache = cacheManager.getCache(fullKey);
        return (T)(cache != null ? cache.get(key, returnType) : null);
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
        saveCacheByNameAndKey(cacheName, key, value, null);
    }

    @Override
    public void saveCacheByNameAndKey(String cacheName, Object key, Object value, Duration ttl) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            log.warn("Cache {} não encontrado", cacheName);
            return;
        }
        String fullKey = cacheName + "::" + key.toString();
        if (ttl != null && ttl.toMillis() > 0) {
            // Para TTL personalizado, usar RedisTemplate diretamente
            redisTemplate.opsForValue().set(fullKey, value, ttl);
        } else {
            // Usar o cache padrão (TTL do cache ou sem expiração se ttl for zero/negativo)
            redisTemplate.opsForValue().set(fullKey, value);
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
