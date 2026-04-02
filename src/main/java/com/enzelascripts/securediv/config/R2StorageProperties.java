package com.enzelascripts.securediv.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "r2")
@Getter
@Setter
public class R2StorageProperties {

    private String endpoint;
    private String accessKeyId;
    private String secretAccessKey;
    private String bucketName;

}