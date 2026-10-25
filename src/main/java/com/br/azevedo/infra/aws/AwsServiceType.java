package com.br.azevedo.infra.aws;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Enum com os tipos de serviços AWS suportados e suas siglas / aliases conhecidos.
 */
public enum AwsServiceType {

    S3("s3", "S3 Storage", List.of("s3", "s3client", "storage")),
    S3_PRESIGNER("s3presigner", "S3 URL Presigner", List.of("s3presigner", "s3-presigner", "s3_presigner", "presigner")),
    SQS("sqs", "Simple Queue Service", List.of("sqs", "sqsasync", "sqs-async", "queue")),
    SNS("sns", "Simple Notification Service", List.of("sns", "snsasync", "sns-async", "notification")),
    SSM("ssm", "Systems Manager Parameter Store", List.of("ssm", "parameter-store", "parameterstore")),
    SECRETS_MANAGER("secretsmanager", "Secrets Manager", List.of("secretsmanager", "secrets-manager", "secrets_manager", "sm")),
    DYNAMODB("dynamodb", "DynamoDB NoSQL Database", List.of("dynamodb", "dynamo", "ddb"));

    private final String code;
    private final String description;
    private final List<String> aliases;

    AwsServiceType(String code, String description, List<String> aliases) {
        this.code = code;
        this.description = description;
        this.aliases = aliases;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public boolean matches(String input) {
        if (input == null || input.isBlank()) {
            return false;
        }
        String normalizedInput = normalize(input);
        if (normalize(code).equalsIgnoreCase(normalizedInput)) {
            return true;
        }
        return aliases.stream().anyMatch(alias -> normalize(alias).equalsIgnoreCase(normalizedInput));
    }

    public static Optional<AwsServiceType> fromSigla(String input) {
        if (input == null || input.isBlank()) {
            return Optional.empty();
        }
        String normalizedInput = normalize(input);
        return Arrays.stream(values())
                .filter(service -> service.matches(normalizedInput))
                .findFirst();
    }

    private static String normalize(String str) {
        return str.trim().toLowerCase().replaceAll("[_\\-\\s]", "");
    }
}
