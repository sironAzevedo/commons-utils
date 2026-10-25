package com.br.azevedo.infra.aws.s3;

import java.io.InputStream;
import java.net.URL;
import java.time.Duration;

/**
 * Contrato centralizador de operações com o Amazon S3 e S3Presigner.
 */
public interface AwsS3StorageService {

    /**
     * Gera URL pré-assinada para upload de arquivo (PUT).
     *
     * @param bucket      Nome do bucket
     * @param key         Chave/caminho do arquivo no bucket
     * @param duration    Duração da validade da URL
     * @param contentType Tipo MIME do conteúdo (opcional)
     * @return URL pré-assinada
     */
    URL generatePresignedUploadUrl(String bucket, String key, Duration duration, String contentType);

    /**
     * Gera URL pré-assinada para upload utilizando o bucket padrão configurado.
     */
    URL generatePresignedUploadUrl(String key, Duration duration, String contentType);

    /**
     * Gera URL pré-assinada para download de arquivo (GET).
     *
     * @param bucket             Nome do bucket
     * @param key                Chave/caminho do arquivo
     * @param duration           Duração da validade da URL
     * @param contentType        Content-Type da resposta HTTP (opcional)
     * @param contentDisposition Content-Disposition da resposta (ex: inline ou attachment; filename="...")
     * @return URL pré-assinada
     */
    URL generatePresignedDownloadUrl(String bucket, String key, Duration duration, String contentType, String contentDisposition);

    /**
     * Gera URL pré-assinada para download utilizando o bucket padrão configurado.
     */
    URL generatePresignedDownloadUrl(String key, Duration duration);

    /**
     * Verifica se um arquivo existe no bucket especificado.
     *
     * @param bucket Nome do bucket
     * @param key    Chave/caminho do arquivo
     * @return true se o arquivo existe, false caso contrário
     */
    boolean fileExists(String bucket, String key);

    /**
     * Verifica se um arquivo existe utilizando o bucket padrão configurado.
     */
    boolean fileExists(String key);

    /**
     * Move (copia e remove da origem) um arquivo entre buckets/caminhos.
     */
    void moveFile(String sourceBucket, String sourceKey, String destinationBucket, String destinationKey);

    /**
     * Move um arquivo dentro do mesmo bucket padrão.
     */
    void moveFile(String fromPath, String toPath);

    /**
     * Copia um arquivo entre buckets/caminhos.
     */
    void copyFile(String sourceBucket, String sourceKey, String destinationBucket, String destinationKey);

    /**
     * Remove um arquivo do bucket especificado.
     */
    void removeFile(String bucket, String key);

    /**
     * Remove um arquivo do bucket padrão.
     */
    void removeFile(String key);

    /**
     * Faz upload direto de bytes para o S3.
     */
    void uploadFile(String bucket, String key, byte[] content, String contentType);

    /**
     * Faz upload direto de bytes para o bucket padrão.
     */
    void uploadFile(String key, byte[] content, String contentType);

    /**
     * Faz upload via InputStream para o S3.
     */
    void uploadFile(String bucket, String key, InputStream inputStream, long contentLength, String contentType);

    /**
     * Baixa os bytes de um arquivo do S3.
     */
    byte[] downloadFile(String bucket, String key);

    /**
     * Baixa os bytes de um arquivo do bucket padrão.
     */
    byte[] downloadFile(String key);

    /**
     * Inspeciona uma URL pré-assinada e determina se ela já expirou.
     *
     * @param presignedUrl URL pré-assinada completa do S3
     * @return Informações de expiração e nome do arquivo
     */
    PresignedUrlInfo isPresignedUrlExpired(String presignedUrl);

    /**
     * Extrai o nome do arquivo a partir de uma URL pré-assinada do S3.
     *
     * @param presignedUrl URL pré-assinada
     * @return Nome decodificado do arquivo
     */
    String extractFileName(String presignedUrl);
}
