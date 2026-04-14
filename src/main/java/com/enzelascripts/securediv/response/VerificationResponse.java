package com.enzelascripts.securediv.response;

import jakarta.persistence.Column;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationResponse {
    
    private String status;              // "VALID", "REVOKED", "EXPIRED", "NOT_FOUND"
    @Column(columnDefinition = "TEXT")
    private String message;
    
    private String documentNumber;
    private String documentType;        // "CERTIFICATE", "TRANSCRIPT"
    
    // Student info (limited for privacy)
    private String studentName;
    private String studentNumber;
    
    // Academic info
    private String degree;
    private String course;
    private String classOfDegree;
    private String cgpaValue;         // "5.3"
    
    private LocalDate graduationDate;
    private String institutionName;
    
    // Security
    private LocalDateTime issuedAt;
    private LocalDateTime verifiedAt;   // When this verification occurred
    
    // Revocation info (if applicable)
    private boolean isRevoked;
    @Column(columnDefinition = "TEXT")
    private String revocationReason;
    private LocalDateTime revokedAt;
}