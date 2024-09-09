package com.br.azevedo.infra.cache.redis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RedisProperties {
    private String host;
    private Integer port;
    private Integer database;
    private String password;
}
