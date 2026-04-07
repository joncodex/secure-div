package com.enzelascripts.securediv.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;


@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentIssueRequest {

    @NotBlank(message = "Student ID is required")
    private String studentId;

    @NotBlank(message = "Degree is required")
    private String degree;

    @NotBlank(message = "Course is required")
    private String course;

    private String classOfDegree;   // Optional: "First-Class Honors"

    private Double cgpaValue;
    private Double cgpaScale;       // "4.0", "5.0", "7.0"

    @NotNull(message = "Graduation date is required")
    @PastOrPresent(message = "Graduation date cannot be in future")
    private LocalDate graduationDate;

    @NotBlank(message = "Institution ID is required")
    private String institutionId;

    @NotEmpty(message = "At least one signatory is required")
    private List<String> signatoryIds;  // List of signatory UUIDs

    @NotBlank(message = "Document type is required")
    @Pattern(regexp = "CERTIFICATE|TRANSCRIPT", message = "Type must be CERTIFICATE or TRANSCRIPT")
    private String documentType;

    // For TRANSCRIPT only
    private List<CourseResultRequest> courseResults;
}

