package com.br.azevedo.infra.aws;

import com.br.azevedo.exception.NotFoundException;
import com.br.azevedo.infra.aws.ssm.AwsParameterStoreServiceImpl;
import com.br.azevedo.infra.cache.redis.repository.ICacheRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.DeleteParameterRequest;
import software.amazon.awssdk.services.ssm.model.DeleteParameterResponse;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import software.amazon.awssdk.services.ssm.model.Parameter;
import software.amazon.awssdk.services.ssm.model.ParameterNotFoundException;
import software.amazon.awssdk.services.ssm.model.ParameterType;
import software.amazon.awssdk.services.ssm.model.PutParameterRequest;
import software.amazon.awssdk.services.ssm.model.PutParameterResponse;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AwsParameterStoreServiceTest {

    @Mock
    private SsmClient ssmClient;

    @Mock
    private ICacheRepository cacheRepository;

    private AwsParameterStoreServiceImpl parameterStoreService;
    private AwsParameterStoreServiceImpl parameterStoreServiceWithCache;

    @BeforeEach
    void setUp() {
        parameterStoreService = new AwsParameterStoreServiceImpl(ssmClient);
        parameterStoreServiceWithCache = new AwsParameterStoreServiceImpl(ssmClient, cacheRepository);
    }

    @Test
    @DisplayName("Deve buscar parâmetro com sucesso sem cache")
    void shouldGetParameterSuccessfullyWithoutCache() {
        GetParameterResponse response = GetParameterResponse.builder()
                .parameter(Parameter.builder()
                        .name("/auth-api/jwt/credentials-dev")
                        .value("super-secret-token")
                        .type(ParameterType.SECURE_STRING)
                        .build())
                .build();

        when(ssmClient.getParameter(any(GetParameterRequest.class))).thenReturn(response);

        String result = parameterStoreService.getParameter("/auth-api/jwt/credentials-dev");
        assertEquals("super-secret-token", result);
    }

    @Test
    @DisplayName("Deve retornar do cache quando houver Cache HIT com valor criptografado e chave mascarada")
    void shouldReturnFromCacheOnHit() {
        String paramName = "/app/data/security/cripto-dev";
        String maskedKey = parameterStoreServiceWithCache.getSsmCacheCrypto().maskKey(paramName);
        String encryptedValue = parameterStoreServiceWithCache.getSsmCacheCrypto().encrypt("master-key-cached");

        when(cacheRepository.getCacheByNameAndKey("ssm_parameter_by_name", maskedKey, String.class))
                .thenReturn(encryptedValue);

        String result = parameterStoreServiceWithCache.getParameter(paramName);

        assertEquals("master-key-cached", result);
        verify(ssmClient, never()).getParameter(any(GetParameterRequest.class));
    }

    @Test
    @DisplayName("Deve manter retrocompatibilidade com dados legados em texto claro no cache")
    void shouldSupportLegacyPlainTextInCache() {
        String paramName = "/app/data/security/legacy-key";
        String maskedKey = parameterStoreServiceWithCache.getSsmCacheCrypto().maskKey(paramName);

        when(cacheRepository.getCacheByNameAndKey("ssm_parameter_by_name", maskedKey, String.class))
                .thenReturn(null);
        when(cacheRepository.getCacheByNameAndKey("ssm_parameter_by_name", paramName, String.class))
                .thenReturn("legacy-unencrypted-secret");

        String result = parameterStoreServiceWithCache.getParameter(paramName);

        assertEquals("legacy-unencrypted-secret", result);
        verify(ssmClient, never()).getParameter(any(GetParameterRequest.class));
    }

    @Test
    @DisplayName("Deve buscar no SSM e salvar no cache com chave mascarada e valor criptografado com TTL padrão")
    void shouldFetchFromSsmAndSaveToCacheWithDefaultTtlOnMiss() {
        String paramName = "/auth-api/jwt/credentials-dev";
        String maskedKey = parameterStoreServiceWithCache.getSsmCacheCrypto().maskKey(paramName);

        GetParameterResponse response = GetParameterResponse.builder()
                .parameter(Parameter.builder()
                        .name(paramName)
                        .value("token-from-ssm")
                        .type(ParameterType.SECURE_STRING)
                        .build())
                .build();

        when(cacheRepository.getCacheByNameAndKey("ssm_parameter_by_name", maskedKey, String.class))
                .thenReturn(null);
        when(ssmClient.getParameter(any(GetParameterRequest.class))).thenReturn(response);

        String result = parameterStoreServiceWithCache.getParameter(paramName);

        assertEquals("token-from-ssm", result);
        verify(cacheRepository).saveCacheByNameAndKey(
                eq("ssm_parameter_by_name"),
                eq(maskedKey),
                org.mockito.ArgumentMatchers.argThat(val -> val != null && val.toString().startsWith("ENC:GCM:")),
                eq(Duration.ofHours(24))
        );
    }

    @Test
    @DisplayName("Deve salvar no cache com TTL customizado de 7 dias com chave mascarada e valor criptografado")
    void shouldSaveToCacheWithCustomTtl() {
        String paramName = "/app/data/security/cripto-prod";
        String maskedKey = parameterStoreServiceWithCache.getSsmCacheCrypto().maskKey(paramName);

        GetParameterResponse response = GetParameterResponse.builder()
                .parameter(Parameter.builder()
                        .name(paramName)
                        .value("custom-ttl-master-key")
                        .type(ParameterType.SECURE_STRING)
                        .build())
                .build();

        when(cacheRepository.getCacheByNameAndKey("ssm_parameter_by_name", maskedKey, String.class))
                .thenReturn(null);
        when(ssmClient.getParameter(any(GetParameterRequest.class))).thenReturn(response);

        String result = parameterStoreServiceWithCache.getParameter(paramName, Duration.ofDays(7));

        assertEquals("custom-ttl-master-key", result);
        verify(cacheRepository).saveCacheByNameAndKey(
                eq("ssm_parameter_by_name"),
                eq(maskedKey),
                org.mockito.ArgumentMatchers.argThat(val -> val != null && val.toString().startsWith("ENC:GCM:")),
                eq(Duration.ofDays(7))
        );
    }

    @Test
    @DisplayName("Deve lançar NotFoundException quando parâmetro não existir")
    void shouldThrowNotFoundExceptionWhenParameterDoesNotExist() {
        when(ssmClient.getParameter(any(GetParameterRequest.class)))
                .thenThrow(ParameterNotFoundException.builder().message("Parameter not found").build());

        assertThrows(NotFoundException.class, () ->
                parameterStoreService.getParameter("/inexistente"));
    }

    @Test
    @DisplayName("Deve retornar Optional no findParameter")
    void shouldReturnOptionalOnFindParameter() {
        when(ssmClient.getParameter(any(GetParameterRequest.class)))
                .thenThrow(ParameterNotFoundException.builder().message("Not found").build());

        Optional<String> result = parameterStoreService.findParameter("/inexistente");
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Deve fazer parse de parâmetro JSON para Map")
    void shouldParseJsonParameterToMap() {
        GetParameterResponse response = GetParameterResponse.builder()
                .parameter(Parameter.builder()
                        .name("/auth-api/jwt/credentials-dev")
                        .value("{\"API_SECRET\":\"my-api-secret\",\"role\":\"ADMIN\"}")
                        .build())
                .build();

        when(ssmClient.getParameter(any(GetParameterRequest.class))).thenReturn(response);

        Map<String, Object> map = parameterStoreService.getParameterAsMap("/auth-api/jwt/credentials-dev");
        assertNotNull(map);
        assertEquals("my-api-secret", map.get("API_SECRET"));
        assertEquals("ADMIN", map.get("role"));
    }

    @Test
    @DisplayName("Deve salvar e invalidar cache")
    void shouldPutAndEvictCache() {
        String paramName = "/test/secret";
        String maskedKey = parameterStoreServiceWithCache.getSsmCacheCrypto().maskKey(paramName);

        when(ssmClient.putParameter(any(PutParameterRequest.class)))
                .thenReturn(PutParameterResponse.builder().build());

        parameterStoreServiceWithCache.putSecureStringParameter(paramName, "secret-val");

        verify(ssmClient).putParameter(any(PutParameterRequest.class));
        verify(cacheRepository).removeCacheByNameAndKey("ssm_parameter_by_name", maskedKey);
    }

    @Test
    @DisplayName("Deve deletar e invalidar cache")
    void shouldDeleteAndEvictCache() {
        String paramName = "/test/secret";
        String maskedKey = parameterStoreServiceWithCache.getSsmCacheCrypto().maskKey(paramName);

        when(ssmClient.deleteParameter(any(DeleteParameterRequest.class)))
                .thenReturn(DeleteParameterResponse.builder().build());

        parameterStoreServiceWithCache.deleteParameter(paramName);

        verify(ssmClient).deleteParameter(any(DeleteParameterRequest.class));
        verify(cacheRepository).removeCacheByNameAndKey("ssm_parameter_by_name", maskedKey);
    }
}
