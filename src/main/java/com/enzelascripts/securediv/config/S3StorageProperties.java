package com.enzelascripts.securediv.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "s3")
@Getter
@Setter
public class S3StorageProperties {

    private String endpoint;
    private String accessKeyId;
    private String secretAccessKey;
    private String bucketName;
    private String region;
    private boolean pathStyleEnabled = false; // default false for R2


}