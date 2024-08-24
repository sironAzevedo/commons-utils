package com.br.azevedo.infra.cache.ehCache;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EhCacheProperties {
    private List<EhCacheConfigurationProperties> caches = new ArrayList<>();
}
