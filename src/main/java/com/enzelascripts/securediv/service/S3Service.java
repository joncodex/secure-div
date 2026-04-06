package com.enzelascripts.securediv.service;

import com.enzelascripts.securediv.config.S3StorageProperties;
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
    private S3StorageProperties s3Storage;
    @Autowired
    private S3Client s3Client;

//  ============================================ public methods ========================================================

    public void revokeCertificateOnS3(String s3Key) {

        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(s3Storage.getBucketName())
                .key(s3Key)
                .build());
        log.info("Revoked certificate {} from S3", s3Key.split("/")[1]);
    }

    public String getCertificateDownloadUrl(String s3Key) {

        return getPresignedDownloadUrl(s3Key);
    }

    public String getTranscriptDownloadUrl(String s3Key) {

        return getPresignedDownloadUrl(s3Key);
    }

    public byte[] getLogoAsBytes(String s3Key) {

        return getS3Object(s3Key);
    }

    public byte[] getSignatureAsBytes(String s3Key) {

        return getS3Object(s3Key);
    }

    public byte[] getQrCodeAsBytes(String s3Key) {

        return getS3Object(s3Key);
    }

    public void uploadCertificate(byte[] pdfBytes, String s3Key){

        upload(pdfBytes, s3Key);
    }

    public void uploadTranscript(byte[] pdfBytes, String s3Key){

        upload(pdfBytes, s3Key);
    }

    public void uploadLogo(byte[] imageBytes, String s3Key) {

        upload(imageBytes, s3Key);
    }

    public void uploadSignature(byte[] imageBytes, String s3Key) {

        upload(imageBytes, s3Key);
    }

    public void uploadQrCode(byte[] imageBytes, String s3Key) {

        upload(imageBytes, s3Key);
    }


//  ============================================ helper methods ========================================================

    private String getPresignedDownloadUrl(String key) {
        //build the presigner object
        @Cleanup
        S3Presigner presigner = S3Presigner.builder()
                .region(Region.of("auto"))
                .endpointOverride(URI.create(s3Storage.getEndpoint()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(
                                s3Storage.getAccessKeyId(),
                                s3Storage.getSecretAccessKey()
                        ))
                )
                .build();

        //Build the request for the object to be downloaded
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(s3Storage.getBucketName())
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

        return presignedRequest.url().toString();

    }

    private void upload(byte[] pdfBytes, String s3Key){

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(s3Storage.getBucketName())
                .key(s3Key)
                .contentType("application/pdf")
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(pdfBytes));
        log.info("Uploaded a certificate {} to S3", s3Key.split("/")[1]);

    }

    private byte[] getS3Object(String key){
        return s3Client.getObjectAsBytes(GetObjectRequest.builder()
                .bucket(s3Storage.getBucketName())
                .key(key)
                .build()).asByteArray();
    }


}
