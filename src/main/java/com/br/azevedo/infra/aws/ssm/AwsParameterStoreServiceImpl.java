package com.br.azevedo.infra.aws.ssm;

import com.br.azevedo.exception.ApplicationException;
import com.br.azevedo.exception.NotFoundException;
import com.br.azevedo.infra.cache.redis.repository.ICacheRepository;
import com.br.azevedo.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.DeleteParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathRequest;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathResponse;
import software.amazon.awssdk.services.ssm.model.Parameter;
import software.amazon.awssdk.services.ssm.model.ParameterNotFoundException;
import software.amazon.awssdk.services.ssm.model.ParameterType;
import software.amazon.awssdk.services.ssm.model.PutParameterRequest;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Implementação padrão de operações com o AWS Systems Manager (SSM) Parameter Store
 * com suporte a cache centralizado (Redis ou Ehcache) e TTL padrão de 24 horas.
 */
@Slf4j
public class AwsParameterStoreServiceImpl implements AwsParameterStoreService {

    private final SsmClient ssmClient;
    private final ICacheRepository cacheRepository;

    public static final String KEY_CACHE_SSM_DEFAULT = "ssm_parameter_by_name";
    private static final Duration DEFAULT_CACHE_TTL = Duration.ofHours(24);

    public AwsParameterStoreServiceImpl(SsmClient ssmClient) {
        this(ssmClient, null);
    }

    @Autowired
    public AwsParameterStoreServiceImpl(SsmClient ssmClient, @Autowired(required = false) ICacheRepository cacheRepository) {
        this.ssmClient = ssmClient;
        this.cacheRepository = cacheRepository;
    }

    @Override
    public String getParameter(String name) {
        return getParameter(name, true, DEFAULT_CACHE_TTL);
    }

    @Override
    public String getParameter(String name, boolean withDecryption) {
        return getParameter(name, withDecryption, DEFAULT_CACHE_TTL);
    }

    @Override
    public String getParameter(String name, Duration ttl) {
        return getParameter(name, true, ttl);
    }

    @Override
    public String getParameter(String name, boolean withDecryption, Duration ttl) {
        Duration effectiveTtl = ttl != null ? ttl : DEFAULT_CACHE_TTL;

        // 1. Cache first
        String cached = getFromCache(name);
        if (StringUtils.isNotBlank(cached)) {
            log.debug("Cache HIT para parâmetro SSM: [{}]", name);
            return cached;
        }

        log.debug("Cache MISS para parâmetro SSM: [{}], buscando no SSM, withDecryption={}", name, withDecryption);
        try {
            GetParameterRequest request = GetParameterRequest.builder()
                    .name(name)
                    .withDecryption(withDecryption)
                    .build();

            GetParameterResponse response = ssmClient.getParameter(request);
            if (response.parameter() != null && response.parameter().value() != null) {
                String value = response.parameter().value();
                cacheIfValid(name, value, effectiveTtl);
                return value;
            }
            throw new NotFoundException(String.format("Parâmetro '%s' não encontrado no Parameter Store", name));
        } catch (ParameterNotFoundException e) {
            log.error("Parâmetro não encontrado no SSM: [{}]", name);
            throw new NotFoundException(String.format("Parâmetro '%s' não encontrado no Parameter Store", name));
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao obter parâmetro do SSM: [{}] - {}", name, e.getMessage());
            throw new ApplicationException(String.format("Erro ao obter parâmetro '%s' do Parameter Store: %s", name, e.getMessage()), e);
        }
    }

    @Override
    public Optional<String> findParameter(String name) {
        return findParameter(name, true, DEFAULT_CACHE_TTL);
    }

    @Override
    public Optional<String> findParameter(String name, boolean withDecryption) {
        return findParameter(name, withDecryption, DEFAULT_CACHE_TTL);
    }

    @Override
    public Optional<String> findParameter(String name, Duration ttl) {
        return findParameter(name, true, ttl);
    }

