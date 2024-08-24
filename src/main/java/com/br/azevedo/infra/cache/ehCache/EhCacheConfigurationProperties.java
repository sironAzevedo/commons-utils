package com.br.azevedo.infra.cache.ehCache;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EhCacheConfigurationProperties {
    private String cacheName;
    private Duration expiration;
    private Integer heapEntries;
}
