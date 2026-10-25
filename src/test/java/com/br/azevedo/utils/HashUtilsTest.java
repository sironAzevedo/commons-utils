package com.br.azevedo.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class HashUtilsTest {

    @Test
    @DisplayName("Deve gerar hash SHA-256 com sucesso")
    void shouldGenerateSha256Successfully() {
        String input = "test-string";
        // SHA-256 de "test-string"
        String expected = "ffe65f1d98fafedea3514adc956c8ada5980c6c5d2552fd61f48401aefd5c00e";

        String actual = HashUtils.sha256(input);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Deve retornar null quando entrada for nula")
    void shouldReturnNullWhenInputIsNull() {
        assertNull(HashUtils.sha256(null));
    }

    @Test
    @DisplayName("Deve gerar hash consistente para string vazia")
    void shouldGenerateHashForEmptyString() {
        // SHA-256 de ""
        String expected = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
        assertEquals(expected, HashUtils.sha256(""));
    }
}
