package com.br.azevedo.infra.cache;

import com.br.azevedo.infra.cache.ehCache.EhCacheProperties;
import com.br.azevedo.infra.cache.redis.RedisCacheProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties("cache")
public class CacheProperties {
    private EhCacheProperties ehCache = new EhCacheProperties();
    private RedisCacheProperties redis = new RedisCacheProperties();

    public Boolean isEnabledRedis() {
        return StringUtils.isNotBlank(redis.getConfig().getHost()) && !redis.getDisabled();
    }
}
