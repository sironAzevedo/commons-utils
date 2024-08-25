package com.br.azevedo.infra.cache;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CacheConfigurationProperties {
    private String cacheName;
    private Duration expiration;
    private Integer heapEntries;
}
