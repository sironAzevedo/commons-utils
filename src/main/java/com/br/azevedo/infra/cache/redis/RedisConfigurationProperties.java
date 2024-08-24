package com.br.azevedo.infra.cache.redis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedisConfigurationProperties {
    private String cacheName;
    private Duration expiration;
}
