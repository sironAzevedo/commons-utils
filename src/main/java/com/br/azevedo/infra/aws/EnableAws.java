package com.br.azevedo.infra.aws;

import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Habilita a integração centralizada com os serviços da AWS no commons-utils.
 *
 * <p>Exemplo de uso na classe principal da aplicação cliente:</p>
 * <pre>{@code
 * @EnableAws
 * @SpringBootApplication
 * public class Application { ... }
 * }</pre>
 *
 * <p>Exemplo de configuração no application.yml da aplicação cliente:</p>
 * <pre>{@code
 * cloud:
 *   aws:
 *     services: s3, s3presigner, sqs, sns, ssm, secretsmanager, dynamodb
 *     region:
 *       static: us-east-1
 *     credentials:
 *       access-key: ${AWS_ACCESS_KEY:}
 *       secret-key: ${AWS_SECRET_KEY:}
 *     endpoint: ${HOST_AWS_LOCAL:http://localhost:4566} # opcional para LocalStack
 * }</pre>
 *
 * Caso a aplicação cliente informe uma sigla de serviço não suportada ou não configurada,
 * será emitido um log de aviso (WARN) informando que o serviço não está configurado.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({
        AwsProperties.class,
        AWSCredentialFactory.class,
        AWSInfraConfig.class
})
public @interface EnableAws {
}
