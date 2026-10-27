package com.br.azevedo.infra.aws;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Propriedades de configuração para centralização de serviços da AWS.
 * Mapeia propriedades sob o prefixo 'cloud.aws'.
 */
@Data
@ConfigurationProperties(prefix = "cloud.aws")
public class AwsProperties {

    /**
     * Lista de serviços da AWS a serem habilitados por sigla/código.
     * Exemplo: s3, s3presigner, sqs, sns, ssm, secretsmanager, dynamodb
     */
    private List<String> services = new ArrayList<>();

    /**
     * Endpoint global padrão (útil para LocalStack ou testes locais).
     * Exemplo: http://localhost:4566
     */
    private String endpoint;

    /**
     * Região padrão da AWS (compatível com cloud.aws.region.static ou cloud.aws.region).
     */
    private RegionConfig region = new RegionConfig();

    /**
     * Credenciais da AWS (compatível com cloud.aws.credentials.access-key e secret-key).
     */
    private CredentialsConfig credentials = new CredentialsConfig();

    /**
     * Configurações específicas para o S3.
     */
    private S3Config s3 = new S3Config();

    /**
     * Configurações legadas de fila SQS (compatível com cloud.aws.queue.endpoint).
     */
    private QueueConfig queue = new QueueConfig();

    /**
     * Configurações específicas para SQS.
     */
    private ServiceEndpointConfig sqs = new ServiceEndpointConfig();

    /**
     * Configurações específicas para SNS.
     */
    private ServiceEndpointConfig sns = new ServiceEndpointConfig();

    /**
     * Configurações específicas para SSM (Parameter Store).
     */
    private SsmConfig ssm = new SsmConfig();

    /**
     * Configurações específicas para Secrets Manager.
     */
    private ServiceEndpointConfig secretsmanager = new ServiceEndpointConfig();

    /**
     * Configurações específicas para DynamoDB.
     */
    private ServiceEndpointConfig dynamodb = new ServiceEndpointConfig();

    /**
     * Retorna a região resolvida (default: us-east-1).
     */
    public String getResolvedRegion() {
        if (region != null && region.getStaticRegion() != null && !region.getStaticRegion().isBlank()) {
            return region.getStaticRegion().trim();
        }
        return "us-east-1";
    }

    /**
     * Retorna o endpoint específico para o serviço ou o endpoint global caso configurado.
     */
    public String getResolvedEndpoint(AwsServiceType serviceType) {
        String specificEndpoint = switch (serviceType) {
            case S3, S3_PRESIGNER -> s3 != null ? s3.getEndpoint() : null;
            case SQS -> {
                if (sqs != null && sqs.getEndpoint() != null && !sqs.getEndpoint().isBlank()) {
                    yield sqs.getEndpoint();
                }
                yield queue != null ? queue.getEndpoint() : null;
            }
            case SNS -> sns != null ? sns.getEndpoint() : null;
            case SSM -> ssm != null ? ssm.getEndpoint() : null;
            case SECRETS_MANAGER -> secretsmanager != null ? secretsmanager.getEndpoint() : null;
            case DYNAMODB -> dynamodb != null ? dynamodb.getEndpoint() : null;
        };

        if (specificEndpoint != null && !specificEndpoint.isBlank()) {
            return specificEndpoint.trim();
        }
        if (endpoint != null && !endpoint.isBlank()) {
            return endpoint.trim();
        }
        return null;
    }

    @Data
    public static class RegionConfig {
        /**
         * Mapeia cloud.aws.region.static
         */
        private String staticRegion = "us-east-1";

        public String getStatic() {
            return staticRegion;
        }

        public void setStatic(String staticRegion) {
            this.staticRegion = staticRegion;
        }
    }

    @Data
    public static class CredentialsConfig {
        private String accessKey;
        private String secretKey;
        private String sessionToken;

        public boolean hasStaticCredentials() {
            return accessKey != null && !accessKey.isBlank() &&
                   secretKey != null && !secretKey.isBlank();
        }
    }

    @Data
    public static class QueueConfig {
        private Boolean enabled = false;
        private String endpoint;
    }

    @Data
    public static class S3Config {
        private String endpoint;
        private String bucket;
        private Boolean pathStyleAccessEnabled = false;
    }

    @Data
    public static class ServiceEndpointConfig {
        private String endpoint;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class SsmConfig extends ServiceEndpointConfig {
        private CacheConfig cache = new CacheConfig();

        @Data
        public static class CacheConfig {
            private Boolean encryptionEnabled = true;
            private String secretKey;
            private String cacheName;
        }
    }
}
