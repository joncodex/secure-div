package com.enzelascripts.securediv.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDownloadRequest {
    
    @NotBlank(message = "Document number is required")
    private String documentNumber;

    private String studentId;           //studentId if it is the student

    @Email(message = "Valid email required")
    private String companyEmail;      //official email if a corporate body
    
    private String requesterName;       // For logging

    @Size(max = 60)
    private String purpose;             // "EMPLOYMENT", "ADMISSION", etc.
}