package com.enzelascripts.securediv.service;

import com.enzelascripts.securediv.dto.WebhookPayload;
import com.enzelascripts.securediv.entity.Document;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class WebhookService {
    @Value("${webhook-secret}")
    private String secret;


    public <T extends Document> void sendWebhook(String url, T document){
        WebhookPayload payload = WebhookPayload.builder()
                .documentType(document.getDocumentType())
                .documentNumber(document.getDocumentNumber())
                .studentFirstName(document.getStudent().getFirstName())
                .studentLastName(document.getStudent().getLastName())
                .email(document.getStudent().getEmail())
                .timeStamp(LocalDateTime.now())
                .build();

        sendWebhook(url, payload);

    }

    public void sendWebhook(String url, WebhookPayload payload){
        String jsonPayload;

        try {
            ObjectMapper json = new ObjectMapper();
            jsonPayload = json.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        sendWebhook(url, jsonPayload);

    }

    public void sendWebhook(String url, String payload){

        //Sign the payload
        String signedPayload = signPayload(payload);

        //build the header
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("X-Signature", signedPayload);

        HttpEntity <String> entity = new HttpEntity<>(payload, headers);

        try {
            //Build the restTemplate
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(5000);
            factory.setReadTimeout(5000);
            RestTemplate restTemplate = new RestTemplate(factory);
            restTemplate.postForEntity(url, entity, String.class);
        } catch (RestClientException e) {
            throw new RuntimeException("webhook failed", e);
        }

    }

    private String signPayload(String payload){

        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(), "HmacSHA256"));
            byte[] raw = mac.doFinal(payload.getBytes());

            return Base64.getEncoder().encodeToString(raw);

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);}

    }


}
