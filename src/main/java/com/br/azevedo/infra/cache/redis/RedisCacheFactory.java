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
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration(proxyBeanMethods = false)
public class RedisCacheFactory extends AbstractCacheConfig implements CachingConfigurer {

    public LettuceConnectionFactory redisConnectionFactory(CacheProperties redisProperties) {
        var clientResources = ClientResources.builder()
                .reconnectDelay(Delay.constant(Duration.ofSeconds(10)))
                .build();

        // Set custom client options
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .clientOptions(ClientOptions.builder()
                        .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                        .autoReconnect(true)
                        .build())
                .clientResources(clientResources)
                .build();

        RedisStandaloneConfiguration standaloneConfiguration = new RedisStandaloneConfiguration(redisProperties.getRedis().getConfig().getHost(), redisProperties.getRedis().getConfig().getPort());
        if (StringUtils.isNotEmpty(redisProperties.getRedis().getConfig().getPassword())) {
            standaloneConfiguration.setPassword(RedisPassword.of(redisProperties.getRedis().getConfig().getPassword()));
        }

        LettuceConnectionFactory factory = new LettuceConnectionFactory(standaloneConfiguration, clientConfig);
        factory.setShareNativeConnection(false);
        return factory;
    }

    public RedisCacheManager cacheManagerRedis(List<CacheConfigurationProperties> cacheConfigurationProperties, CacheProperties redisProperties) {
        try {
            var cacheConfigurations = new HashMap<String, RedisCacheConfiguration>();
            addCaches(cacheConfigurations, cacheConfigurationProperties);

            LettuceConnectionFactory connectionFactory = this.redisConnectionFactory(redisProperties);
            connectionFactory.afterPropertiesSet();
            return RedisCacheManager.builder(connectionFactory)
                    .withInitialCacheConfigurations(cacheConfigurations)
                    .disableCreateOnMissingCache()
                    .transactionAware()
                    .build();
        } catch (Exception e) {
            log.error("Erro ao configurar o cacheManagerRedis", e);
        }
        return null;
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
        return RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .entryTtl(ObjectUtils.defaultIfNull(cache.getExpiration(), Duration.ZERO))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.json()));
    }
}
