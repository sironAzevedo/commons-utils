package com.br.azevedo.infra.aws;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Map;

/**
 * Condição Spring que avalia se um serviço AWS específico está habilitado na aplicação.
 */
public class OnAwsServiceCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Map<String, Object> attributes = metadata.getAnnotationAttributes(ConditionalOnAwsService.class.getName());
        if (attributes == null || !attributes.containsKey("value")) {
            return false;
        }

        AwsServiceType targetService = (AwsServiceType) attributes.get("value");
        return AwsServiceDetector.isServiceEnabled(context.getEnvironment(), targetService);
    }
}
