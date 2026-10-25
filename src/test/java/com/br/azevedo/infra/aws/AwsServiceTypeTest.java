package com.br.azevedo.infra.aws;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AwsServiceTypeTest {

    @ParameterizedTest
    @ValueSource(strings = {"s3", "S3", "s3-client", "storage"})
    @DisplayName("Deve reconhecer o serviço S3 pelas siglas e aliases")
    void shouldRecognizeS3(String input) {
        Optional<AwsServiceType> service = AwsServiceType.fromSigla(input);
        assertTrue(service.isPresent());
        assertEquals(AwsServiceType.S3, service.get());
    }

    @ParameterizedTest
    @ValueSource(strings = {"s3presigner", "S3PRESIGNER", "s3-presigner", "s3_presigner", "presigner"})
    @DisplayName("Deve reconhecer o serviço S3Presigner pelas siglas e aliases")
    void shouldRecognizeS3Presigner(String input) {
        Optional<AwsServiceType> service = AwsServiceType.fromSigla(input);
        assertTrue(service.isPresent());
        assertEquals(AwsServiceType.S3_PRESIGNER, service.get());
    }

    @ParameterizedTest
    @ValueSource(strings = {"sqs", "SQS", "sqsasync", "sqs-async", "queue"})
    @DisplayName("Deve reconhecer o serviço SQS pelas siglas e aliases")
    void shouldRecognizeSqs(String input) {
        Optional<AwsServiceType> service = AwsServiceType.fromSigla(input);
        assertTrue(service.isPresent());
        assertEquals(AwsServiceType.SQS, service.get());
    }

    @ParameterizedTest
    @ValueSource(strings = {"sns", "SNS", "snsasync", "sns-async", "notification"})
    @DisplayName("Deve reconhecer o serviço SNS pelas siglas e aliases")
    void shouldRecognizeSns(String input) {
        Optional<AwsServiceType> service = AwsServiceType.fromSigla(input);
        assertTrue(service.isPresent());
        assertEquals(AwsServiceType.SNS, service.get());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ssm", "SSM", "parameter-store", "parameterstore"})
    @DisplayName("Deve reconhecer o serviço SSM pelas siglas e aliases")
    void shouldRecognizeSsm(String input) {
        Optional<AwsServiceType> service = AwsServiceType.fromSigla(input);
        assertTrue(service.isPresent());
        assertEquals(AwsServiceType.SSM, service.get());
    }

    @ParameterizedTest
    @ValueSource(strings = {"secretsmanager", "SECRETSMANAGER", "secrets-manager", "secrets_manager", "sm"})
    @DisplayName("Deve reconhecer o serviço Secrets Manager pelas siglas e aliases")
    void shouldRecognizeSecretsManager(String input) {
        Optional<AwsServiceType> service = AwsServiceType.fromSigla(input);
        assertTrue(service.isPresent());
        assertEquals(AwsServiceType.SECRETS_MANAGER, service.get());
    }

    @ParameterizedTest
    @ValueSource(strings = {"dynamodb", "DYNAMODB", "dynamo", "ddb"})
    @DisplayName("Deve reconhecer o serviço DynamoDB pelas siglas e aliases")
    void shouldRecognizeDynamoDb(String input) {
        Optional<AwsServiceType> service = AwsServiceType.fromSigla(input);
        assertTrue(service.isPresent());
        assertEquals(AwsServiceType.DYNAMODB, service.get());
    }

    @Test
    @DisplayName("Deve retornar empty para serviços não conhecidos ou vazios")
    void shouldReturnEmptyForUnknownServices() {
        assertTrue(AwsServiceType.fromSigla(null).isEmpty());
        assertTrue(AwsServiceType.fromSigla("").isEmpty());
        assertTrue(AwsServiceType.fromSigla("   ").isEmpty());
        assertTrue(AwsServiceType.fromSigla("kinesis").isEmpty());
        assertTrue(AwsServiceType.fromSigla("ses").isEmpty());
        assertTrue(AwsServiceType.fromSigla("lambda").isEmpty());
    }
}
