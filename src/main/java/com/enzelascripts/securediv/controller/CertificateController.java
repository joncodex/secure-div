package com.enzelascripts.securediv.controller;

import com.enzelascripts.securediv.entity.Certificate;
import com.enzelascripts.securediv.request.CertificateRequest;
import com.enzelascripts.securediv.response.CertificateResponse;
import com.enzelascripts.securediv.service.CertificateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/certificates")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService service;

    // Create a new certificate
    @PostMapping("/create")
    public ResponseEntity<String> create(
            @Valid
            @RequestBody
            CertificateRequest dto) {

        String downloadUrl = service.createCertificate(dto);
        return ResponseEntity.ok(downloadUrl);

    }

    //verify a certificate
    @GetMapping("/verify/{certificateNumber}")
    public ResponseEntity<CertificateResponse> get(
            @PathVariable
            String certificateNumber) {

        CertificateResponse response = service
                .getCertificateResponseObject(
                        service.getCertificateByCertificateNumber(certificateNumber));

        return ResponseEntity.ok(response);
    }

    //revoke a certificate
    @DeleteMapping("/delete/{certificateNumber}")
    public ResponseEntity<Void> delete(
            @PathVariable
            String certificateNumber) {

        service.revokeCertificate(certificateNumber);
        return ResponseEntity.ok().build();
    }


}