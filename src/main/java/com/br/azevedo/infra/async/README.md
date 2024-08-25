# Como utilizar a anotação do @EnableAsync

Adicione a dependencia abaixo no seu projeto:  

````
<dependency>
    <groupId>com.br.azevedo</groupId>
    <artifactId>commons-utils</artifactId>
    <version>versão atual</version>
</dependency>
````
No classe principal da aplicação, adicione a anotação @EnableCache e excluia a classe RedisRepositoriesAutoConfiguration

````
@EnableAsync
@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
````

No Arquivo de propriedade, application.yml, application.properties, adicione o seguinte trecho de código:  
````
async:
    corePoolSize: 2
    queueCapacity: 500
    maxPoolSize: 2
    threadNamePrefix: APP_ASYNC-
````

### Configurações disponíveis
* async.enabled=true 
  * Habilita/Desabilita todas as configurações de async
* async.corePoolSize=3
  * Quantidade de threads que sempre estão disponíveis para processamento
* async.queueCapacity=15
  * Quantidade máxima de threads que podem ser enfileiradas
* async.maxPoolSize=15
  * Quantidade máxima de threads ativas
* async.threadNamePrefix=AsyncApi-
  * Prefixo do nome da thread
