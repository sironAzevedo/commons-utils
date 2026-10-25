package com.br.azevedo.infra.aws;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AWSCredentialFactoryTest {

    @Test
    @DisplayName("Deve criar StaticCredentialsProvider quando accessKey e secretKey forem informados")
    void shouldCreateStaticCredentialsProviderWhenKeysProvided() {
        AwsProperties properties = new AwsProperties();
        properties.getCredentials().setAccessKey("test-access-key");
        properties.getCredentials().setSecretKey("test-secret-key");

        AWSCredentialFactory factory = new AWSCredentialFactory(properties);
        AwsCredentialsProvider provider = factory.getCredentialsProvider();

        assertNotNull(provider);
        assertInstanceOf(StaticCredentialsProvider.class, provider);

        AwsCredentials credentials = provider.resolveCredentials();
        assertInstanceOf(AwsBasicCredentials.class, credentials);
        assertEquals("test-access-key", credentials.accessKeyId());
        assertEquals("test-secret-key", credentials.secretAccessKey());
    }

    @Test
    @DisplayName("Deve criar AwsSessionCredentials quando sessionToken for fornecido")
    void shouldCreateSessionCredentialsWhenTokenProvided() {
        AwsProperties properties = new AwsProperties();
        properties.getCredentials().setAccessKey("test-access-key");
        properties.getCredentials().setSecretKey("test-secret-key");
        properties.getCredentials().setSessionToken("test-session-token");

        AWSCredentialFactory factory = new AWSCredentialFactory(properties);
        AwsCredentialsProvider provider = factory.getCredentialsProvider();

        assertNotNull(provider);
        assertInstanceOf(StaticCredentialsProvider.class, provider);

        AwsCredentials credentials = provider.resolveCredentials();
        assertInstanceOf(AwsSessionCredentials.class, credentials);
        assertEquals("test-session-token", ((AwsSessionCredentials) credentials).sessionToken());
    }

    @Test
    @DisplayName("Deve utilizar DefaultCredentialsProvider quando nenhuma chave for configurada")
    void shouldUseDefaultCredentialsProviderWhenNoKeys() {
        AwsProperties properties = new AwsProperties();

        AWSCredentialFactory factory = new AWSCredentialFactory(properties);
        AwsCredentialsProvider provider = factory.getCredentialsProvider();

        assertNotNull(provider);
        assertInstanceOf(DefaultCredentialsProvider.class, provider);
    }
}
