package com.br.azevedo.infra.aws;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AwsServiceDetectorTest {

    @Test
    @DisplayName("Deve extrair serviços de string separada por vírgula")
    void shouldExtractServicesFromCommaSeparatedString() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("cloud.aws.services", "s3, sqs, dynamodb");

        List<String> services = AwsServiceDetector.extractConfiguredServices(env);
        assertEquals(3, services.size());
        assertTrue(services.contains("s3"));
        assertTrue(services.contains("sqs"));
        assertTrue(services.contains("dynamodb"));

        assertTrue(AwsServiceDetector.isServiceEnabled(env, AwsServiceType.S3));
        assertTrue(AwsServiceDetector.isServiceEnabled(env, AwsServiceType.SQS));
        assertTrue(AwsServiceDetector.isServiceEnabled(env, AwsServiceType.DYNAMODB));
        assertFalse(AwsServiceDetector.isServiceEnabled(env, AwsServiceType.SNS));
    }

    @Test
    @DisplayName("Deve extrair serviços de lista indexada do Spring Boot")
    void shouldExtractServicesFromIndexedList() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("cloud.aws.services[0]", "s3presigner");
        env.setProperty("cloud.aws.services[1]", "sns");
        env.setProperty("cloud.aws.services[2]", "ssm");

        List<String> services = AwsServiceDetector.extractConfiguredServices(env);
        assertEquals(3, services.size());
        assertTrue(services.contains("s3presigner"));
        assertTrue(services.contains("sns"));
        assertTrue(services.contains("ssm"));

        assertTrue(AwsServiceDetector.isServiceEnabled(env, AwsServiceType.S3_PRESIGNER));
        assertTrue(AwsServiceDetector.isServiceEnabled(env, AwsServiceType.SNS));
        assertTrue(AwsServiceDetector.isServiceEnabled(env, AwsServiceType.SSM));
        assertFalse(AwsServiceDetector.isServiceEnabled(env, AwsServiceType.S3));
    }

    @Test
    @DisplayName("Deve respeitar propriedade booleana individual como fallback")
    void shouldRespectIndividualBooleanFallback() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("cloud.aws.secretsmanager.enabled", "true");

        assertTrue(AwsServiceDetector.isServiceEnabled(env, AwsServiceType.SECRETS_MANAGER));
        assertFalse(AwsServiceDetector.isServiceEnabled(env, AwsServiceType.SQS));
    }
}
