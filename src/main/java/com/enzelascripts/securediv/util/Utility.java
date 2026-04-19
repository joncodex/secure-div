package com.enzelascripts.securediv.util;

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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;

@Component
@Slf4j
public class Utility {
    // ============================================ Fields ================================================================
    @Value("${base-url}")
    public static String BASE_URL;

    @Value("${webhook-url}")
    public static String WEBHOOKURL;

    public static final int PRESIGNED_DURATION = 15;
    public static final String CERTIFICATE_VERIFICATION_URL = BASE_URL+"/api/v1/certificates/verify";
    public static final String TRANSCRIPT_VERIFICATION_URL = BASE_URL+"/api/v1/transcripts/verify";

    // ========================================== public methods ==========================================================
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

    public static boolean isFileCorrupted(byte[] fileBytes, String fingerprint) {

        byte[] fileFingerprint = Base64.getDecoder().decode(fingerprint);

        return !MessageDigest.isEqual(fileBytes, fileFingerprint);

    }

    public static String get12AlphaNumString(String initial4Letters) {
        Random random = new SecureRandom();
        long randomNum = random.nextLong(1_000_000, 10_000_000);

        String alphabets = "QPWEALKMSNDBJVCUXRTYFGHZ";
        char randomCharacter = alphabets.charAt(random.nextInt(alphabets.length()));

        StringBuilder stringBuilder = new StringBuilder();

        if(initial4Letters.length()>4) initial4Letters =
                initial4Letters.substring(0, 4).toUpperCase();

        stringBuilder.append(initial4Letters);
        stringBuilder.append("-");
        stringBuilder.append(randomNum);
        stringBuilder.append(randomCharacter);

        return stringBuilder.toString();
    }

    public static <U, T> T transferData(@NonNull U from, @NonNull T to) {

        List<Field> fromFields = getAllFields(from.getClass());
        List<Field> toFields = getAllFields(to.getClass());

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
                            if(value instanceof List<?> list)
                                value = new ArrayList<>((list));

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

    private static List<Field> getAllFields(Class<?> type){
        List<Field> listOfAllFields = new ArrayList<>();

        while(type != null){
            listOfAllFields.addAll(Arrays.asList(type.getDeclaredFields()));
            type = type.getSuperclass();
        }

        return listOfAllFields;
    }



}
