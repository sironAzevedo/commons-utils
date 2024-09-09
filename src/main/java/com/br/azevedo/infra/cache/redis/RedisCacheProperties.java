package com.br.azevedo.infra.cache.redis;

import com.br.azevedo.infra.cache.CacheConfigurationProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RedisCacheProperties {
    private Boolean disabled = Boolean.FALSE;
    private RedisProperties config = new RedisProperties();
    private List<CacheConfigurationProperties> caches = new ArrayList<>();
}
