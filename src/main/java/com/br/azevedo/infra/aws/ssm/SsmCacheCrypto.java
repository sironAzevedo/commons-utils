package com.br.azevedo.infra.aws.ssm;

import com.br.azevedo.utils.HashUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * Utilitário de segurança e criptografia para o cache de parâmetros do AWS SSM Parameter Store.
 * <p>
 * Responsável por:
 * 1. Mascarar o nome da chave de cache no Redis através de Hash unidirecional (SHA-256 com Salt),
 *    evitando a exposição da estrutura, nomes e caminhos dos parâmetros.
 * 2. Criptografar simetricamente o conteúdo dos parâmetros usando AES-256-GCM (Authenticated Encryption),
 *    garantindo que mesmo com acesso direto ao Redis, o conteúdo seja ilegível e à prova de adulteração.
 */
@Slf4j
public class SsmCacheCrypto {

    public static final String PREFIX_ENCRYPTED_GCM = "ENC:GCM:";
    public static final String PREFIX_MASKED_KEY = "sec_";

    private static final String DEFAULT_MASTER_SALT = "AzevedoCommonsUtils::SSM::SecureCache::Salt::v1";
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH_BYTES = 12; // 96 bits recomendado pelo NIST para AES-GCM
    private static final int GCM_TAG_LENGTH_BITS = 128; // 128 bits de tag de autenticação

    private final String secretKey;
    private final byte[] derivedAesKeyBytes;
    private final SecureRandom secureRandom;

    public SsmCacheCrypto() {
        this((String) null);
    }

    public SsmCacheCrypto(String customSecretKey) {
        this.secretKey = resolveSecretKey(customSecretKey, null);
        this.derivedAesKeyBytes = derive256BitKey(this.secretKey);
        this.secureRandom = new SecureRandom();
    }

    public SsmCacheCrypto(Environment environment) {
        this.secretKey = resolveSecretKey(null, environment);
        this.derivedAesKeyBytes = derive256BitKey(this.secretKey);
        this.secureRandom = new SecureRandom();
    }

    /**
     * Gera uma chave de cache ofuscada e unidirecional para o Redis a partir do nome do parâmetro SSM.
     * O formato de saída é "sec_" + SHA256(secretKey + ":" + parameterName).
     *
     * @param parameterName nome original do parâmetro no SSM (ex: /auth-api/jwt/credentials-dev)
     * @return hash seguro e determinístico para uso como chave no cache
     */
    public String maskKey(String parameterName) {
        if (StringUtils.isBlank(parameterName)) {
            return parameterName;
        }
        String saltedInput = this.secretKey + ":" + parameterName.trim();
        String sha256Hex = HashUtils.sha256(saltedInput);
        return PREFIX_MASKED_KEY + sha256Hex;
    }

    /**
     * Criptografa o valor do parâmetro utilizando AES-256-GCM.
     * Gera um novo IV criptograficamente seguro para cada execução.
     *
     * @param plainText texto em claro a ser armazenado no cache
     * @return texto cifrado com prefixo ENC:GCM: e payload Base64 (IV + Ciphertext + Tag)
     */
    public String encrypt(String plainText) {
        if (StringUtils.isBlank(plainText)) {
            return plainText;
        }
        try {
            byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(derivedAesKeyBytes, "AES");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);

            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);
            byte[] cipherBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[iv.length + cipherBytes.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(cipherBytes, 0, combined, iv.length, cipherBytes.length);

            return PREFIX_ENCRYPTED_GCM + Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            log.error("Erro ao criptografar valor para o cache SSM: {}", e.getMessage());
            throw new RuntimeException("Falha na criptografia do cache SSM", e);
        }
    }

    /**
     * Descriptografa o valor obtido do cache.
     * Suporta retrocompatibilidade: se o valor não possuir o prefixo de criptografia,
     * assume que se trata de dado legado em texto claro e o retorna diretamente.
     *
     * @param cachedValue valor recuperado do cache
     * @return texto em claro descriptografado
     */
    public String decrypt(String cachedValue) {
        if (StringUtils.isBlank(cachedValue)) {
            return cachedValue;
        }

        // Se não possui o prefixo criptografado, é um dado legado ou mock em texto puro
        if (!cachedValue.startsWith(PREFIX_ENCRYPTED_GCM)) {
            return cachedValue;
        }

        try {
            String base64Payload = cachedValue.substring(PREFIX_ENCRYPTED_GCM.length());
            byte[] combined = Base64.getDecoder().decode(base64Payload);

            if (combined.length <= GCM_IV_LENGTH_BYTES) {
                log.warn("Payload cifrado do cache SSM tem tamanho inválido.");
                return null;
            }

            byte[] iv = Arrays.copyOfRange(combined, 0, GCM_IV_LENGTH_BYTES);
            byte[] cipherBytes = Arrays.copyOfRange(combined, GCM_IV_LENGTH_BYTES, combined.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(derivedAesKeyBytes, "AES");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);

            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
            byte[] plainBytes = cipher.doFinal(cipherBytes);

            return new String(plainBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("Falha ao descriptografar valor do cache SSM (pode ter sido gravado com chave anterior): {}", e.getMessage());
            return null; // Força Cache MISS para buscar valor atualizado no SSM
        }
    }

    private String resolveSecretKey(String customKey, Environment env) {
        if (StringUtils.isNotBlank(customKey)) {
            return customKey.trim();
        }

        if (env != null) {
            String fromProperties = env.getProperty("cloud.aws.ssm.cache.secret-key");
            if (StringUtils.isNotBlank(fromProperties)) {
                return fromProperties.trim();
            }

            String fromAlternative = env.getProperty("cloud.aws.ssm.cache.secret");
            if (StringUtils.isNotBlank(fromAlternative)) {
                return fromAlternative.trim();
            }
        }

        // Variáveis de ambiente padrão
        String envVar = System.getenv("AWS_SSM_CACHE_SECRET_KEY");
        if (StringUtils.isNotBlank(envVar)) {
            return envVar.trim();
        }

        envVar = System.getenv("SSM_CACHE_SECRET");
        if (StringUtils.isNotBlank(envVar)) {
            return envVar.trim();
        }

        // Fallback determinístico seguro baseado na aplicação
        String appName = env != null ? env.getProperty("spring.application.name", "azevedo-app") : "azevedo-app";
        return DEFAULT_MASTER_SALT + "::" + appName;
    }

    private byte[] derive256BitKey(String secret) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            return sha.digest(secret.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 não disponível no ambiente", e);
        }
    }
}
