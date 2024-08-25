package com.br.azevedo.infra.cache.redis;

import com.br.azevedo.infra.cache.CacheConfigurationProperties;
import com.br.azevedo.infra.cache.CacheProperties;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.Delay;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
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
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@ConditionalOnBean(
        name = {"CompositeCacheManager"}
)
@ConditionalOnProperty(
        value = {"cache.redis.enabled"},
        havingValue = "true"
)
public class RedisCacheFactory implements CachingConfigurer {

    @Bean
    public LettuceConnectionFactory redisConnectionFactory(
            @Value("${cache.redis.config.host}") String host,
            @Value("${cache.redis.config.port}") Integer port,
            @Value("${cache.redis.config.database}") Integer database,
            @Value("${cache.redis.config.password}") String password
    ) {

        log.info("Inicio - Configurando as conexões do cache distribudido");
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

        RedisStandaloneConfiguration standaloneConfiguration = new RedisStandaloneConfiguration(host, port);
        if (StringUtils.isNotEmpty(password)) {
            standaloneConfiguration.setPassword(RedisPassword.of(password));
        }

        LettuceConnectionFactory factory = new LettuceConnectionFactory(standaloneConfiguration, clientConfig);
        factory.setShareNativeConnection(false);
        factory.afterPropertiesSet();
        log.info("Fim - Configurando as conexões do cache distribudido");
        return factory;
    }

    @Bean
    public CacheManager cacheManagerRedis(LettuceConnectionFactory connectionFactory, CacheProperties cacheProperties) {
        log.info("INICIO - Configuracao do CacheManager para o cache distribuido");

        var cacheConfigurations = new HashMap<String, RedisCacheConfiguration>();
        addCacheDefault(cacheConfigurations, getCacheNames(cacheProperties.getCaches()));
        addNewCaches(cacheProperties, cacheConfigurations);

        var manager = RedisCacheManager.builder(connectionFactory)
                .disableCreateOnMissingCache()
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();

        log.info("FIM - Configuracao do CacheManager para o cache distribuido");
        return manager;
    }

    @Lazy
    @Bean
    @Override
    public CacheErrorHandler errorHandler() {
        log.info("Configurando cacheErrorHandler Personalizado para o cache distribuido");
        return new CustomCacheErrorHandler();
    }

    private void addNewCaches(CacheProperties cacheProperties, Map<String, RedisCacheConfiguration> cacheConfigurations) {
        cacheProperties.getCaches().forEach(cache -> cacheConfigurations.put(cache.getCacheName(), this.cacheConfiguration(cache)));
    }

    private void addCacheDefault(Map<String, RedisCacheConfiguration> cacheConfigurations, Set<String> cacheNames) {
        cacheNames.forEach(initialCache -> {
            CacheConfigurationProperties cache = CacheConfigurationProperties
                    .builder()
                    .cacheName(initialCache)
                    .expiration(Duration.ofDays(1L))
                    .build();

            cacheConfigurations.put(cache.getCacheName(), this.cacheConfiguration(cache));
        });
    }

    private RedisCacheConfiguration cacheConfiguration(CacheConfigurationProperties cache) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .entryTtl(ObjectUtils.defaultIfNull(cache.getExpiration(), Duration.ZERO))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.json()));
    }

    private Set<String> getCacheNames(List<CacheConfigurationProperties> cachesExistentes) {
        Set<String> methods = new Reflections(new ConfigurationBuilder()
                .forPackages("br.com", "com.br", "com.br.azevedo")
                .setScanners(new MethodAnnotationsScanner()))
                .getMethodsAnnotatedWith(Cacheable.class)
                .stream().map(method -> method.getAnnotation(Cacheable.class))
                .flatMap(cacheable -> Arrays.stream(cacheable.value()))
                .collect(Collectors.toSet());

        // Criar um HashSet diretamente para evitar duplicatas e otimizar o contains
        Set<String> existingCacheNames = cachesExistentes.stream()
                .map(CacheConfigurationProperties::getCacheName)
                .collect(Collectors.toSet());

        // Filtrar diretamente em um HashSet para remover duplicatas e não existentes
        return methods.stream()
                .filter(cacheName -> !existingCacheNames.contains(cacheName))
                .collect(Collectors.toSet());
    }
}
