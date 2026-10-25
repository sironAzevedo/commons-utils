# Centralização e Configuração de Serviços AWS (`@EnableAws`)

O módulo `@EnableAws` centraliza a conectividade e configuração dos clientes oficiais do AWS SDK v2 (`software.amazon.awssdk`) na biblioteca `commons-utils`.

Com isso, nenhuma aplicação cliente precisa reescrever classes de infraestrutura como `AWSCredentialFactory` ou `AWSInfraConfig`. Toda a habilitação é feita de forma declarativa via anotação e arquivos de configuração (`application.yml` ou `application.properties`).

---

## 1. Como Usar na Aplicação Cliente

### 1.1. Adicionar anotação na classe principal
Na classe principal (ou em uma classe de `@Configuration`) da sua aplicação, adicione a anotação `@EnableAws`:

```java
package br.com.fatura;

import com.br.azevedo.infra.aws.EnableAws;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableAws
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

---

## 2. Configuração no `application.yml`

Basta informar quais serviços deseja habilitar via sigla/código na propriedade `cloud.aws.services`, além das credenciais e região:

```yaml
cloud:
  aws:
    # Lista de siglas dos serviços que deseja ativar
    services: s3, s3presigner, sqs, sns, ssm, secretsmanager, dynamodb

    # Região da AWS (padrão: us-east-1)
    region:
      static: ${AWS_REGION:us-east-1}

    # Credenciais estáticas (opcional caso utilize IAM Role / DefaultCredentialsProvider)
    credentials:
      access-key: ${AWS_ACCESS_KEY:}
      secret-key: ${AWS_SECRET_KEY:}
      session-token: ${AWS_SESSION_TOKEN:} # Opcional (para credenciais temporárias STS)

    # Endpoint global para LocalStack ou mocks locais (opcional)
    endpoint: ${AWS_ENDPOINT_OVERRIDE:}

    # Configurações específicas por serviço (opcionais)
    s3:
      path-style-access-enabled: false
      endpoint: ${AWS_S3_ENDPOINT:}
    sqs:
      endpoint: ${HOST_QUEUE_SQS:}
```

Também é suportado o formato de lista YAML:
```yaml
cloud:
  aws:
    services:
      - s3
      - s3presigner
      - sqs
```

---

## 3. Serviços Suportados e Beans Criados

| Serviço AWS | Siglas e Aliases Aceitos | Beans Spring Criados |
| :--- | :--- | :--- |
| **S3** | `s3`, `storage`, `s3client` | `software.amazon.awssdk.services.s3.S3Client` |
| **S3 Presigner** | `s3presigner`, `s3-presigner`, `s3_presigner`, `presigner` | `software.amazon.awssdk.services.s3.presigner.S3Presigner` |
| **SQS** | `sqs`, `sqsasync`, `queue` | `software.amazon.awssdk.services.sqs.SqsAsyncClient`<br>`software.amazon.awssdk.services.sqs.SqsClient` |
| **SNS** | `sns`, `snsasync`, `notification` | `software.amazon.awssdk.services.sns.SnsClient`<br>`software.amazon.awssdk.services.sns.SnsAsyncClient` |
| **SSM (Parameter Store)** | `ssm`, `parameter-store`, `parameterstore` | `software.amazon.awssdk.services.ssm.SsmClient` |
| **Secrets Manager** | `secretsmanager`, `secrets-manager`, `sm` | `software.amazon.awssdk.services.secretsmanager.SecretsManagerClient` |
| **DynamoDB** | `dynamodb`, `dynamo`, `ddb` | `software.amazon.awssdk.services.dynamodb.DynamoDbClient` |

---


---

## 3.1. Cache Integrado no Parameter Store (SSM)

O serviço centralizado  () possui integração nativa com a infraestrutura de cache (), suportando tanto **Redis** quanto **Ehcache**.

### Características do Cache:
- **Cache-First**: Ao solicitar um parâmetro, a aplicação verifica primeiramente o cache (). Em caso de *Cache HIT*, o valor é retornado imediatamente sem realizar chamada de rede à AWS.
- **TTL Padrão de 24 Horas**: Quando o tempo de vida não é informado explicitamente, o parâmetro é gravado no cache com TTL padrão de **24 horas**.
- **TTL Customizado**: Todos os métodos de busca (, , ) possuem sobrecargas que aceitam um parâmetro  para definir tempos de expiração customizados (ex: 7 dias para chaves mestras).
- **Invalidação Automática**: Sempre que um parâmetro é gravado (, , ) ou removido (), a chave correspondente no cache é imediatamente invalidada.
- **Tolerância a Falhas**: Caso a aplicação cliente não tenha cache habilitado, o serviço funciona transparentemente realizando chamadas diretas ao SSM.

## 4. Tratamento de Siglas Não Reconhecidas

Caso a aplicação cliente solicite uma sigla que ainda não foi implementada ou não é suportada (ex: `kinesis`, `ses`), o sistema:
1. **Não aborta o startup** da aplicação;
2. Registra um log de aviso (`WARN`) no console informando que o serviço não está configurado:
   ```text
   WARN [AWSInfraConfig] : Serviço AWS [kinesis] solicitado via configuração NÃO está configurado ou não é suportado pelo azevedo-commons-utils.
   ```
3. Registra logs de confirmação (`INFO`) para todos os serviços válidos que foram devidamente ativados:
   ```text
   INFO [AWSInfraConfig] : Serviço AWS [S3] (S3 Storage) habilitado com sucesso a partir da sigla 's3'.
   INFO [AWSInfraConfig] : Serviço AWS [SQS] (Simple Queue Service) habilitado com sucesso a partir da sigla 'sqs'.
   INFO [AWSInfraConfig] : Total de serviços AWS ativos: 2
   ```

---

## 5. Resolução de Credenciais (`AWSCredentialFactory`)

- **Ambiente Local / Credenciais Estáticas**: Se `cloud.aws.credentials.access-key` e `cloud.aws.credentials.secret-key` forem informados, o sistema instancia um `StaticCredentialsProvider` usando `AwsBasicCredentials` ou `AwsSessionCredentials`.
- **Ambiente Cloud / Produção (IAM Role / EKS / ECS / EC2)**: Se nenhuma credencial estática for informada, o sistema faz fallback automático para o `DefaultCredentialsProvider` oficial da AWS, respeitando credenciais de variáveis de ambiente, perfis do sistema ou IAM roles anexadas à instância/pod.
- **Customização**: Todos os beans possuem `@ConditionalOnMissingBean`, permitindo sobrescrever qualquer bean caso a aplicação cliente necessite de customizações pontuais.
