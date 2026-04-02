package com.enzelascripts.securediv.controller;

import com.enzelascripts.securediv.request.SignatoryRequest;
import com.enzelascripts.securediv.response.SignatoryResponse;
import com.enzelascripts.securediv.service.SignatoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/signatories")
@RequiredArgsConstructor
public class SignatoryController {

    private final SignatoryService service;

    // Create new signatory
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createSignatory(
            @Valid
            @ModelAttribute
            SignatoryRequest dto) {

        Map<String, Object> response = service.createSignatory(dto);
        return ResponseEntity.ok(response);

    }

    // Get current signatory
    @GetMapping("/current")
    public ResponseEntity<List<SignatoryResponse>> getCurrentSignatories() {

        return ResponseEntity.ok(service.getCurrentSignatories());
    }

    //invalidate signatory
    @PutMapping("/invalidate/{name}")
    public ResponseEntity<Void> invalidateSignatories(@PathVariable String name) {
        service.invalidateSignatories(name);
        return ResponseEntity.ok().build();
    }

}