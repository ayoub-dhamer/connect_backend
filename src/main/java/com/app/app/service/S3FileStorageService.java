package com.app.app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Condition;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

@Service
@Conditional(S3FileStorageService.S3Enabled.class)
public class S3FileStorageService implements FileStorageService {

    private final S3Client s3Client;

    @Value("${storage.s3.bucket:}")
    private String bucket;

    @Value("${storage.s3.public-url:}")
    private String publicUrlBase;

    public S3FileStorageService(
            @Value("${storage.s3.endpoint:}") String endpoint,
            @Value("${storage.s3.region:us-east-1}") String region,
            @Value("${storage.s3.access-key:}") String accessKey,
            @Value("${storage.s3.secret-key:}") String secretKey) {

        var builder = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)));

        if (endpoint != null && !endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint)).forcePathStyle(true);
        }

        this.s3Client = builder.build();
    }

    @Override
    public String upload(MultipartFile file, String folder) throws IOException {
        String extension = getExtension(file.getOriginalFilename());
        String key = folder + "/" + UUID.randomUUID() + extension;

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(file.getContentType())
                        .build(),
                RequestBody.fromInputStream(file.getInputStream(), file.getSize())
        );

        return publicUrlBase + "/" + key;
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.'));
    }

    /** Only registers this bean when storage.provider=s3 is explicitly set. */
    static class S3Enabled implements Condition {
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            String provider = context.getEnvironment().getProperty("storage.provider", "local");
            return "s3".equalsIgnoreCase(provider);
        }
    }
}