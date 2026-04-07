package com.enzelascripts.securediv.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDownloadRequest {
    
    @NotBlank(message = "Document number is required")
    private String documentNumber;
    
    @NotBlank(message = "Requester email is required")
    @Email(message = "Valid email required")
    private String requesterEmail;
    
    private String requesterName;     // For logging
    private String purpose;             // "EMPLOYMENT", "ADMISSION", etc.
}