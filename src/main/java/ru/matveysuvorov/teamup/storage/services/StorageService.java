package ru.matveysuvorov.teamup.storage.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;

@Service
public class StorageService {

    private final S3Client client;
    private final S3Presigner presigner;

    @Value("${storage.s3.bucket}")
    private String bucket;

    public StorageService(
            S3Client client, S3Presigner presigner
    ) {
        this.client = client;
        this.presigner = presigner;
    }

    public void upload(MultipartFile file, String path) {
        try {
            PutObjectRequest request = PutObjectRequest
                    .builder()
                    .bucket(bucket)
                    .key(path)
                    .contentType(file.getContentType())
                    .build();

            client.putObject(
                    request,
                    RequestBody.fromInputStream(
                            file.getInputStream(),
                            file.getSize()
                    )
            );

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file");
        }
    }

    public void delete(String path) {
        DeleteObjectRequest request = DeleteObjectRequest
                .builder()
                .bucket(bucket)
                .key(path)
                .build();

        client.deleteObject(request);
    }

    public String getPresignedUrl(String key) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofHours(1))
                .getObjectRequest(request)
                .build();

        return presigner.presignGetObject(presignRequest).url().toString();
    }

    public byte[] download(String path) {
        GetObjectRequest request = GetObjectRequest
                .builder()
                .key(path)
                .build();

        ResponseBytes<GetObjectResponse> response = client.getObjectAsBytes(request);

        return response.asByteArray();
    }


}
