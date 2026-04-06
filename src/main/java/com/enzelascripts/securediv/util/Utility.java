package com.enzelascripts.securediv.util;

import com.enzelascripts.securediv.exception.BadInputException;
import com.enzelascripts.securediv.exception.DownloadException;
import com.enzelascripts.securediv.exception.ImageConversionException;
import com.enzelascripts.securediv.exception.OperationalException;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@Slf4j
public class Utility {
/// ============================================ Fields ================================================================
    @Value("${base-url}")
    public static String BASE_URL;
    public static final String CERTIFICATE_VERIFICATION_URL = BASE_URL+"/api/v1/certificates/verify";
    public static final String TRANSCRIPT_VERIFICATION_URL = BASE_URL+"/api/v1/transcripts/verify";


/// ========================================== public methods ==========================================================
    public static byte[] getFileBytes(MultipartFile file){
        try {
            return file.getBytes();
        } catch (IOException e) {
            log.error("Failed to convert file to bytes", e);
            throw new RuntimeException("Failed to convert file " + file.getName() + " to bytes");
        }
    }

    public static String generateQRCode(String verificationUrl) {
        int width = 200;
        int height = 200;
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(verificationUrl, BarcodeFormat.QR_CODE, width, height);

            //paint a canvas of the QR grid
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y< height; y++) {
                    image.setRGB(x, y, bitMatrix.get(x, y)? 0x000000 : 0xFFFFFF);
                }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);

            return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());

        } catch (WriterException | IOException e) {
            log.debug("Failed to generate QR code", e);
            throw new OperationalException("Failed to generate QR code");
        }
    }

    public static String convertToBase64ImageUrl(MultipartFile file) {

        String mimeType = validateNotNull(file).getContentType();
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            assert mimeType != null;
            ImageIO.write(image, mimeType.split("/")[1], baos);
            baos.flush();
            baos.close();

            return "data:" + mimeType + ";base64,"
                    + Base64.getEncoder().encodeToString(baos.toByteArray());

        } catch (IOException e) {
            log.error("Failed to convert image to base64", e);
            throw new ImageConversionException("Failed to convert image to base64");
        }
    }

    public static String getFileFingerprint (byte[] pdfBytes) {
        try {

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(pdfBytes);
            return Base64.getEncoder().encodeToString(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algorithm error occurred while generating the file's fingerprint", e);
        }
    }

    public static byte[] downloadAsByteArray(S3Client s3, String s3Key, String bucketName) {

        GetObjectRequest objectToGet = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();

        try {
            return s3.getObject(objectToGet).readAllBytes();
        } catch (S3Exception | IOException e) {
            log.error("Failed to download document from S3", e);
            throw new DownloadException("Failed to download document " + s3Key + " from S3");
        }

    }

    public static String uploadImageFile(MultipartFile file, S3Client s3Client, String bucketName) {
        // 1. Validate the file
        if (!Objects.requireNonNull(
                file.getContentType()).startsWith("image/jpg")
                || !file.getContentType().startsWith("image/jpeg")
                || !file.getContentType().startsWith("image/png")
                || file.isEmpty()) {

            throw new BadInputException("Invalid file. File must be PNG or JPEG");
        }

        String extension = file.getContentType().split("/")[1] ;

        // 2. Generate storage name
        String date = String.valueOf(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        String key = "logo/logo." + date + "." + extension;

        try {
            // 3. Upload to S3
            PutObjectRequest metadata = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .contentLength(file.getSize())
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            RequestBody requestBody = RequestBody.fromBytes(file.getBytes());


            s3Client.putObject(metadata, requestBody);

        } catch (IOException e) {
            log.error("Failed to upload file", e);
            throw new RuntimeException("Failed to upload file", e);
        }

        // 4. Return storage key
        return key;
    }

    public static boolean isFileCorrupted(byte[] fileBytes, String fingerprint) {

        byte[] fileFingerprint = Base64.getDecoder().decode(fingerprint);

        return !MessageDigest.isEqual(fileBytes, fileFingerprint);

    }

    public static String get12AlphaNumString() {
        Random random = new SecureRandom();
        long randomNum = random.nextLong(1_000_000, 10_000_000);

        String alphabets = "QPWEALKMSNDBJVCUXRTYFGHZ";

        StringBuilder stringBuilder = new StringBuilder();

        // for 5 random characters
//        for (int i = 0; i < 5; i++) {
//            char randomCharacter = alphabets.charAt(random.nextInt(alphabets.length()));
//            stringBuilder.append(randomCharacter);
//        }
//        stringBuilder.insert(4, randomNum);

        //for 4 known characters and 1 random character
        stringBuilder.append("FIPS");
        stringBuilder.append(randomNum);

        char randomCharacter = alphabets.charAt(random.nextInt(alphabets.length()));
        stringBuilder.append(randomCharacter);

        return stringBuilder.toString();
    }

    public static <U, T> T transferData(@NonNull U from, @NonNull T to) {

        Field[] fromFields = from.getClass().getDeclaredFields();
        Field[] toFields = to.getClass().getDeclaredFields();

        for (Field fromField : fromFields) {
            fromField.setAccessible(true);

            for (Field toField : toFields) {
                toField.setAccessible(true);

                if (fromField.getName().equals(toField.getName()) &&
                        fromField.getType().equals(toField.getType())) {

                    try {
                        Object value = fromField.get(from);

                        // prevent null overwrite
                        if (value != null) {

                            //if a List, make your own copy of the List's content; instead of referencing it
                            if(value instanceof List<?> list) value = new ArrayList<>((list));

                            //set the value of the target object
                            toField.set(to, value);
                        }

                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(
                                "Failed to copy field: " + fromField.getName(), e
                        );
                    }

                    break;
                }
            }
        }

        return to;
    }

    public static <T> T validateNotNull(T objectToValidate) {

        return validateNotNull(objectToValidate, null);
    }

    public static<T> T validateNotNull(T objectToValidate, String errorMessage) {

        String message = (errorMessage == null) || (errorMessage.isBlank())?
                "can not be null" : errorMessage;

        if(objectToValidate instanceof String obj && (obj.isBlank()))
            throw new NullPointerException(message);

        return Objects.requireNonNull(objectToValidate,() -> message );
    }




}
