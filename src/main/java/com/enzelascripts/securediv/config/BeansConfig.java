package com.enzelascripts.securediv.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

import java.net.URI;
import java.util.Objects;

@Component
public class BeansConfig {
    @Autowired
    private S3StorageProperties s3Storage;

    @Bean
    public S3Client getS3Client(){
        S3ClientBuilder builder = S3Client.builder()
                .endpointOverride(URI.create(s3Storage.getEndpoint()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(
                                s3Storage.getAccessKeyId(),
                                s3Storage.getSecretAccessKey())))
                .region(Region.of(s3Storage.getRegion()));

            // Conditionally apply path-style (only for MinIO)
            if (s3Storage.isPathStyleEnabled()) {
                builder.forcePathStyle(true);
            }

            return builder.build();
        }

}




