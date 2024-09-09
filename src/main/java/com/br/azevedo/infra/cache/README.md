# Como utilizar a anotação do cache

Adicione a dependencia abaixo no seu projeto:  

````
<dependency>
    <groupId>com.br.azevedo</groupId>
    <artifactId>commons-utils</artifactId>
    <version>versão atual</version>
</dependency>
````
No classe principal da aplicação, adicione a anotação @EnableCache e excluia a classe RedisRepositoriesAutoConfiguration.

````
@EnableCache
@SpringBootApplication(exclude = {RedisRepositoriesAutoConfiguration.class})
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
````

No Arquivo de propriedade, application.yml, application.properties, adicione o seguinte trecho de código:  
A Lib tem suporte para o cache Redis e EhCache

EhCache:  
````
cache:
    ehCache:
        caches:
            - cacheName: user_services_cliente_por_email
              expiration: ${CACHE_DURATION_CONSULTA_PESSOA_EMAIL:PT24H}
            - cacheName: user_services_cliente_login_email
             expiration: ${CACHE_DURATION_CONSULTA_LOGIN_EMAIL:PT24H}
````

Redis:
````
cache:
    redis:
        config:
            host: ${REDIS_HOST:localhost}
            port: ${REDIS_PORT:6379}
            database: ${REDIS_DATABASE:0}
            password: ${REDIS_PASSWORD:senha_caso_tenha}
        caches:
            - cacheName: user_services_cliente_por_email
              expiration: ${CACHE_DURATION_CONSULTA_PESSOA_EMAIL:PT24H}
            - cacheName: user_services_cliente_login_email
              expiration: ${CACHE_DURATION_CONSULTA_LOGIN_EMAIL:PT24H}
````
Por padrão o multicache já vem habilitado e você poderá utilizar os dois caches, distribuido com o redis e o local com o ehCache
````
cache:
    redis:
        config:
            host: ${REDIS_HOST:localhost}
            port: ${REDIS_PORT:6379}
            database: ${REDIS_DATABASE:0}
            password: ${REDIS_PASSWORD:senha_caso_tenha}
        caches:
            - cacheName: user_services_cliente_login_email
              expiration: ${CACHE_DURATION_CONSULTA_LOGIN_EMAIL:PT24H}
    ehCache:
        caches:
            - cacheName: user_services_cliente_por_email
              expiration: ${CACHE_DURATION_CONSULTA_PESSOA_EMAIL:PT24H}
````

OBS.: O valor do parametro cacheName, deverá ser igual colocado na anotação @Cacheable, por exemplo: user_services_cliente_por_email  
````
@Override
@Cacheable(value = "user_services_cliente_por_email", key = "#email", unless="#result == null")
public UserDTO findByEmail(String email) {
    return repo.findByEmail(email).map(UserMapper.INSTANCE::toDTO).orElseThrow(() -> new UserException("Usuario não encontrado"));
}
````

OBS.:  
1 - Caso o cache seja habilitado e você não especificar a configuração de cada cache na lista de caches "caches" a biblioteca irá buscas os metodos anotados com @Cacheable
recuperará o value e criará uma configuração padrão de tempo que é de 1 dia e adicionará seu caches no redis caso o mesmo esteja com as configurações adicionadas.  

2 - Caso deseje desabilitar o cache faça isso de forma explicita conforme exemplo abaixo:

````
cache:
    enabled: false
````

3 - Não repita os caches nos dois caches local e redis, caso isso aconteça a aplicação removerá o cache do redis e deixará no local