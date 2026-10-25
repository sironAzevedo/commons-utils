package com.br.azevedo.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utilitário centralizado para geração de hashes criptográficos (SHA-256, etc.).
 */
public final class HashUtils {

    private HashUtils() {
    }

    /**
     * Gera o hash SHA-256 em formato hexadecimal minúsculo a partir de uma String de entrada.
     *
     * @param input texto a ser hasheado
     * @return hash SHA-256 em hexadecimal, ou null se o input for nulo
     */
    public static String sha256(String input) {
        if (input == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder(hashBytes.length * 2);
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algoritmo SHA-256 não disponível no ambiente", e);
        }
    }
}
