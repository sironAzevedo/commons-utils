package com.br.azevedo.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Utilitário genérico para resolução e consulta do ambiente de execução (profiles ativos)
 * da aplicação Spring Boot.
 * <p>
 * Suporta resolução a partir de:
 * <ol>
 *   <li>{@link Environment#getActiveProfiles()}</li>
 *   <li>Propriedade {@code spring.profiles.active} no {@link Environment}</li>
 *   <li>Variável de ambiente {@code SPRING_PROFILES_ACTIVE}</li>
 *   <li>Propriedade de sistema Java {@code spring.profiles.active}</li>
 *   <li>Valor padrão configurável (padrão: {@code 'dev'})</li>
 * </ol>
 * <p>
 * Pode ser utilizado tanto de forma estática (passando ou não uma instância de {@link Environment})
 * quanto como bean Spring injetado/gerenciado via {@link EnvironmentAware}.
 */
@Slf4j
@Component
public class EnvironmentUtils implements EnvironmentAware {

    public static final String DEFAULT_ENVIRONMENT = "dev";
    public static final String PROPERTY_SPRING_PROFILES_ACTIVE = "spring.profiles.active";
    public static final String ENV_SPRING_PROFILES_ACTIVE = "SPRING_PROFILES_ACTIVE";

    private static final List<String> PRODUCTION_PROFILES = List.of("prod", "prd", "production", "staging");
    private static final List<String> DEVELOPMENT_PROFILES = List.of("dev", "development", "local", "default");
    private static final List<String> TEST_PROFILES = List.of("test", "testing");

    private static Environment staticEnvironment;

    @Override
    public void setEnvironment(Environment environment) {
        setStaticEnvironment(environment);
    }

    /**
     * Define explicitamente a instância de {@link Environment} utilizada nas chamadas estáticas sem parâmetro.
     *
     * @param environment o ambiente Spring
     */
    public static void setStaticEnvironment(Environment environment) {
        staticEnvironment = environment;
    }

    /**
     * Resolve o ambiente ativo principal utilizando o {@link Environment} estático (se disponível),
     * variáveis de ambiente ou propriedades de sistema, assumindo 'dev' se nenhum for definido.
     *
     * @return nome do ambiente ativo em minúsculas (ex: 'dev', 'prd', 'hml')
     */
    public static String resolveActiveEnvironment() {
        return resolveActiveEnvironment(staticEnvironment, DEFAULT_ENVIRONMENT);
    }

    /**
     * Resolve o ambiente ativo principal com um valor padrão caso nenhum esteja definido.
     *
     * @param defaultEnvironment ambiente padrão de fallback
     * @return nome do ambiente ativo
     */
    public static String resolveActiveEnvironment(String defaultEnvironment) {
        return resolveActiveEnvironment(staticEnvironment, defaultEnvironment);
    }

    /**
     * Resolve o ambiente ativo principal a partir do {@link Environment} informado.
     *
     * @param environment instância do Environment Spring
     * @return nome do ambiente ativo
     */
    public static String resolveActiveEnvironment(Environment environment) {
        return resolveActiveEnvironment(environment, DEFAULT_ENVIRONMENT);
    }

    /**
     * Resolve o ambiente ativo principal a partir do {@link Environment} e do fallback informados.
     *
     * @param environment instância do Environment Spring (ou nulo para busca em variáveis de sistema)
     * @param defaultEnvironment ambiente padrão de fallback
     * @return nome do ambiente ativo
     */
    public static String resolveActiveEnvironment(Environment environment, String defaultEnvironment) {
        String fallback = StringUtils.isNotBlank(defaultEnvironment)
                ? defaultEnvironment.trim().toLowerCase(Locale.ROOT)
                : DEFAULT_ENVIRONMENT;

        Environment env = environment != null ? environment : staticEnvironment;
        String activeProfile = null;

        if (env != null) {
            String[] activeProfiles = env.getActiveProfiles();
            if (activeProfiles != null && activeProfiles.length > 0) {
                for (String p : activeProfiles) {
                    if (StringUtils.isNotBlank(p)) {
                        activeProfile = p;
                        break;
                    }
                }
            }

            if (StringUtils.isBlank(activeProfile)) {
                activeProfile = env.getProperty(PROPERTY_SPRING_PROFILES_ACTIVE);
            }
        }

        if (StringUtils.isBlank(activeProfile)) {
            activeProfile = System.getenv(ENV_SPRING_PROFILES_ACTIVE);
        }

        if (StringUtils.isBlank(activeProfile)) {
            activeProfile = System.getProperty(PROPERTY_SPRING_PROFILES_ACTIVE);
        }

        if (StringUtils.isBlank(activeProfile)) {
            log.debug("Nenhum profile ativo identificado. Utilizando ambiente padrão: [{}]", fallback);
            return fallback;
        }

        if (activeProfile.contains(",")) {
            for (String part : activeProfile.split(",")) {
                if (StringUtils.isNotBlank(part)) {
                    activeProfile = part;
                    break;
                }
            }
        }

        return activeProfile.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Retorna a lista imutável de todos os profiles ativos identificados.
     *
     * @param environment instância do Environment Spring
     * @return lista de profiles ativos
     */
    public static List<String> getActiveProfiles(Environment environment) {
        Environment env = environment != null ? environment : staticEnvironment;
        List<String> result = new ArrayList<>();

        if (env != null && env.getActiveProfiles() != null && env.getActiveProfiles().length > 0) {
            for (String p : env.getActiveProfiles()) {
                if (StringUtils.isNotBlank(p)) {
                    result.add(p.trim().toLowerCase(Locale.ROOT));
                }
            }
        }

        if (result.isEmpty() && env != null) {
            String active = env.getProperty(PROPERTY_SPRING_PROFILES_ACTIVE);
            addProfilesFromString(result, active);
        }

        if (result.isEmpty()) {
            addProfilesFromString(result, System.getenv(ENV_SPRING_PROFILES_ACTIVE));
        }

        if (result.isEmpty()) {
            addProfilesFromString(result, System.getProperty(PROPERTY_SPRING_PROFILES_ACTIVE));
        }

        if (result.isEmpty()) {
            result.add(DEFAULT_ENVIRONMENT);
        }

        return Collections.unmodifiableList(result);
    }

    /**
     * Retorna a lista imutável de profiles ativos utilizando o Environment estático ou sistema.
     *
     * @return lista de profiles ativos
     */
    public static List<String> getActiveProfiles() {
        return getActiveProfiles(staticEnvironment);
    }

    /**
     * Verifica se o profile informado está ativo.
     *
     * @param environment instância do Environment Spring
     * @param targetProfile profile a ser consultado
     * @return true se o profile estiver ativo, false caso contrário
     */
    public static boolean hasProfile(Environment environment, String targetProfile) {
        if (StringUtils.isBlank(targetProfile)) {
            return false;
        }
        String normalizedTarget = targetProfile.trim().toLowerCase(Locale.ROOT);
        return getActiveProfiles(environment).contains(normalizedTarget);
    }

    /**
     * Verifica se o profile informado está ativo utilizando o Environment estático ou sistema.
     *
     * @param targetProfile profile a ser consultado
     * @return true se o profile estiver ativo, false caso contrário
     */
    public static boolean hasProfile(String targetProfile) {
        return hasProfile(staticEnvironment, targetProfile);
    }

    /**
     * Verifica se o ambiente de execução é de Produção ('prod', 'prd', 'production', 'staging').
     *
     * @param environment instância do Environment Spring
     * @return true se for produção
     */
    public static boolean isProduction(Environment environment) {
        return matchesAny(getActiveProfiles(environment), PRODUCTION_PROFILES);
    }

    /**
     * Verifica se o ambiente de execução é de Produção.
     *
     * @return true se for produção
     */
    public static boolean isProduction() {
        return isProduction(staticEnvironment);
    }

    /**
     * Verifica se o ambiente de execução é de Desenvolvimento ('dev', 'development', 'local', 'default').
     *
     * @param environment instância do Environment Spring
     * @return true se for desenvolvimento
     */
    public static boolean isDevelopment(Environment environment) {
        return matchesAny(getActiveProfiles(environment), DEVELOPMENT_PROFILES);
    }

    /**
     * Verifica se o ambiente de execução é de Desenvolvimento.
     *
     * @return true se for desenvolvimento
     */
    public static boolean isDevelopment() {
        return isDevelopment(staticEnvironment);
    }

    /**
     * Verifica se o ambiente de execução é de Testes ('test', 'testing').
     *
     * @param environment instância do Environment Spring
     * @return true se for teste
     */
    public static boolean isTest(Environment environment) {
        return matchesAny(getActiveProfiles(environment), TEST_PROFILES);
    }

    /**
     * Verifica se o ambiente de execução é de Testes.
     *
     * @return true se for teste
     */
    public static boolean isTest() {
        return isTest(staticEnvironment);
    }

    private static void addProfilesFromString(List<String> list, String value) {
        if (StringUtils.isNotBlank(value)) {
            for (String part : value.split(",")) {
                if (StringUtils.isNotBlank(part)) {
                    list.add(part.trim().toLowerCase(Locale.ROOT));
                }
            }
        }
    }

    private static boolean matchesAny(List<String> activeProfiles, List<String> targetProfiles) {
        for (String active : activeProfiles) {
            for (String target : targetProfiles) {
                if (active.equalsIgnoreCase(target)) {
                    return true;
                }
            }
        }
        return false;
    }
}
