package com.br.azevedo.infra.aws;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

/**
 * Fábrica de credenciais da AWS.
 * Centraliza a resolução de credenciais para as aplicações clientes.
 * Se access-key e secret-key forem configurados em 'cloud.aws.credentials',
 * utiliza StaticCredentialsProvider; caso contrário, utiliza DefaultCredentialsProvider
 * (suporta variáveis de ambiente, IAM Roles em ECS/EKS/EC2, etc.).
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class AWSCredentialFactory {

    private final AwsProperties awsProperties;

    @Bean
    @ConditionalOnMissingBean(AwsCredentialsProvider.class)
    public AwsCredentialsProvider awsCredentialsProvider() {
        return getCredentialsProvider();
    }

    /**
     * Retorna o provedor de credenciais configurado.
     */
    public AwsCredentialsProvider getCredentialsProvider() {
        AwsProperties.CredentialsConfig creds = awsProperties.getCredentials();

        if (creds != null && creds.hasStaticCredentials()) {
            log.info("AWS Credentials: Utilizando credenciais estáticas fornecidas via application properties (accessKey mascarada: {}***)",
                    maskKey(creds.getAccessKey()));

            if (creds.getSessionToken() != null && !creds.getSessionToken().isBlank()) {
                return StaticCredentialsProvider.create(
                        AwsSessionCredentials.create(
                                creds.getAccessKey().trim(),
                                creds.getSecretKey().trim(),
                                creds.getSessionToken().trim()
                        )
                );
            }

            return StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(
                            creds.getAccessKey().trim(),
                            creds.getSecretKey().trim()
                    )
            );
        }

        log.info("AWS Credentials: Nenhuma credencial estática explícita configurada. Utilizando DefaultCredentialsProvider da AWS.");
        return DefaultCredentialsProvider.create();
    }

    private String maskKey(String key) {
        if (key == null || key.length() < 4) {
            return "****";
        }
        return key.substring(0, Math.min(4, key.length()));
    }
}
