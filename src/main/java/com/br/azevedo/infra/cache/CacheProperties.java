package com.br.azevedo.infra.cache;

import com.br.azevedo.infra.cache.ehCache.EhCacheProperties;
import com.br.azevedo.infra.cache.redis.RedisProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties("cache")
public class CacheProperties {
    private RedisProperties redis;
    private EhCacheProperties ehCache;
    private List<CacheConfigurationProperties> caches = new ArrayList<>();
}
