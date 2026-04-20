package com.enzelascripts.securediv.controller;

import com.enzelascripts.securediv.entity.AccessLog;
import com.enzelascripts.securediv.request.DocumentRevocationRequest;
import com.enzelascripts.securediv.response.AdminDocumentSummary;
import com.enzelascripts.securediv.response.DashboardStats;
import com.enzelascripts.securediv.response.StudentResponse;
import com.enzelascripts.securediv.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/admins")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStats> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @GetMapping("/certificates")
    public ResponseEntity<List<AdminDocumentSummary>> getAllCertificates() {
        return ResponseEntity.ok(adminService.getAllCertificates());
    }

    @GetMapping("/transcripts")
    public ResponseEntity<List<AdminDocumentSummary>> getAllTranscripts() {
        return ResponseEntity.ok(adminService.getAllTranscripts());
    }

    @PutMapping("/certificates/revoke")
    public ResponseEntity<String> revokeCertificate(@Valid @RequestBody DocumentRevocationRequest request) {
        adminService.revokeCertificate(request);
        return ResponseEntity.ok("Certificate " + request.getDocumentNumber() + " revoked successfully");
    }

    @PutMapping("/transcripts/revoke")
    public ResponseEntity<String> revokeTranscript(@Valid @RequestBody DocumentRevocationRequest request) {
        adminService.revokeTranscript(request);
        return ResponseEntity.ok("Transcript " + request.getDocumentNumber() + " revoked successfully");
    }

    @GetMapping("/access-logs")
    public ResponseEntity<List<AccessLog>> getAccessLogs() {
        return ResponseEntity.ok(adminService.getAccessLogs());
    }

    @GetMapping("/students")
    public ResponseEntity<List<StudentResponse>> getAllStudents() {
        return ResponseEntity.ok(adminService.getAllStudents());
    }
}
