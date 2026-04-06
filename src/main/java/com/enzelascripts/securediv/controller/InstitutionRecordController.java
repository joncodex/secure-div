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

    //get current institution record
    @GetMapping("/current")
    public ResponseEntity<InstitutionResponse> getCurrentInstitution() {

        return ResponseEntity.ok(
                service.getCurrentInstitutionRecord());
    }

}