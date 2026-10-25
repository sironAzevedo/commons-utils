package com.br.azevedo.infra.aws;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.sns.SnsAsyncClient;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.ssm.SsmClient;

import static org.assertj.core.api.Assertions.assertThat;

class EnableAwsIntegrationTest {

    @Configuration
    @EnableAws
    static class TestAppConfig {
    }

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestAppConfig.class);

    @Test
    @DisplayName("Deve registrar apenas os beans dos serviços especificados em cloud.aws.services (s3, sqs)")
    void shouldRegisterOnlyS3AndSqsBeans() {
        contextRunner
                .withPropertyValues(
                        "cloud.aws.region.static=sa-east-1",
                        "cloud.aws.credentials.access-key=dummy-key",
                        "cloud.aws.credentials.secret-key=dummy-secret",
                        "cloud.aws.services=s3, sqs",
                        "cloud.aws.endpoint=http://localhost:4566"
                )
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(AwsCredentialsProvider.class);

                    // S3 & SQS ativos
                    assertThat(context).hasSingleBean(S3Client.class);
                    assertThat(context).hasSingleBean(SqsAsyncClient.class);
                    assertThat(context).hasSingleBean(SqsClient.class);
                    assertThat(context).hasSingleBean(com.br.azevedo.infra.aws.s3.AwsS3StorageService.class);

                    // Outros serviços inativos
                    assertThat(context).doesNotHaveBean(S3Presigner.class);
                    assertThat(context).doesNotHaveBean(SnsClient.class);
                    assertThat(context).doesNotHaveBean(SnsAsyncClient.class);
                    assertThat(context).doesNotHaveBean(SsmClient.class);
                    assertThat(context).doesNotHaveBean(com.br.azevedo.infra.aws.ssm.AwsParameterStoreService.class);
                    assertThat(context).doesNotHaveBean(SecretsManagerClient.class);
                    assertThat(context).doesNotHaveBean(DynamoDbClient.class);
                });
    }

    @Test
    @DisplayName("Deve registrar presigner, sns, ssm, secretsmanager e dynamodb quando solicitados")
    void shouldRegisterOtherServicesWhenRequested() {
        contextRunner
                .withPropertyValues(
                        "cloud.aws.region.static=us-east-1",
                        "cloud.aws.credentials.access-key=dummy-key",
                        "cloud.aws.credentials.secret-key=dummy-secret",
                        "cloud.aws.services=s3presigner, sns, ssm, secretsmanager, dynamodb",
                        "cloud.aws.endpoint=http://localhost:4566"
                )
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(S3Presigner.class);
                    assertThat(context).hasSingleBean(SnsClient.class);
                    assertThat(context).hasSingleBean(SnsAsyncClient.class);
                    assertThat(context).hasSingleBean(SsmClient.class);
                    assertThat(context).hasSingleBean(com.br.azevedo.infra.aws.ssm.AwsParameterStoreService.class);
                    assertThat(context).hasSingleBean(SecretsManagerClient.class);
                    assertThat(context).hasSingleBean(DynamoDbClient.class);

                    assertThat(context).doesNotHaveBean(S3Client.class);
                    assertThat(context).doesNotHaveBean(com.br.azevedo.infra.aws.s3.AwsS3StorageService.class);
                    assertThat(context).doesNotHaveBean(SqsAsyncClient.class);
                });
    }

    @Test
    @DisplayName("Não deve registrar nenhum client se cloud.aws.services estiver vazio ou não configurado")
    void shouldNotRegisterAnyClientWhenServicesEmpty() {
        contextRunner
                .withPropertyValues(
                        "cloud.aws.region.static=us-east-1",
                        "cloud.aws.credentials.access-key=dummy-key",
                        "cloud.aws.credentials.secret-key=dummy-secret"
                )
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(AwsCredentialsProvider.class);
                    assertThat(context).doesNotHaveBean(S3Client.class);
                    assertThat(context).doesNotHaveBean(S3Presigner.class);
                    assertThat(context).doesNotHaveBean(SqsAsyncClient.class);
                    assertThat(context).doesNotHaveBean(SnsClient.class);
                    assertThat(context).doesNotHaveBean(SsmClient.class);
                    assertThat(context).doesNotHaveBean(SecretsManagerClient.class);
                    assertThat(context).doesNotHaveBean(DynamoDbClient.class);
                });
    }

    @Test
    @DisplayName("Deve aceitar sigla desconhecida sem falhar o contexto e registrar as válidas")
    void shouldHandleUnknownSiglaGracefully() {
        contextRunner
                .withPropertyValues(
                        "cloud.aws.region.static=us-east-1",
                        "cloud.aws.credentials.access-key=dummy-key",
                        "cloud.aws.credentials.secret-key=dummy-secret",
                        "cloud.aws.services=s3, servico-desconhecido, kinesis"
                )
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(S3Client.class);
                    assertThat(context).doesNotHaveBean(SqsAsyncClient.class);
                });
    }
}
