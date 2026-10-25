package com.br.azevedo.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnvironmentUtilsTest {

    private MockEnvironment environment;

    @BeforeEach
    void setUp() {
        environment = new MockEnvironment();
        EnvironmentUtils.setStaticEnvironment(null);
    }

    @AfterEach
    void tearDown() {
        System.clearProperty(EnvironmentUtils.PROPERTY_SPRING_PROFILES_ACTIVE);
        EnvironmentUtils.setStaticEnvironment(null);
    }

    @Test
    @DisplayName("Deve resolver ambiente ativo a partir do getActiveProfiles do Environment")
    void shouldResolveFromActiveProfiles() {
        environment.setActiveProfiles("hml");
        String result = EnvironmentUtils.resolveActiveEnvironment(environment);
        assertEquals("hml", result);
    }

    @Test
    @DisplayName("Deve considerar o primeiro profile quando houver múltiplos em getActiveProfiles")
    void shouldResolveFirstWhenMultipleActiveProfiles() {
        environment.setActiveProfiles("prd", "cloud");
        String result = EnvironmentUtils.resolveActiveEnvironment(environment);
        assertEquals("prd", result);
    }

    @Test
    @DisplayName("Deve resolver ambiente a partir da propriedade spring.profiles.active")
    void shouldResolveFromPropertyWhenNoActiveProfiles() {
        environment.setProperty(EnvironmentUtils.PROPERTY_SPRING_PROFILES_ACTIVE, "staging,aws");
        String result = EnvironmentUtils.resolveActiveEnvironment(environment);
        assertEquals("staging", result);
    }

    @Test
    @DisplayName("Deve resolver ambiente a partir de propriedade de sistema Java quando environment for nulo")
    void shouldResolveFromSystemPropertyWhenEnvironmentIsNull() {
        System.setProperty(EnvironmentUtils.PROPERTY_SPRING_PROFILES_ACTIVE, "uat");
        String result = EnvironmentUtils.resolveActiveEnvironment();
        assertEquals("uat", result);
    }

    @Test
    @DisplayName("Deve normalizar para minúsculas e sem espaços adicionais")
    void shouldNormalizeToLowercaseAndTrim() {
        environment.setActiveProfiles("  PROD  ");
        String result = EnvironmentUtils.resolveActiveEnvironment(environment);
        assertEquals("prod", result);
    }

    @Test
    @DisplayName("Deve retornar dev como padrão quando nenhum profile estiver definido")
    void shouldReturnDefaultDevWhenNoneDefined() {
        String result = EnvironmentUtils.resolveActiveEnvironment(environment);
        assertEquals("dev", result);
    }

    @Test
    @DisplayName("Deve respeitar fallback customizado quando informado")
    void shouldRespectCustomFallback() {
        String result = EnvironmentUtils.resolveActiveEnvironment(environment, "local");
        assertEquals("local", result);
    }

    @Test
    @DisplayName("Deve identificar corretamente ambientes de Produção")
    void shouldIdentifyProductionEnvironments() {
        environment.setActiveProfiles("prd");
        assertTrue(EnvironmentUtils.isProduction(environment));
        assertFalse(EnvironmentUtils.isDevelopment(environment));
        assertFalse(EnvironmentUtils.isTest(environment));

        environment.setActiveProfiles("production");
        assertTrue(EnvironmentUtils.isProduction(environment));

        environment.setActiveProfiles("staging");
        assertTrue(EnvironmentUtils.isProduction(environment));

        environment.setActiveProfiles("prod");
        assertTrue(EnvironmentUtils.isProduction(environment));
    }

    @Test
    @DisplayName("Deve identificar corretamente ambientes de Desenvolvimento")
    void shouldIdentifyDevelopmentEnvironments() {
        environment.setActiveProfiles("dev");
        assertTrue(EnvironmentUtils.isDevelopment(environment));
        assertFalse(EnvironmentUtils.isProduction(environment));

        environment.setActiveProfiles("local");
        assertTrue(EnvironmentUtils.isDevelopment(environment));

        environment.setActiveProfiles("development");
        assertTrue(EnvironmentUtils.isDevelopment(environment));
    }

    @Test
    @DisplayName("Deve identificar corretamente ambiente de Teste")
    void shouldIdentifyTestEnvironment() {
        environment.setActiveProfiles("test");
        assertTrue(EnvironmentUtils.isTest(environment));
        assertFalse(EnvironmentUtils.isProduction(environment));
        assertFalse(EnvironmentUtils.isDevelopment(environment));
    }

    @Test
    @DisplayName("Deve verificar se contém profile específico via hasProfile")
    void shouldCheckHasProfile() {
        environment.setActiveProfiles("dev", "docker", "aws");
        assertTrue(EnvironmentUtils.hasProfile(environment, "docker"));
        assertTrue(EnvironmentUtils.hasProfile(environment, "aws"));
        assertTrue(EnvironmentUtils.hasProfile(environment, "dev"));
        assertFalse(EnvironmentUtils.hasProfile(environment, "k8s"));
    }

    @Test
    @DisplayName("Deve retornar todos os profiles ativos")
    void shouldReturnAllActiveProfiles() {
        environment.setActiveProfiles("hml", "cloud");
        List<String> profiles = EnvironmentUtils.getActiveProfiles(environment);
        assertEquals(2, profiles.size());
        assertTrue(profiles.contains("hml"));
        assertTrue(profiles.contains("cloud"));
    }

    @Test
    @DisplayName("Deve suportar injeção via EnvironmentAware e resolver estaticamente")
    void shouldSupportEnvironmentAwareInjection() {
        environment.setActiveProfiles("sandbox");
        EnvironmentUtils utils = new EnvironmentUtils();
        utils.setEnvironment(environment);

        assertEquals("sandbox", EnvironmentUtils.resolveActiveEnvironment());
        assertTrue(EnvironmentUtils.hasProfile("sandbox"));
    }
}
