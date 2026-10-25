package com.br.azevedo.infra.aws;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotação para registro condicional de beans de clientes AWS baseado na presença da sigla do serviço
 * na propriedade 'cloud.aws.services'.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnAwsServiceCondition.class)
public @interface ConditionalOnAwsService {

    /**
     * Tipo do serviço AWS necessário para registrar o bean.
     */
    AwsServiceType value();
}
