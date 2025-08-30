package com.br.azevedo.infra.cache.redis;

import com.br.azevedo.infra.cache.AbstractCacheConfig;
import com.br.azevedo.infra.cache.CacheConfigurationProperties;
import com.br.azevedo.infra.cache.CacheProperties;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.Delay;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.br.azevedo.utils.JsonUtils.objectMapperRedis;

@Slf4j
@Configuration(proxyBeanMethods = false)
public class RedisCacheFactory extends AbstractCacheConfig implements CachingConfigurer {

    @Bean
    @Primary
    public LettuceConnectionFactory redisConnectionFactory(CacheProperties cacheProperties) {
        RedisProperties redisConfig = cacheProperties.getRedis().getConfig();

        // Configuração do cliente Lettuce (reconexão, etc.)
        var clientResources = ClientResources.builder()
                .reconnectDelay(Delay.constant(Duration.ofSeconds(10)))
                .build();

        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .clientOptions(ClientOptions.builder()
                        .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                        .autoReconnect(true)
                        .build())
                .clientResources(clientResources)
                .build();

        // Configuração do servidor Redis (host, porta, senha)
        RedisStandaloneConfiguration standaloneConfiguration = new RedisStandaloneConfiguration(redisConfig.getHost(), redisConfig.getPort());
        if (StringUtils.isNotEmpty(redisConfig.getPassword())) {
            standaloneConfiguration.setPassword(RedisPassword.of(redisConfig.getPassword()));
        }
        if (ObjectUtils.isNotEmpty(redisConfig.getDatabase())) {
            standaloneConfiguration.setDatabase(redisConfig.getDatabase());
        }

        return new LettuceConnectionFactory(standaloneConfiguration, clientConfig);
    }

    @Lazy
    @Bean
    @ConditionalOnBean(LettuceConnectionFactory.class) // Garante que este bean só será criado se a conexão com o Redis estiver configurada.
    public RedisCacheManager cacheManagerRedis(
            LettuceConnectionFactory lettuceConnectionFactory,
            Map<String, List<CacheConfigurationProperties>> cachesConfig) {

        try {
            List<CacheConfigurationProperties> redisCaches = cachesConfig.getOrDefault("redis", new ArrayList<>());
            var cacheConfigurations = new HashMap<String, RedisCacheConfiguration>();
            redisCaches.forEach(cache -> cacheConfigurations.put(cache.getCacheName(), createCacheConfiguration(cache)));

            // Constrói o manager usando a factory injetada
            return RedisCacheManager.builder(lettuceConnectionFactory)
                    .withInitialCacheConfigurations(cacheConfigurations)
                    .disableCreateOnMissingCache()
                    .transactionAware()
                    .build();
        } catch (Exception e) {
            log.error("Erro ao configurar o cacheManagerRedis", e);
            return null; // Ou lançar exceção para impedir a inicialização
        }
    }

    private RedisCacheConfiguration createCacheConfiguration(CacheConfigurationProperties cache) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .entryTtl(ObjectUtils.defaultIfNull(cache.getExpiration(), Duration.ZERO))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer(objectMapperRedis())));
    }

    @Lazy
    @Override
    public CacheErrorHandler errorHandler() {
        log.info("Configurando cacheErrorHandler Personalizado para o cache distribuido");
        return new CustomCacheErrorHandler();
    }

    private void addCaches(Map<String, RedisCacheConfiguration> cacheConfigurations, List<CacheConfigurationProperties> cacheConfigurationProperties) {
       cacheConfigurationProperties.forEach(cache -> cacheConfigurations.put(cache.getCacheName(), this.cacheConfiguration(cache)));
    }

    private RedisCacheConfiguration cacheConfiguration(CacheConfigurationProperties cache) {
        // Usar o ObjectMapper configurado no GenericJackson2JsonRedisSerializer
        return RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .entryTtl(ObjectUtils.defaultIfNull(cache.getExpiration(), Duration.ZERO))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer(objectMapperRedis())));
    }
}
