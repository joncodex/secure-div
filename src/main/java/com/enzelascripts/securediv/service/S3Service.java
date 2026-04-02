package com.enzelascripts.securediv.service;

import com.enzelascripts.securediv.config.R2StorageProperties;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.URI;
import java.time.Duration;

@Service
@Slf4j
public class S3Service {
    @Autowired
    private R2StorageProperties r2;
    @Autowired
    private S3Client s3;



    public void revokeCertificateOnS3(String certificateNumber) {
        String key = "certificates/" + certificateNumber + ".pdf";
        s3.deleteObject(DeleteObjectRequest.builder()
                .bucket(r2.getBucketName())
                .key(key)
                .build());
        log.info("Revoked certificate {} from S3", certificateNumber);
    }

    public String getDownloadUrl(String certificateNumber) {

        String key = "certificates/" + certificateNumber + ".pdf";

        //build the presigner object
        @Cleanup
        S3Presigner presigner = S3Presigner.builder()
                .region(Region.of("auto"))
                .endpointOverride(URI.create(r2.getEndpoint()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(
                                r2.getAccessKeyId(),
                                r2.getSecretAccessKey()
                        ))
                )
                .build();

        //Build the request for the object to be downloaded
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(r2.getBucketName())
                .key(key)
                .build();

        //config/set the presign request to get the object
        GetObjectPresignRequest presignRequest =
                GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(10)) // expiry
                        .getObjectRequest(getObjectRequest)
                        .build();

        //call the PresignedGetObjectRequest
        PresignedGetObjectRequest presignedRequest =
                presigner.presignGetObject(presignRequest);

        // the url expires after 10 minutes
        String downloadUrl = presignedRequest.url().toString();

        //This Method accepts url,
        //call the download url as a client
        //hash the file downloaded
        //compare the hash with the one in DB
        //If all is good, return downloadUrl else throw fileCorrupted exception
        return downloadUrl;

    }

    public void upload(byte[] pdfBytes, String certificateNumber){

        String key = "certificates/" + certificateNumber + ".pdf";

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(r2.getBucketName())
                .key(key)
                .contentType("application/pdf")
                .build();

        s3.putObject(request, RequestBody.fromBytes(pdfBytes));
        log.info("Uploaded a certificate {} to S3", certificateNumber);
    }
}
