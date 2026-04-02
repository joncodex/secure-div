package com.enzelascripts.securediv.controller;

import com.enzelascripts.securediv.request.InstitutionRecordRequest;
import com.enzelascripts.securediv.entity.InstitutionRecord;
import com.enzelascripts.securediv.response.InstitutionResponse;
import com.enzelascripts.securediv.service.InstitutionRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/institutions")
@RequiredArgsConstructor
public class InstitutionRecordController {

    private final InstitutionRecordService service;

    // Create new institution record
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createInstitution(
            @Valid
            @ModelAttribute
            InstitutionRecordRequest dto) {

        Map<String, Object> response = service.createInstitutionRecord(dto);
        return ResponseEntity.ok(response);

    }

    // Get current institution record
    @GetMapping("/current")
    public ResponseEntity<InstitutionResponse> getCurrentInstitution() {
        InstitutionRecord record = service.getCurrentInstitutionRecord();
        InstitutionResponse response = InstitutionResponse.builder()
                .institutionName(record.getInstitutionName())
                .address(record.getAddress())
                .logoUrl(record.getLogoUrl())
                .motto(record.getMotto())
                .email(record.getEmail())
                .phoneNumber(record.getPhoneNumber())
                .website(record.getWebsite())
                .build();
        return ResponseEntity.ok(response);
    }

}