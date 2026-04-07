package com.enzelascripts.securediv.controller;

import com.enzelascripts.securediv.request.InstitutionRecordRequest;
import com.enzelascripts.securediv.entity.InstitutionRecord;
import com.enzelascripts.securediv.response.InstitutionResponse;
import com.enzelascripts.securediv.service.InstitutionRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/institutions")
@RequiredArgsConstructor
public class InstitutionRecordController {

    @Autowired
    private InstitutionRecordService service;

    // Create new institution record
    @Operation(summary = "Create a new institution record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Institution created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createInstitution(
            @Valid
            @ModelAttribute
            InstitutionRecordRequest dto) {

        Map<String, Object> response = service.createInstitutionRecord(dto);
        return ResponseEntity.ok(response);

    }

    //get current institution record
    @Operation(summary = "Get current institution record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Institution retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "No institution found")
    })
    @GetMapping("/current")
    public ResponseEntity<InstitutionResponse> getCurrentInstitution() {

        return ResponseEntity.ok(
                service.getCurrentInstitutionRecord());
    }

}



//postman details

//api: "/api/v1/institutions/create"

//header: Content-Type: multipart/form-data

//institutionName = University of Lagos
//address = Akoka, Lagos
//logoImage = [file upload]
//motto = Knowledge for Service
//email = info@unilag.edu.ng
//phoneNumber = +2348012345678
//website = https://www.unilag.edu.ng