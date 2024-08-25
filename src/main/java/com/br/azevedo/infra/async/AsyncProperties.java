package com.br.azevedo.infra.async;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("async")
public class AsyncProperties {
    private Integer corePoolSize;
    private Integer queueCapacity;
    private Integer maxPoolSize;
    private String threadNamePrefix;
}
