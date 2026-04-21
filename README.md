O projeto `azevedo-commons-utils` é uma biblioteca Java que oferece utilitários e componentes para facilitar o desenvolvimento de aplicações, especialmente em cenários corporativos. Abaixo está uma documentação completa das funcionalidades disponíveis, organizada por módulos e diretórios.

# azevedo-commons-utils

## Sumário

- [Conversores de Datas](#conversores-de-datas)
- [Serializadores e Desserializadores](#serializadores-e-desserializadores)
- [Exceções Personalizadas](#exceções-personalizadas)
- [Infraestrutura](#infraestrutura)
    - [Assíncrono](#assíncrono)
    - [Cache](#cache)
    - [Log](#log)
- [Modelos](#modelos)
    - [DTOs](#dtos)
    - [Enums](#enums)
    - [VOs](#vos)
- [Utilitários](#utilitários)
- [Validação](#validação)
- [Internacionalização](#internacionalização)
- [Recursos](#recursos)

---

## Conversores de Datas

Localização: `com/br/azevedo/conversor/date/`

- **DateFormat**: Utilitário para formatação de datas.
- **DateRange**: Representa um intervalo de datas.
- **ISOLocalDateDeserializer / ISOLocalDateSerializer**: (De)serialização de `LocalDate` em formato ISO.
- **ISOLocalDateTimeDeserializer / ISOLocalDateTimeSerializer**: (De)serialização de `LocalDateTime` em formato ISO.
- **LocalDateSerializer / LocalDateTimeSerializer**: Serializadores customizados para datas.

## Serializadores e Desserializadores

Localização:
- Desserializador: `com/br/azevedo/conversor/deserializer/BigDecimalCurrencyDeserializer`
- Serializador: `com/br/azevedo/conversor/serializer/BigDecimalCurrencySerializer`

- **BigDecimalCurrencyDeserializer**: Converte valores monetários em `BigDecimal` a partir de strings.
- **BigDecimalCurrencySerializer**: Serializa valores `BigDecimal` para formato monetário.

## Exceções Personalizadas

Localização: `com/br/azevedo/exception/`

- **ApplicationException**: Exceção base para erros de aplicação.
- **AuthenticationException / AuthorizationException**: Erros de autenticação e autorização.
- **BusinessException**: Exceção para regras de negócio.
- **ClientApiException**: Erros em chamadas de APIs externas.
- **ConflictException**: Conflitos de dados.
- **EmptyResultDataAccessException / EntidadeNaoEncontradaException / NotFoundException**: Entidade não encontrada ou resultado vazio.
- **FaturaException**: Erros relacionados a faturas.
- **InternalErrorException**: Erros internos.
- **PermissaoException**: Falha de permissão.
- **UserException**: Erros relacionados ao usuário.
- **ValidationException**: Erros de validação.

## Infraestrutura

### Assíncrono

Localização: `com/br/azevedo/infra/async/`

- **AsyncConfig / AsyncProperties**: Configuração de execução assíncrona.
- **EnableAsync**: Anotação para habilitar processamento assíncrono.

### Cache

Localização: `com/br/azevedo/infra/cache/`

- **AbstractCacheConfig / MultipleCacheConfig**: Configuração de múltiplos caches.
- **CacheConfigurationProperties / CacheProperties**: Propriedades de configuração de cache.
- **EnableCache**: Anotação para habilitar cache.
- **ehCache / redis**: Suporte a diferentes implementações de cache.

### Log

Localização: `com/br/azevedo/infra/log/`

- **method / xtrid**: Estruturas para logging customizado (detalhes dependem dos arquivos internos).

## Modelos

### DTOs

Localização: `com/br/azevedo/model/dto/`

- **UserDTO**: Objeto de transferência de dados para usuário.

### Enums

Localização: `com/br/azevedo/model/enums/`

- **PerfilEnum**: Enumeração de perfis de usuário.
- **StatusEnum / UserStatusEnum**: Enumerações de status.

### VOs

Localização: `com/br/azevedo/model/vo/`

- **FieldMessage**: Representa mensagens de campo para validação.
- **StandardError**: Estrutura padrão para erros.

## Utilitários

Localização: `com/br/azevedo/utils/`

- **ConstantesUtils**: Constantes gerais.
- **DateUtils**: Utilitários para manipulação de datas.
- **JsonUtils**: Utilitários para manipulação de JSON.
- **MoedaUtils**: Utilitários para manipulação de moedas.
- **PhoneUtils**: Utilitários para manipulação de telefones.

## Validação

Localização: `com/br/azevedo/validation/`

- **AbstraticEntity**: Entidade abstrata para validação.
- **group/**: Grupos de validação (`Create`, `Delete`, `Patch`, `Update`).
- **model/**: Modelos para validação.

## Internacionalização

Localização: `com/br/azevedo/utils/mensagemUtils/`

- **BundleMessageConfig**: Configuração de mensagens internacionalizadas.
- **EnableI18N**: Anotação para habilitar internacionalização.
- **MessageUtils**: Utilitário para mensagens.
- **README.md**: Documentação do módulo.

## Recursos

Localização: `src/main/resources/`

- **exceptionMessages.properties**: Mensagens de exceção internacionalizadas.
- **META-INF/**: Metadados do projeto.

---

## Como Utilizar

1. **Adicione a dependência Maven** ao seu projeto.
2. **Implemente os utilitários e componentes** conforme a necessidade, utilizando as classes e anotações disponíveis.
3. **Configure os módulos de cache, async e internacionalização** conforme os exemplos nos arquivos `README.md` de cada módulo.

---

## Observações

- O projeto é modular e pode ser utilizado parcialmente conforme a necessidade.
- Os exemplos de uso e configurações detalhadas estão disponíveis nos arquivos `README.md` de cada módulo.

---

**Licença:** Consulte o arquivo de licença do projeto para detalhes sobre uso e distribuição.