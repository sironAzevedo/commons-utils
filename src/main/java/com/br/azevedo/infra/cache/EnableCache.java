package com.br.azevedo.infra.cache;

import com.br.azevedo.infra.cache.ehCache.EhCacheFactory;
import com.br.azevedo.infra.cache.redis.RedisCacheFactory;
import com.br.azevedo.infra.cache.redis.RedisProperties;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({MultipleCacheConfig.class, RedisCacheFactory.class, EhCacheFactory.class, CacheProperties.class, RedisProperties.class})
public @interface EnableCache {
}
