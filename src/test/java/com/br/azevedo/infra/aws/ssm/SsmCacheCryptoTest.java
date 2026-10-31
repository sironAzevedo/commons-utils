package com.br.azevedo.infra.aws.ssm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SsmCacheCryptoTest {

    @Test
    @DisplayName("Deve mascarar a chave de cache com prefixo sec_ e hash SHA-256")
    void shouldMaskKeyWithPrefixAndSha256() {
        SsmCacheCrypto crypto = new SsmCacheCrypto("my-super-secret");
        String param = "/auth-api/jwt/credentials-dev";

        String maskedKey = crypto.maskKey(param);

        assertNotNull(maskedKey);
        assertTrue(maskedKey.startsWith("sec_"));
        assertEquals(4 + 64, maskedKey.length()); // "sec_" (4 chars) + 64 hex chars
        assertFalse(maskedKey.contains("auth-api"));
        assertFalse(maskedKey.contains("jwt"));
        assertFalse(maskedKey.contains("credentials-dev"));

        // Determinístico para a mesma chave e parâmetro
        assertEquals(maskedKey, crypto.maskKey(param));

        // Diferente para parâmetros distintos
        assertNotEquals(maskedKey, crypto.maskKey("/outra-api/database/password"));
    }

    @Test
    @DisplayName("Diferentes secrets geram diferentes chaves mascaradas para o mesmo parâmetro")
    void shouldGenerateDifferentMaskedKeysForDifferentSecrets() {
        SsmCacheCrypto crypto1 = new SsmCacheCrypto("secret-1");
        SsmCacheCrypto crypto2 = new SsmCacheCrypto("secret-2");
        String param = "/auth-api/jwt/credentials-dev";

        assertNotEquals(crypto1.maskKey(param), crypto2.maskKey(param));
    }

    @Test
    @DisplayName("Deve criptografar e descriptografar texto com sucesso usando AES-256-GCM")
    void shouldEncryptAndDecryptSuccessfully() {
        SsmCacheCrypto crypto = new SsmCacheCrypto("my-super-secret");
        String originalText = "{\"API_SECRET\":\"super-token-12345\",\"password\":\"p@ssw0rd\"}";

        String encrypted = crypto.encrypt(originalText);

        assertNotNull(encrypted);
        assertTrue(encrypted.startsWith("ENC:GCM:"));
        assertFalse(encrypted.contains("super-token-12345"));
        assertFalse(encrypted.contains("p@ssw0rd"));

        String decrypted = crypto.decrypt(encrypted);
        assertEquals(originalText, decrypted);
    }

    @Test
    @DisplayName("Dois encrypts do mesmo texto devem gerar ciphertexts diferentes (devido ao IV aleatório)")
    void shouldGenerateDifferentCiphertextsDueToRandomIv() {
        SsmCacheCrypto crypto = new SsmCacheCrypto("my-super-secret");
        String originalText = "secret-value";

        String encrypted1 = crypto.encrypt(originalText);
        String encrypted2 = crypto.encrypt(originalText);

        assertNotEquals(encrypted1, encrypted2);
        assertEquals(crypto.decrypt(encrypted1), originalText);
        assertEquals(crypto.decrypt(encrypted2), originalText);
    }

    @Test
    @DisplayName("Deve retornar texto original se for dado legado sem prefixo de criptografia")
    void shouldReturnLegacyPlainTextAsIs() {
        SsmCacheCrypto crypto = new SsmCacheCrypto("my-super-secret");
        String legacyText = "legacy-plain-token";

        String result = crypto.decrypt(legacyText);

        assertEquals(legacyText, result);
    }

    @Test
    @DisplayName("Deve retornar null ao tentar descriptografar payload corrompido")
    void shouldReturnNullOnCorruptedCiphertext() {
        SsmCacheCrypto crypto = new SsmCacheCrypto("my-super-secret");
        String corrupted = "ENC:GCM:invalid-payload";

        String result = crypto.decrypt(corrupted);

        assertNull(result);
    }

    @Test
    @DisplayName("Deve ser 100% interoperável com Node.js SsmCacheCrypto (mesmo hash e mesmo AES-GCM)")
    void shouldInteroperateWithNodeCrypto() {
        SsmCacheCrypto javaCrypto = new SsmCacheCrypto("cross-platform-secret-123");

        // Hash gerado pelo Node.js
        String param = "/auth-api/jwt/credentials-dev";
        String expectedMaskedKey = "sec_237379b619e09da2ce8d883dc2ad76d1fdb0eb2cbfac1bf421c637db53e64f3d";
        assertEquals(expectedMaskedKey, javaCrypto.maskKey(param));

        // Payload cifrado pelo Node.js com a mesma secret
        String nodeCiphertext = "ENC:GCM:HMVOcPKLiGNVRImkhJ4VENOz/w1VvMSqjg+la9Rg+jrb1gZObN6GqO5tmv2aCoEaHe15XMoe";
        String decryptedInJava = javaCrypto.decrypt(nodeCiphertext);
        assertEquals("super-cross-platform-token", decryptedInJava);
    }
}
