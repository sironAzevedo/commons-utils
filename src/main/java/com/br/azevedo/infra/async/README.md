# Módulo `com.br.azevedo.infra.async` — Uso da anotação @EnableAsync

Este pacote fornece uma anotação conveniente `@EnableAsync` que importa automaticamente a configuração necessária para habilitar processamento assíncrono com um ThreadPool configurável (via propriedades `async.*`).

Sumário rápido
- O que faz: registra `AsyncConfig` e `AsyncProperties` no contexto (via `@Import`) para habilitar execução assíncrona usando `ThreadPoolTaskExecutor`.
- Local: `com.br.azevedo.infra.async`.
- Como usar: basta anotar a classe principal da aplicação com `@EnableAsync` (importar `com.br.azevedo.infra.async.EnableAsync`).

Dependência
Adicione a dependência do artefato que fornece este pacote no seu projeto Maven/Gradle:

Maven
```
<dependency>
    <groupId>com.br.azevedo</groupId>
    <artifactId>commons-utils</artifactId>
    <version>versão-atual</version>
</dependency>
```

Uso (exemplo)
1) Anote a classe principal da aplicação com `@EnableAsync`:

```
import com.br.azevedo.infra.async.EnableAsync;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;

@EnableAsync
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

2) Configure as propriedades `async.*` no seu `application.yml` ou `application.properties` (veja observações abaixo):

application.yml (exemplo)
```
async:
  corePoolSize: 3
  queueCapacity: 50
  maxPoolSize: 10
  threadNamePrefix: APP_ASYNC-
  # enabled: true # (opcional) habilita/desabilita o módulo
```

Como usar métodos assíncronos
- Marque métodos com `@Async` (do Spring) para que sejam executados em background.
- Pode retornar `void`, `java.util.concurrent.Future<T>`, `java.util.concurrent.CompletableFuture<T>` ou qualquer outro wrapper compatível.

Exemplo de serviço
```
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class RelatorioService {

    @Async
    public void gerarRelatorio(Long id) {
        // processamento em background
    }

    @Async
    public CompletableFuture<String> gerarRelatorioAsync(Long id) {
        // processamento e retorno futuro
        return CompletableFuture.completedFuture("OK");
    }
}
```

Propriedades disponíveis (chave `async`)
- `async.enabled` (boolean) — Habilita/desabilita a configuração. Observação: `AsyncConfig` usa `@ConditionalOnProperty(value = "async.enabled", havingValue = "true", matchIfMissing = true)`, portanto por padrão o módulo está habilitado se a propriedade estiver ausente. Para desabilitar: `async.enabled=false`.
- `async.corePoolSize` (Integer) — número de threads do pool que ficam sempre ativas. (Ex.: `3`)
- `async.queueCapacity` (Integer) — capacidade da fila de tarefas antes de aumentar o pool. (Ex.: `50`)
- `async.maxPoolSize` (Integer) — número máximo de threads que podem ser criadas no pool. (Ex.: `10`)
- `async.threadNamePrefix` (String) — prefixo para o nome das threads do pool (Ex.: `APP_ASYNC-`).

Importante — valores obrigatórios / comportamento
- A implementação atual de `AsyncConfig` obtém as propriedades diretamente de `AsyncProperties` e usa-as ao configurar `ThreadPoolTaskExecutor`.
- Se alguma propriedade numérica (por exemplo `corePoolSize`) não for fornecida (ou for nula), a chamada para configurar o executor pode causar um erro devido a unboxing de `Integer` para `int`. Portanto, é altamente recomendável definir explicitamente `corePoolSize`, `queueCapacity` e `maxPoolSize` no seu arquivo de configuração.

Tratamento de exceções
- `AsyncConfig` já registra um `AsyncUncaughtExceptionHandler` que faz log de exceções não capturadas em métodos `@Async` (quando o método retorna `void`). Para métodos que retornam `Future`/`CompletableFuture`, capture exceções no próprio futuro.

Customizações e extensões
- Se precisar de um comportamento diferente do `ThreadPoolTaskExecutor` padrão, você pode:
  - Definir um bean `Executor` nomeado (por exemplo `taskExecutor`) na sua configuração; dependendo do contexto, isso pode sobrescrever o executor usado pelo Spring.
  - Substituir `AsyncProperties` registrando seu próprio bean com o mesmo tipo e valores customizados.

Considerações finais / boas práticas
- Ajuste `corePoolSize`, `maxPoolSize` e `queueCapacity` com base no perfil de carga da sua aplicação e nas características do ambiente (CPU, I/O, memória). Use valores conservadores inicialmente e monitore o consumo de threads.
- Lembre-se que processamento assíncrono não resolve problemas de concorrência — proteja recursos compartilhados adequadamente.

Se quiser que eu adicione exemplos adicionais (por exemplo, um snippet `application.properties`, ou um exemplo de teste unitário para métodos `@Async`), diga qual formato prefere e eu adiciono.
