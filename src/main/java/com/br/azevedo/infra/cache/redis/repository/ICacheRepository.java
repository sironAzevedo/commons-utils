package com.br.azevedo.infra.cache.redis.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import java.time.Duration;

public interface ICacheRepository {

    /**
     * Remove dados do cache com base no nome do cache e na chave fornecida.
     *
     * @param cacheName o nome do cache
     * @param key a chave do item a ser removido
     */
    void removeCacheByNameAndKey(String cacheName, Object key);

    /**
     * Recupera um item do cache com base no nome do cache e na chave fornecida.
     *
     * @param cacheName o nome do cache
     * @param key a chave do item a ser recuperado
     * @param <T> o tipo do objeto esperado
     * @return o objeto recuperado do cache ou null se não existir
     */
    <T> T getCacheByNameAndKey(String cacheName, Object key, Class<T> returnType);

    /**
     * Recupera um item do cache com base no nome do cache e na chave fornecida.
     *
     * @param cacheName o nome do cache
     * @param key a chave do item a ser recuperado
     * @param <T> o tipo do objeto esperado
     * @return o objeto recuperado do cache ou null se não existir
     */
    <T> T getCacheByNameAndKey(String cacheName, Object key, TypeReference<T> returnType);

    /**
     * Salva um item no cache com base no nome do cache, na chave e no valor fornecidos.
     *
     * @param cacheName o nome do cache
     * @param key a chave do item
     * @param value o valor a ser armazenado no cache
     */
    void saveCacheByNameAndKey(String cacheName, Object key, Object value);

    /**
     * Salva um item no cache com base no nome do cache, na chave e no valor fornecidos, com TTL especificado.
     *
     * @param cacheName o nome do cache
     * @param key a chave do item
     * @param value o valor a ser armazenado no cache
     * @param ttl o tempo de vida do item no cache, se null usa o TTL padrão do cache
     */
    void saveCacheByNameAndKey(String cacheName, Object key, Object value, Duration ttl);

    /**
     * Limpa todos os dados de um cache com base no nome fornecido.
     *
     * @param cacheName o nome do cache a ser limpo
     */
    void clearCacheByName(String cacheName);
}
