package com.enzelascripts.securediv.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;
import java.util.Objects;

@Component
public class BeansConfig {
    @Autowired
    private R2StorageProperties r2;

    @Bean
    public S3Client getS3Client(){

        return S3Client.builder()
                .region(Region.of("auto"))
                .endpointOverride(URI.create(Objects.requireNonNull(r2).getEndpoint()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(
                                r2.getAccessKeyId(),
                                r2.getSecretAccessKey()
                        ))
                )
                .build();
    }




}