    @Override
    public Optional<String> findParameter(String name, boolean withDecryption, Duration ttl) {
        try {
            return Optional.of(getParameter(name, withDecryption, ttl));
        } catch (NotFoundException e) {
            return Optional.empty();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getParameterAsMap(String name) {
        return getParameterAsMap(name, true, DEFAULT_CACHE_TTL);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getParameterAsMap(String name, boolean withDecryption) {
        return getParameterAsMap(name, withDecryption, DEFAULT_CACHE_TTL);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getParameterAsMap(String name, Duration ttl) {
        return getParameterAsMap(name, true, ttl);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getParameterAsMap(String name, boolean withDecryption, Duration ttl) {
        String value = getParameter(name, withDecryption, ttl);
        if (value == null || value.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return JsonUtils.jsonToObject(value, Map.class);
        } catch (Exception e) {
            log.warn("Conteúdo do parâmetro [{}] não é um JSON válido. Retornando map com chave 'value'.", name);
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("value", value);
            return fallback;
        }
    }

    @Override
    public void putParameter(String name, String value, ParameterType type, boolean overwrite) {
        log.info("Salvando parâmetro no SSM Parameter Store: [{}], type={}, overwrite={}", name, type, overwrite);
        try {
            PutParameterRequest request = PutParameterRequest.builder()
                    .name(name)
                    .value(value)
                    .type(type)
                    .overwrite(overwrite)
                    .build();

            ssmClient.putParameter(request);
            evictCache(name);
            log.info("Parâmetro [{}] salvo com sucesso no SSM.", name);
        } catch (Exception e) {
            log.error("Erro ao salvar parâmetro no SSM: [{}] - {}", name, e.getMessage());
            throw new ApplicationException(String.format("Erro ao salvar parâmetro '%s' no Parameter Store: %s", name, e.getMessage()), e);
        }
    }

    @Override
    public void putStringParameter(String name, String value) {
        putParameter(name, value, ParameterType.STRING, true);
    }

    @Override
    public void putSecureStringParameter(String name, String value) {
        putParameter(name, value, ParameterType.SECURE_STRING, true);
    }

    @Override
    public void deleteParameter(String name) {
        log.info("Removendo parâmetro no SSM Parameter Store: [{}]", name);
        try {
            DeleteParameterRequest request = DeleteParameterRequest.builder()
                    .name(name)
                    .build();

            ssmClient.deleteParameter(request);
            evictCache(name);
            log.info("Parâmetro [{}] removido com sucesso do SSM.", name);
        } catch (ParameterNotFoundException e) {
            evictCache(name);
            log.warn("Parâmetro não encontrado para exclusão no SSM: [{}]", name);
        } catch (Exception e) {
            log.error("Erro ao deletar parâmetro no SSM: [{}] - {}", name, e.getMessage());
            throw new ApplicationException(String.format("Erro ao deletar parâmetro '%s' no Parameter Store: %s", name, e.getMessage()), e);
        }
    }

    @Override
    public Map<String, String> getParametersByPath(String path, boolean recursive, boolean withDecryption) {
        log.debug("Buscando parâmetros por path no SSM: [{}], recursive={}, withDecryption={}", path, recursive, withDecryption);
        Map<String, String> parametersMap = new HashMap<>();
        String nextToken = null;

        try {
            do {
                GetParametersByPathRequest request = GetParametersByPathRequest.builder()
                        .path(path)
                        .recursive(recursive)
                        .withDecryption(withDecryption)
                        .nextToken(nextToken)
                        .build();

                GetParametersByPathResponse response = ssmClient.getParametersByPath(request);
                for (Parameter parameter : response.parameters()) {
                    parametersMap.put(parameter.name(), parameter.value());
                }
                nextToken = response.nextToken();
            } while (nextToken != null && !nextToken.isBlank());

            return parametersMap;
        } catch (Exception e) {
            log.error("Erro ao buscar parâmetros por path no SSM: [{}] - {}", path, e.getMessage());
            throw new ApplicationException(String.format("Erro ao buscar parâmetros por path '%s' no Parameter Store: %s", path, e.getMessage()), e);
        }
    }

    private String getFromCache(String name) {
        if (cacheRepository == null) return null;
        try {
            return cacheRepository.getCacheByNameAndKey(KEY_CACHE_SSM_DEFAULT, name, String.class);
        } catch (Exception e) {
            log.warn("Erro ao buscar parâmetro [{}] no cache: {}", name, e.getMessage());
            return null;
        }
    }

    private void cacheIfValid(String name, String value, Duration ttl) {
        if (cacheRepository == null || StringUtils.isBlank(value)) return;
        try {
            cacheRepository.saveCacheByNameAndKey(KEY_CACHE_SSM_DEFAULT, name, value, ttl);
        } catch (Exception e) {
            log.warn("Erro ao salvar parâmetro [{}] no cache: {}", name, e.getMessage());
        }
    }

    private void evictCache(String name) {
        if (cacheRepository == null) return;
        try {
            cacheRepository.removeCacheByNameAndKey(KEY_CACHE_SSM_DEFAULT, name);
        } catch (Exception e) {
            log.warn("Erro ao remover parâmetro [{}] do cache: {}", name, e.getMessage());
        }
    }
}
