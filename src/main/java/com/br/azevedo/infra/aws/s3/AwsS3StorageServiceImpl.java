package com.br.azevedo.infra.aws.s3;

import com.br.azevedo.exception.ApplicationException;
import com.br.azevedo.exception.NotFoundException;
import com.br.azevedo.infra.aws.AwsProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementação padrão de operações com o Amazon S3 e geração de URLs pré-assinadas.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AwsS3StorageServiceImpl implements AwsS3StorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final AwsProperties awsProperties;

    private static final Pattern USER_ID_FILE_PATTERN = Pattern.compile("USER_\\d+/([^?]+)");
    private static final DateTimeFormatter AMZ_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

    private String getDefaultBucket() {
        if (awsProperties.getS3() != null && awsProperties.getS3().getBucket() != null && !awsProperties.getS3().getBucket().isBlank()) {
            return awsProperties.getS3().getBucket().trim();
        }
        throw new ApplicationException("Nome do bucket S3 padrão não configurado. Defina 'cloud.aws.s3.bucket' ou informe o bucket no método.");
    }

    @Override
    public URL generatePresignedUploadUrl(String bucket, String key, Duration duration, String contentType) {
        log.debug("Gerando presigned upload URL para bucket={}, key={}", bucket, key);
        PutObjectRequest.Builder requestBuilder = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key);

        if (contentType != null && !contentType.isBlank()) {
            requestBuilder.contentType(contentType);
        }

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(duration != null ? duration : Duration.ofDays(7))
                .putObjectRequest(requestBuilder.build())
                .build();

        return s3Presigner.presignPutObject(presignRequest).url();
    }

    @Override
    public URL generatePresignedUploadUrl(String bucket, String key, Duration duration) {
        return generatePresignedUploadUrl(bucket, key, duration, null);
    }

    @Override
    public URL generatePresignedUploadUrl(String key, Duration duration, String contentType) {
        return generatePresignedUploadUrl(getDefaultBucket(), key, duration, contentType);
    }

    @Override
    public URL generatePresignedUploadUrl(String key, Duration duration) {
        return generatePresignedUploadUrl(getDefaultBucket(), key, duration, null);
    }

    @Override
    public URL generatePresignedDownloadUrl(String bucket, String key, Duration duration, String contentType, String contentDisposition) {
        log.debug("Gerando presigned download URL para bucket={}, key={}", bucket, key);
        GetObjectRequest.Builder requestBuilder = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key);

        if (contentType != null && !contentType.isBlank()) {
            requestBuilder.responseContentType(contentType);
        }
        if (contentDisposition != null && !contentDisposition.isBlank()) {
            requestBuilder.responseContentDisposition(contentDisposition);
        } else {
            requestBuilder.responseContentDisposition("inline");
        }

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(duration != null ? duration : Duration.ofDays(7))
                .getObjectRequest(requestBuilder.build())
                .build();

        return s3Presigner.presignGetObject(presignRequest).url();
    }

    @Override
    public URL generatePresignedDownloadUrl(String key, Duration duration) {
        return generatePresignedDownloadUrl(getDefaultBucket(), key, duration, null, "inline");
    }

    @Override
    public boolean fileExists(String bucket, String key) {
        if (StringUtils.isBlank(key)) {
            return false;
        }

        HeadObjectRequest request = HeadObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        try {
            s3Client.headObject(request);
            return true;
        } catch (NoSuchKeyException e) {
            log.warn("Arquivo não encontrado no S3: bucket={}, key={}", bucket, key);
            return false;
        } catch (S3Exception e) {
            log.error("Erro ao verificar arquivo no S3: bucket={}, key={} - {}", bucket, key, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean fileExists(String key) {
        return fileExists(getDefaultBucket(), key);
    }

    @Override
    public void moveFile(String sourceBucket, String sourceKey, String destinationBucket, String destinationKey) {
        copyFile(sourceBucket, sourceKey, destinationBucket, destinationKey);
        removeFile(sourceBucket, sourceKey);
    }

    @Override
    public void moveFile(String fromPath, String toPath) {
        String bucket = getDefaultBucket();
        moveFile(bucket, fromPath, bucket, toPath);
    }

    @Override
    public void copyFile(String sourceBucket, String sourceKey, String destinationBucket, String destinationKey) {
        log.info("Copiando arquivo no S3 de {}/{} para {}/{}", sourceBucket, sourceKey, destinationBucket, destinationKey);
        CopyObjectRequest request = CopyObjectRequest.builder()
                .sourceBucket(sourceBucket)
                .sourceKey(sourceKey)
                .destinationBucket(destinationBucket)
                .destinationKey(destinationKey)
                .build();

        try {
            s3Client.copyObject(request);
        } catch (Exception e) {
            log.error("Erro ao copiar arquivo no S3: {}", e.getMessage(), e);
            throw new ApplicationException(String.format("Erro ao copiar o arquivo %s para %s: %s", sourceKey, destinationKey, e.getMessage()), e);
        }
    }

    @Override
    public void removeFile(String bucket, String key) {
        log.info("Removendo arquivo no S3: bucket={}, key={}", bucket, key);
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        try {
            s3Client.deleteObject(request);
        } catch (Exception e) {
            log.error("Erro ao remover arquivo no S3: bucket={}, key={} - {}", bucket, key, e.getMessage(), e);
            throw new ApplicationException(String.format("Erro ao remover arquivo %s: %s", key, e.getMessage()), e);
        }
    }

    @Override
    public void removeFile(String key) {
        removeFile(getDefaultBucket(), key);
    }

    @Override
    public void uploadFile(String bucket, String key, byte[] content, String contentType) {
        log.info("Fazendo upload direto de {} bytes para bucket={}, key={}", content.length, bucket, key);
        PutObjectRequest.Builder builder = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key);

        if (contentType != null && !contentType.isBlank()) {
            builder.contentType(contentType);
        }

        try {
            s3Client.putObject(builder.build(), RequestBody.fromBytes(content));
        } catch (Exception e) {
            log.error("Erro no upload para o S3: {}", e.getMessage(), e);
            throw new ApplicationException(String.format("Erro ao fazer upload do arquivo %s para o S3: %s", key, e.getMessage()), e);
        }
    }

    @Override
    public void uploadFile(String key, byte[] content, String contentType) {
        uploadFile(getDefaultBucket(), key, content, contentType);
    }

    @Override
    public void uploadFile(String bucket, String key, InputStream inputStream, long contentLength, String contentType) {
        log.info("Fazendo upload via stream de {} bytes para bucket={}, key={}", contentLength, bucket, key);
        PutObjectRequest.Builder builder = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key);

        if (contentType != null && !contentType.isBlank()) {
            builder.contentType(contentType);
        }

        try {
            s3Client.putObject(builder.build(), RequestBody.fromInputStream(inputStream, contentLength));
        } catch (Exception e) {
            log.error("Erro no upload de stream para o S3: {}", e.getMessage(), e);
            throw new ApplicationException(String.format("Erro ao fazer upload do arquivo %s para o S3: %s", key, e.getMessage()), e);
        }
    }

    @Override
    public byte[] downloadFile(String bucket, String key) {
        log.info("Baixando arquivo do S3: bucket={}, key={}", bucket, key);
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        try {
            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(request);
            return objectBytes.asByteArray();
        } catch (NoSuchKeyException e) {
            throw new NotFoundException(String.format("Arquivo '%s' não encontrado no bucket '%s'", key, bucket));
        } catch (Exception e) {
            log.error("Erro ao baixar arquivo do S3: {}", e.getMessage(), e);
            throw new ApplicationException(String.format("Erro ao baixar arquivo %s do S3: %s", key, e.getMessage()), e);
        }
    }

    @Override
    public byte[] downloadFile(String key) {
        return downloadFile(getDefaultBucket(), key);
    }

    @Override
    public PresignedUrlInfo isPresignedUrlExpired(String presignedUrl) {
        String nomeArquivo = extractFileName(presignedUrl);
        if (StringUtils.isBlank(presignedUrl)) {
            return new PresignedUrlInfo(null, false, nomeArquivo);
        }

        try {
            URL url = new URL(presignedUrl);
            String query = url.getQuery();
            if (StringUtils.isBlank(query)) {
                return new PresignedUrlInfo(null, false, nomeArquivo);
            }

            Map<String, String> queryParams = Stream.of(query.split("&"))
                    .map(param -> param.split("=", 2))
                    .filter(param -> param.length >= 2)
                    .collect(Collectors.toMap(
                            entry -> URLDecoder.decode(entry[0], StandardCharsets.UTF_8),
                            entry -> URLDecoder.decode(entry[1], StandardCharsets.UTF_8),
                            (existing, replacement) -> existing
                    ));

            if (!queryParams.containsKey("X-Amz-Expires") || !queryParams.containsKey("X-Amz-Date")) {
                return new PresignedUrlInfo(null, false, nomeArquivo);
            }

            long expirationSeconds = Long.parseLong(queryParams.get("X-Amz-Expires"));
            String amzDate = queryParams.get("X-Amz-Date");

            LocalDateTime creationTime = LocalDateTime.parse(amzDate, AMZ_DATE_FORMATTER);
            LocalDateTime expirationTime = creationTime.plusSeconds(expirationSeconds);
            LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

            return new PresignedUrlInfo(expirationTime, now.isAfter(expirationTime), nomeArquivo);
        } catch (Exception e) {
            log.error("Não foi possível verificar expiração da URL pré-assinada: [{}] - {}", presignedUrl, e.getMessage());
            return new PresignedUrlInfo(LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0), true, nomeArquivo);
        }
    }

    @Override
    public String extractFileName(String presignedUrl) {
        if (StringUtils.isBlank(presignedUrl)) {
            return null;
        }

        Matcher matcher = USER_ID_FILE_PATTERN.matcher(presignedUrl);
        String rawName = null;
        if (matcher.find()) {
            rawName = matcher.group(1);
        } else {
            try {
                URL url = new URL(presignedUrl);
                String path = url.getPath();
                if (path != null && path.contains("/")) {
                    rawName = path.substring(path.lastIndexOf('/') + 1);
                }
            } catch (Exception ignored) {
            }
        }

        if (rawName == null) {
            return null;
        }

        String decodedName = rawName;
        try {
            decodedName = URLDecoder.decode(rawName, StandardCharsets.UTF_8);
        } catch (Exception e) {
            decodedName = rawName.replace("%20", " ");
        }
        if (decodedName.contains("%")) {
            decodedName = decodedName.replace("%", " ");
        }
        return decodedName;
    }
}
