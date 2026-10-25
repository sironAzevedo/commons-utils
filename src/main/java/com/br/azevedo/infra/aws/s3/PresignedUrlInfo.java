package com.br.azevedo.infra.aws.s3;

import java.time.LocalDateTime;

/**
 * Informações extraídas de uma URL pré-assinada do S3.
 *
 * @param dataExpiracao Data e hora UTC de expiração do link
 * @param isExpired     Indica se o link já está expirado em relação ao instante atual
 * @param nomeArquivo   Nome original do arquivo decodificado a partir da URL
 */
public record PresignedUrlInfo(
        LocalDateTime dataExpiracao,
        boolean isExpired,
        String nomeArquivo
) {
    public PresignedUrlInfo(LocalDateTime dataExpiracao, boolean isExpired) {
        this(dataExpiracao, isExpired, null);
    }
}
