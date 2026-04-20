package com.enzelascripts.securediv.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDocumentSummary {
    private String documentNumber;
    private String documentType;
    private String studentId;
    private String studentName;
    private String degree;
    private String course;
    private String classOfDegree;
    private Double cgpaValue;
    private LocalDate graduationDate;
    private String institutionName;
    private LocalDateTime issuedAt;
    private boolean revoked;
    private String revocationReason;
    private LocalDateTime revokedAt;
    private String revokedBy;
}
