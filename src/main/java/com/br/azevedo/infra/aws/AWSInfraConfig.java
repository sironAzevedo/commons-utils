package com.br.azevedo.infra.aws;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import com.br.azevedo.infra.aws.s3.AwsS3StorageService;
import com.br.azevedo.infra.aws.s3.AwsS3StorageServiceImpl;
import com.br.azevedo.infra.aws.ssm.AwsParameterStoreService;
import com.br.azevedo.infra.aws.ssm.AwsParameterStoreServiceImpl;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClientBuilder;
import software.amazon.awssdk.services.sns.SnsAsyncClient;
import software.amazon.awssdk.services.sns.SnsAsyncClientBuilder;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.SnsClientBuilder;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.SqsAsyncClientBuilder;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.SqsClientBuilder;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.SsmClientBuilder;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Configuração centralizadora de infraestrutura e clients da AWS.
 * Instancia sob demanda os clients dos serviços solicitados na propriedade 'cloud.aws.services'.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class AWSInfraConfig {

    private final AwsProperties awsProperties;
    private final AwsCredentialsProvider awsCredentialsProvider;
    private final Environment environment;

    @PostConstruct
    public void validateAndLogConfiguredServices() {
        List<String> configuredSiglas = AwsServiceDetector.extractConfiguredServices(environment);
        if (configuredSiglas.isEmpty()) {
            log.warn("Nenhum serviço AWS informado na propriedade 'cloud.aws.services'. Nenhum client AWS será inicializado automaticamente.");
            return;
        }

        Set<AwsServiceType> enabledServices = new HashSet<>();
        for (String sigla : configuredSiglas) {
            Optional<AwsServiceType> serviceType = AwsServiceType.fromSigla(sigla);
            if (serviceType.isPresent()) {
                enabledServices.add(serviceType.get());
                log.info("Serviço AWS [{}] ({}) habilitado com sucesso a partir da sigla '{}'.",
                        serviceType.get().getCode().toUpperCase(),
                        serviceType.get().getDescription(),
                        sigla);
            } else {
                log.warn("Serviço AWS [{}] solicitado via configuração NÃO está configurado ou não é suportado pelo azevedo-commons-utils.", sigla);
            }
        }

        log.info("Total de serviços AWS ativos: {}", enabledServices.size());
    }

    private Region getRegion() {
        return Region.of(awsProperties.getResolvedRegion());
    }

    private URI getEndpointOverride(AwsServiceType serviceType) {
        String endpoint = awsProperties.getResolvedEndpoint(serviceType);
        if (endpoint != null && !endpoint.isBlank()) {
            try {
                return URI.create(endpoint.trim());
            } catch (Exception ex) {
                log.error("Erro ao converter endpoint '{}' para URI: {}", endpoint, ex.getMessage());
            }
        }
        return null;
    }

    // ==========================================
    // S3
    // ==========================================
    @Bean
    @ConditionalOnAwsService(AwsServiceType.S3)
    @ConditionalOnMissingBean(S3Client.class)
    public S3Client s3Client() {
        log.info("Configurando o S3Client para a região [{}]", getRegion());
        S3ClientBuilder builder = S3Client.builder()
                .region(getRegion())
                .credentialsProvider(awsCredentialsProvider);

        URI endpoint = getEndpointOverride(AwsServiceType.S3);
        if (endpoint != null) {
            log.info("Aplicando endpointOverride no S3Client: {}", endpoint);
            builder.endpointOverride(endpoint);
        }

        boolean pathStyle = awsProperties.getS3() != null &&
                Boolean.TRUE.equals(awsProperties.getS3().getPathStyleAccessEnabled());
        if (pathStyle) {
            builder.serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build());
        }

        return builder.build();
    }

    // ==========================================
    // S3 Presigner
    // ==========================================
    @Bean
    @ConditionalOnAwsService(AwsServiceType.S3_PRESIGNER)
    @ConditionalOnMissingBean(S3Presigner.class)
    public S3Presigner presigner() {
        log.info("Configurando o S3Presigner para a região [{}]", getRegion());
        S3Presigner.Builder builder = S3Presigner.builder()
                .region(getRegion())
                .credentialsProvider(awsCredentialsProvider);

        URI endpoint = getEndpointOverride(AwsServiceType.S3_PRESIGNER);
        if (endpoint != null) {
            log.info("Aplicando endpointOverride no S3Presigner: {}", endpoint);
            builder.endpointOverride(endpoint);
        }

        boolean pathStyle = awsProperties.getS3() != null &&
                Boolean.TRUE.equals(awsProperties.getS3().getPathStyleAccessEnabled());
        if (pathStyle) {
            builder.serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build());
        }

        return builder.build();
    }

    // ==========================================
    // SQS
    // ==========================================
    @Bean
    @ConditionalOnAwsService(AwsServiceType.SQS)
    @ConditionalOnMissingBean(SqsAsyncClient.class)
    public SqsAsyncClient sqsAsyncClient() {
        log.info("Configurando o SqsAsyncClient para a região [{}]", getRegion());
        SqsAsyncClientBuilder builder = SqsAsyncClient.builder()
                .region(getRegion())
                .credentialsProvider(awsCredentialsProvider);

        URI endpoint = getEndpointOverride(AwsServiceType.SQS);
        if (endpoint != null) {
            log.info("Aplicando endpointOverride no SqsAsyncClient: {}", endpoint);
            builder.endpointOverride(endpoint);
        }

        return builder.build();
    }

    @Bean
    @ConditionalOnAwsService(AwsServiceType.SQS)
    @ConditionalOnMissingBean(SqsClient.class)
    public SqsClient sqsClient() {
        log.info("Configurando o SqsClient síncrono para a região [{}]", getRegion());
        SqsClientBuilder builder = SqsClient.builder()
                .region(getRegion())
                .credentialsProvider(awsCredentialsProvider);

        URI endpoint = getEndpointOverride(AwsServiceType.SQS);
        if (endpoint != null) {
            log.info("Aplicando endpointOverride no SqsClient: {}", endpoint);
            builder.endpointOverride(endpoint);
        }

        return builder.build();
    }

    // ==========================================
    // SNS
    // ==========================================
    @Bean
    @ConditionalOnAwsService(AwsServiceType.SNS)
    @ConditionalOnMissingBean(SnsClient.class)
    public SnsClient snsClient() {
        log.info("Configurando o SnsClient para a região [{}]", getRegion());
        SnsClientBuilder builder = SnsClient.builder()
                .region(getRegion())
                .credentialsProvider(awsCredentialsProvider);

        URI endpoint = getEndpointOverride(AwsServiceType.SNS);
        if (endpoint != null) {
            log.info("Aplicando endpointOverride no SnsClient: {}", endpoint);
            builder.endpointOverride(endpoint);
        }

        return builder.build();
    }

    @Bean
    @ConditionalOnAwsService(AwsServiceType.SNS)
    @ConditionalOnMissingBean(SnsAsyncClient.class)
    public SnsAsyncClient snsAsyncClient() {
        log.info("Configurando o SnsAsyncClient para a região [{}]", getRegion());
        SnsAsyncClientBuilder builder = SnsAsyncClient.builder()
                .region(getRegion())
                .credentialsProvider(awsCredentialsProvider);

        URI endpoint = getEndpointOverride(AwsServiceType.SNS);
        if (endpoint != null) {
            log.info("Aplicando endpointOverride no SnsAsyncClient: {}", endpoint);
            builder.endpointOverride(endpoint);
        }

        return builder.build();
    }

    // ==========================================
    // SSM (Parameter Store)
    // ==========================================
    @Bean
    @ConditionalOnAwsService(AwsServiceType.SSM)
    @ConditionalOnMissingBean(SsmClient.class)
    public SsmClient ssmClient() {
        log.info("Configurando o SsmClient para a região [{}]", getRegion());
        SsmClientBuilder builder = SsmClient.builder()
                .region(getRegion())
                .credentialsProvider(awsCredentialsProvider);

        URI endpoint = getEndpointOverride(AwsServiceType.SSM);
        if (endpoint != null) {
            log.info("Aplicando endpointOverride no SsmClient: {}", endpoint);
            builder.endpointOverride(endpoint);
        }

        return builder.build();
    }

    // ==========================================
    // Secrets Manager
    // ==========================================
    @Bean
    @ConditionalOnAwsService(AwsServiceType.SECRETS_MANAGER)
    @ConditionalOnMissingBean(SecretsManagerClient.class)
    public SecretsManagerClient secretsManagerClient() {
        log.info("Configurando o SecretsManagerClient para a região [{}]", getRegion());
        SecretsManagerClientBuilder builder = SecretsManagerClient.builder()
                .region(getRegion())
                .credentialsProvider(awsCredentialsProvider);

        URI endpoint = getEndpointOverride(AwsServiceType.SECRETS_MANAGER);
        if (endpoint != null) {
            log.info("Aplicando endpointOverride no SecretsManagerClient: {}", endpoint);
            builder.endpointOverride(endpoint);
        }

        return builder.build();
    }

    // ==========================================
    // DynamoDB
    // ==========================================
    @Bean
    @ConditionalOnAwsService(AwsServiceType.DYNAMODB)
    @ConditionalOnMissingBean(DynamoDbClient.class)
    public DynamoDbClient dynamoDbClient() {
        log.info("Configurando o DynamoDbClient para a região [{}]", getRegion());
        DynamoDbClientBuilder builder = DynamoDbClient.builder()
                .region(getRegion())
                .credentialsProvider(awsCredentialsProvider);

        URI endpoint = getEndpointOverride(AwsServiceType.DYNAMODB);
        if (endpoint != null) {
            log.info("Aplicando endpointOverride no DynamoDbClient: {}", endpoint);
            builder.endpointOverride(endpoint);
        }

        return builder.build();
    }

    // ==========================================
    // Centralized Services (S3 & Parameter Store)
    // ==========================================
    @Bean
    @ConditionalOnAwsService(AwsServiceType.S3)
    @ConditionalOnMissingBean(AwsS3StorageService.class)
    public AwsS3StorageService awsS3StorageService(S3Client s3Client, org.springframework.beans.factory.ObjectProvider<S3Presigner> presignerProvider) {
        log.info("Configurando o serviço centralizado AwsS3StorageService");
        S3Presigner presigner = presignerProvider.getIfAvailable();
        if (presigner == null) {
            S3Presigner.Builder builder = S3Presigner.builder()
                    .region(getRegion())
                    .credentialsProvider(awsCredentialsProvider);
            URI endpoint = getEndpointOverride(AwsServiceType.S3_PRESIGNER);
            if (endpoint != null) {
                builder.endpointOverride(endpoint);
            }
            presigner = builder.build();
        }
        return new AwsS3StorageServiceImpl(s3Client, presigner, awsProperties);
    }

    @Bean
    @ConditionalOnAwsService(AwsServiceType.SSM)
    @ConditionalOnMissingBean(AwsParameterStoreService.class)
    public AwsParameterStoreService awsParameterStoreService(
            SsmClient ssmClient,
            org.springframework.beans.factory.ObjectProvider<com.br.azevedo.infra.cache.redis.repository.ICacheRepository> cacheRepositoryProvider,
            org.springframework.core.env.Environment environment) {
        log.info("Configurando o serviço centralizado AwsParameterStoreService com cache seguro (chaves mascaradas e valores criptografados)");
        com.br.azevedo.infra.aws.ssm.SsmCacheCrypto crypto = new com.br.azevedo.infra.aws.ssm.SsmCacheCrypto(environment);
        String customCacheName = awsProperties != null && awsProperties.getSsm() != null && awsProperties.getSsm().getCache() != null
                ? awsProperties.getSsm().getCache().getCacheName()
                : null;
        return new AwsParameterStoreServiceImpl(ssmClient, cacheRepositoryProvider.getIfAvailable(), crypto, customCacheName);
    }

    /**
     * Método fábrica estático para criar um SsmClient sob demanda utilizando as configurações da aplicação.
     */
    public static SsmClient createSsmClient(AwsProperties properties, AwsCredentialsProvider credentialsProvider) {
        SsmClientBuilder builder = SsmClient.builder()
                .region(Region.of(properties.getResolvedRegion()))
                .credentialsProvider(credentialsProvider);

        String endpoint = properties.getResolvedEndpoint(AwsServiceType.SSM);
        if (endpoint != null && !endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint.trim()));
        }
        return builder.build();
    }

    /**
     * Método fábrica estático para criar um AwsParameterStoreService a partir de um SsmClient.
     */
    public static AwsParameterStoreService createParameterStoreService(SsmClient ssmClient) {
        return new AwsParameterStoreServiceImpl(ssmClient, null);
    }

    /**
     * Método fábrica estático para criar um AwsParameterStoreService com cache a partir de um SsmClient e ICacheRepository.
     */
    public static AwsParameterStoreService createParameterStoreService(SsmClient ssmClient, com.br.azevedo.infra.cache.redis.repository.ICacheRepository cacheRepository) {
        return new AwsParameterStoreServiceImpl(ssmClient, cacheRepository);
    }
}
