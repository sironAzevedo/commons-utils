package com.br.azevedo.infra.aws;

import com.br.azevedo.infra.aws.s3.AwsS3StorageServiceImpl;
import com.br.azevedo.infra.aws.s3.PresignedUrlInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.CopyObjectResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AwsS3StorageServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner s3Presigner;

    private AwsProperties awsProperties;
    private AwsS3StorageServiceImpl s3StorageService;

    @BeforeEach
    void setUp() {
        awsProperties = new AwsProperties();
        awsProperties.getS3().setBucket("test-bucket");
        s3StorageService = new AwsS3StorageServiceImpl(s3Client, s3Presigner, awsProperties);
    }

    @Test
    @DisplayName("Deve gerar URL pré-assinada de upload com sucesso")
    void shouldGeneratePresignedUploadUrl() throws MalformedURLException {
        PresignedPutObjectRequest presignedResponse = mock(PresignedPutObjectRequest.class);
        when(presignedResponse.url()).thenReturn(new URL("https://s3.amazonaws.com/test-bucket/fatura.pdf"));
        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(presignedResponse);

        URL url = s3StorageService.generatePresignedUploadUrl("fatura.pdf", Duration.ofDays(7), "application/pdf");
        assertNotNull(url);
        assertEquals("https://s3.amazonaws.com/test-bucket/fatura.pdf", url.toString());
    }

    @Test
    @DisplayName("Deve gerar URL pré-assinada de download com sucesso")
    void shouldGeneratePresignedDownloadUrl() throws MalformedURLException {
        PresignedGetObjectRequest presignedResponse = mock(PresignedGetObjectRequest.class);
        when(presignedResponse.url()).thenReturn(new URL("https://s3.amazonaws.com/test-bucket/fatura.pdf"));
        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(presignedResponse);

        URL url = s3StorageService.generatePresignedDownloadUrl("fatura.pdf", Duration.ofDays(7));
        assertNotNull(url);
        assertEquals("https://s3.amazonaws.com/test-bucket/fatura.pdf", url.toString());
    }

    @Test
    @DisplayName("Deve verificar se arquivo existe no S3")
    void shouldCheckIfFileExists() {
        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenReturn(HeadObjectResponse.builder().build());

        assertTrue(s3StorageService.fileExists("fatura.pdf"));

        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenThrow(NoSuchKeyException.builder().message("Not found").build());

        assertFalse(s3StorageService.fileExists("fatura_inexistente.pdf"));
    }

    @Test
    @DisplayName("Deve mover e remover arquivo no S3")
    void shouldMoveAndRemoveFile() {
        when(s3Client.copyObject(any(CopyObjectRequest.class))).thenReturn(CopyObjectResponse.builder().build());
        when(s3Client.deleteObject(any(DeleteObjectRequest.class))).thenReturn(DeleteObjectResponse.builder().build());

        s3StorageService.moveFile("origem/fatura.pdf", "destino/fatura.pdf");

        verify(s3Client).copyObject(any(CopyObjectRequest.class));
        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    @DisplayName("Deve inspecionar expiração de URL pré-assinada")
    void shouldInspectPresignedUrlExpiration() {
        String validUrl = "https://test-bucket.s3.amazonaws.com/USER_123/fatura.pdf?X-Amz-Expires=86400&X-Amz-Date=20261021T100000Z";
        PresignedUrlInfo info = s3StorageService.isPresignedUrlExpired(validUrl);

        assertNotNull(info);
        assertEquals("fatura.pdf", info.nomeArquivo());
        assertNotNull(info.dataExpiracao());
    }

    @Test
    @DisplayName("Deve extrair nome de arquivo decodificado")
    void shouldExtractDecodedFileName() {
        String url = "https://test-bucket.s3.amazonaws.com/USER_123/minha%20fatura.pdf?X-Amz-Expires=86400";
        String fileName = s3StorageService.extractFileName(url);
        assertEquals("minha fatura.pdf", fileName);
    }
}
