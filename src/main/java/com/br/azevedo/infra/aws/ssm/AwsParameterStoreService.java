package com.br.azevedo.infra.aws.ssm;

import software.amazon.awssdk.services.ssm.model.ParameterType;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

/**
 * Contrato para operações centralizadas no AWS Systems Manager (SSM) Parameter Store.
 */
public interface AwsParameterStoreService {

    /**
     * Busca o valor de um parâmetro (com descriptografia ativa por padrão e TTL padrão de 24 horas no cache).
     *
     * @param name Nome ou caminho do parâmetro (ex: /auth-api/jwt/credentials-dev)
     * @return Valor do parâmetro em texto
     */
    String getParameter(String name);

    /**
     * Busca o valor de um parâmetro especificando se deve descriptografar (com TTL padrão de 24 horas no cache).
     *
     * @param name           Nome ou caminho do parâmetro
     * @param withDecryption Se true, descriptografa SecureString
     * @return Valor do parâmetro em texto
     */
    String getParameter(String name, boolean withDecryption);

    /**
     * Busca o valor de um parâmetro (com descriptografia ativa) com TTL customizado no cache.
     *
     * @param name Nome ou caminho do parâmetro
     * @param ttl  Tempo de expiração no cache
     * @return Valor do parâmetro em texto
     */
    String getParameter(String name, Duration ttl);

    /**
     * Busca o valor de um parâmetro especificando se deve descriptografar e tempo de expiração no cache.
     *
     * @param name           Nome ou caminho do parâmetro
     * @param withDecryption Se true, descriptografa SecureString
     * @param ttl            Tempo de expiração no cache
     * @return Valor do parâmetro em texto
     */
    String getParameter(String name, boolean withDecryption, Duration ttl);

    /**
     * Busca o valor de um parâmetro de forma segura, retornando Optional vazio caso não exista.
     *
     * @param name Nome ou caminho do parâmetro
     * @return Optional com o valor ou Optional.empty()
     */
    Optional<String> findParameter(String name);

    /**
     * Busca o valor de um parâmetro de forma segura especificando se deve descriptografar.
     *
     * @param name           Nome ou caminho do parâmetro
     * @param withDecryption Se true, descriptografa SecureString
     * @return Optional com o valor ou Optional.empty()
     */
    Optional<String> findParameter(String name, boolean withDecryption);

    /**
     * Busca o valor de um parâmetro de forma segura com TTL customizado no cache.
     *
     * @param name Nome ou caminho do parâmetro
     * @param ttl  Tempo de expiração no cache
     * @return Optional com o valor ou Optional.empty()
     */
    Optional<String> findParameter(String name, Duration ttl);

    /**
     * Busca o valor de um parâmetro de forma segura especificando descriptografia e TTL no cache.
     *
     * @param name           Nome ou caminho do parâmetro
     * @param withDecryption Se true, descriptografa SecureString
     * @param ttl            Tempo de expiração no cache
     * @return Optional com o valor ou Optional.empty()
     */
    Optional<String> findParameter(String name, boolean withDecryption, Duration ttl);

    /**
     * Busca um parâmetro cujo conteúdo seja JSON e faz o parse para Map&lt;String, Object&gt;.
     *
     * @param name Nome ou caminho do parâmetro
     * @return Map com as chaves e valores do JSON
     */
    Map<String, Object> getParameterAsMap(String name);

    /**
     * Busca um parâmetro em JSON com opção de descriptografia e faz o parse para Map&lt;String, Object&gt;.
     *
     * @param name           Nome ou caminho do parâmetro
     * @param withDecryption Se true, descriptografa SecureString
     * @return Map com as chaves e valores do JSON
     */
    Map<String, Object> getParameterAsMap(String name, boolean withDecryption);

    /**
     * Busca um parâmetro em JSON com TTL customizado no cache e faz o parse para Map&lt;String, Object&gt;.
     *
     * @param name Nome ou caminho do parâmetro
     * @param ttl  Tempo de expiração no cache
     * @return Map com as chaves e valores do JSON
     */
    Map<String, Object> getParameterAsMap(String name, Duration ttl);

    /**
     * Busca um parâmetro em JSON com opção de descriptografia e TTL customizado no cache.
     *
     * @param name           Nome ou caminho do parâmetro
     * @param withDecryption Se true, descriptografa SecureString
     * @param ttl            Tempo de expiração no cache
     * @return Map com as chaves e valores do JSON
     */
    Map<String, Object> getParameterAsMap(String name, boolean withDecryption, Duration ttl);

    /**
     * Cria ou atualiza um parâmetro no Parameter Store e invalida o cache correspondente.
     *
     * @param name      Nome do parâmetro
     * @param value     Valor do parâmetro
     * @param type      Tipo (String, StringList, SecureString)
     * @param overwrite Se deve sobrescrever caso já exista
     */
    void putParameter(String name, String value, ParameterType type, boolean overwrite);

    /**
     * Cria ou atualiza um parâmetro do tipo String padrão com overwrite habilitado e invalida o cache.
     *
     * @param name  Nome do parâmetro
     * @param value Valor em texto
     */
    void putStringParameter(String name, String value);

    /**
     * Cria ou atualiza um parâmetro do tipo SecureString com overwrite habilitado e invalida o cache.
     *
     * @param name  Nome do parâmetro
     * @param value Valor sensível em texto
     */
    void putSecureStringParameter(String name, String value);

    /**
     * Remove um parâmetro do Parameter Store e invalida o cache correspondente.
     *
     * @param name Nome do parâmetro
     */
    void deleteParameter(String name);

    /**
     * Busca todos os parâmetros sob um caminho hierárquico.
     *
     * @param path           Caminho base (ex: /auth-api/)
     * @param recursive      Se busca em sub-caminhos
     * @param withDecryption Se descriptografa SecureString
     * @return Map onde a chave é o nome completo do parâmetro e o valor é o conteúdo
     */
    Map<String, String> getParametersByPath(String path, boolean recursive, boolean withDecryption);
}
