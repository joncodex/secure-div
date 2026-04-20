package com.enzelascripts.securediv.controller;

import com.enzelascripts.securediv.dto.WebhookPayload;
import com.enzelascripts.securediv.service.WebhookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookService webhookService;
    private final ObjectMapper objectMapper;

    @PostMapping("/receive")
    public ResponseEntity<String> receive(
            @RequestBody String rawPayload,
            @RequestHeader("X-Signature") String signature) {

        if (!webhookService.verifySignature(rawPayload, signature)) {
            log.warn("Webhook rejected: invalid signature");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
        }

        try {
            WebhookPayload payload = objectMapper.readValue(rawPayload, WebhookPayload.class);
            log.info("Webhook received — document: {}, type: {}, student: {} {}",
                    payload.getDocumentNumber(),
                    payload.getDocumentType(),
                    payload.getStudentFirstName(),
                    payload.getStudentLastName());

            // TODO: add further processing here (e.g. trigger email, update status)

        } catch (Exception e) {
            log.error("Failed to parse webhook payload", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Malformed payload");
        }

        return ResponseEntity.ok("Webhook received");
    }
}
