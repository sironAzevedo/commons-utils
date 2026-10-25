package com.br.azevedo.infra.aws;

import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Utilitário para detecção e extração de serviços AWS configurados no Environment do Spring.
 */
public final class AwsServiceDetector {

    public static final String SERVICES_PROPERTY = "cloud.aws.services";

    private AwsServiceDetector() {
    }

    /**
     * Verifica se o serviço especificado foi solicitado via propriedade 'cloud.aws.services'
     * ou via flag direta 'cloud.aws.<servico>.enabled'.
     */
    public static boolean isServiceEnabled(Environment env, AwsServiceType targetService) {
        if (env == null || targetService == null) {
            return false;
        }

        List<String> configuredServices = extractConfiguredServices(env);
        for (String sigla : configuredServices) {
            if (targetService.matches(sigla)) {
                return true;
            }
        }

        // Fallback: propriedade individual booleana cloud.aws.<servico>.enabled
        String explicitProperty = env.getProperty("cloud.aws." + targetService.getCode() + ".enabled");
        if (explicitProperty != null) {
            return Boolean.parseBoolean(explicitProperty.trim());
        }

        return false;
    }

    /**
     * Extrai todas as siglas informadas no Environment do Spring, suportando formatos:
     * - Comma-separated: cloud.aws.services: s3, sqs, sns
     * - Lista YAML indexada: cloud.aws.services[0]: s3, cloud.aws.services[1]: sqs
     */
    public static List<String> extractConfiguredServices(Environment env) {
        if (env == null) {
            return Collections.emptyList();
        }

        Set<String> result = new LinkedHashSet<>();

        // 1. String separada por vírgula
        String rawServices = env.getProperty(SERVICES_PROPERTY);
        if (rawServices != null && !rawServices.isBlank()) {
            String[] tokens = rawServices.split(",");
            for (String token : tokens) {
                if (!token.isBlank()) {
                    result.add(token.trim());
                }
            }
        }

        // 2. Propriedades indexadas pelo Spring Boot Binder: cloud.aws.services[0], ...
        int index = 0;
        while (true) {
            String item = env.getProperty(SERVICES_PROPERTY + "[" + index + "]");
            if (item == null) {
                break;
            }
            if (!item.isBlank()) {
                result.add(item.trim());
            }
            index++;
        }

        return new ArrayList<>(result);
    }
}
