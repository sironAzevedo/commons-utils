package com.br.azevedo.infra.cache.redis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RedisProperties {
    private List<RedisConfigurationProperties> caches = new ArrayList<>();
}
