# Como utilizar a anotação do cache

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
        enabled: ${ENABLE_CACHE:true}       
    caches[0]:
        cacheName: user_services_cliente_por_email
        expiration: ${CACHE_DURATION_CONSULTA_PESSOA_EMAIL:PT24H}
    caches[1]:
        cacheName: user_services_cliente_login_email
        expiration: ${CACHE_DURATION_CONSULTA_LOGIN_EMAIL:PT24H}
````


Redis:
````
cache:
    redis:
        enabled: ${REDIS_REDIS:true}
        config:
        host: ${REDIS_HOST:localhost}
        port: ${REDIS_PORT:6379}
        database: ${REDIS_DATABASE:0}
        password: ${REDIS_PASSWORD:senha_default}
    caches[0]:
        cacheName: user_services_cliente_por_email
        expiration: ${CACHE_DURATION_CONSULTA_PESSOA_EMAIL:PT24H}
    caches[1]:
        cacheName: user_services_cliente_login_email
        expiration: ${CACHE_DURATION_CONSULTA_LOGIN_EMAIL:PT24H}
````

OBS.: O valor do parametro cacheName, deverá ser igual colocado na anotação @Cacheable, por exemplo: user_services_cliente_por_email  
````
@Override
@Cacheable(value = "user_services_cliente_por_email", key = "#email", unless="#result == null")
public UserDTO findByEmail(String email) {
    return repo.findByEmail(email).map(UserMapper.INSTANCE::toDTO).orElseThrow(() -> new UserException("Usuario não encontrado"));
}
````

OBS.: Caso o cache seja habilitado e você não especificar a configuração de cada cache na lista de caches "caches[0]:" a biblioteca irá buscas os metodos anotados com @Cacheable
recuperará o value e criará uma configuração padrão de tempo.