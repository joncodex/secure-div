package com.enzelascripts.securediv.controller;

import com.enzelascripts.securediv.entity.Certificate;
import com.enzelascripts.securediv.entity.Transcript;
import com.enzelascripts.securediv.exception.StaleLinkException;
import com.enzelascripts.securediv.request.CertificateRequest;
import com.enzelascripts.securediv.request.DocumentDownloadRequest;
import com.enzelascripts.securediv.request.DocumentRevocationRequest;
import com.enzelascripts.securediv.request.TranscriptRequest;
import com.enzelascripts.securediv.response.CertificateResponse;
import com.enzelascripts.securediv.response.TranscriptResponse;
import com.enzelascripts.securediv.response.VerificationResponse;
import com.enzelascripts.securediv.service.CertificateService;
import com.enzelascripts.securediv.service.EmailService;
import com.enzelascripts.securediv.service.TranscriptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.flywaydb.database.postgresql.TransactionalModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;

@RestController
@RequestMapping("/api/v1/transcripts")
@RequiredArgsConstructor
public class TranscriptController {

    private final TranscriptService service;

    // Create a new transcript
    @PostMapping("/create")
    public ResponseEntity<TranscriptResponse> create(
            @Valid
            @RequestBody
            TranscriptRequest dto) {

        return ResponseEntity.ok(service.createTranscript(dto));

    }

    //Get a transcript
    @GetMapping("/{documentNumber}")
    public ResponseEntity<TranscriptResponse> get(
            @PathVariable
            String documentNumber) {

        TranscriptResponse response = service.getTranscript(documentNumber);
        return ResponseEntity.ok(response);
    }


    //verify a transcript
    @GetMapping("/verify/{documentNumber}")
    public ResponseEntity<VerificationResponse> verify(
            @PathVariable
            String documentNumber) {

        VerificationResponse response = service.verify(documentNumber);
        return ResponseEntity.ok(response);
    }

    //get a download url
    @PostMapping("/download")
    public ResponseEntity<String> download(@RequestBody DocumentDownloadRequest dto){

        String s3DownloadUrl = service.getTranscriptDownloadUrl(dto);
        String token = Base64.getEncoder().encodeToString(s3DownloadUrl.getBytes());
        String downloadUrl = "/api/v1/transcripts/download/" + token;

        //email download url to the right email address
        EmailService.notifyStudent(dto.getCompanyEmail(), downloadUrl);

        //return value will be changed later
        return ResponseEntity.ok(downloadUrl);
    }

    //download url
    @GetMapping("/download/{token}")
    private ResponseEntity<String> download(@PathVariable String token){
        byte[] downloadUrlBytes = Base64.getDecoder().decode(token);
        String downloadUrl = new String(downloadUrlBytes, StandardCharsets.UTF_8);

        String docNumber = downloadUrl.substring(0,11);
        Transcript trans = service.getTranscriptByDocumentNumber(docNumber);

        //only run if link has expired
        if(trans.getExpiresAt().isBefore(LocalDateTime.now()))
            throw new StaleLinkException("the link has expired. Request another one");

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(downloadUrl))
                .build();

    }

    //revoke a certificate
    @PutMapping("/revoke")
    public ResponseEntity<Void> revoke(
            @RequestBody
            DocumentRevocationRequest revoke) {

        service.revokeTranscript(revoke);
        return ResponseEntity.ok().build();
    }


}