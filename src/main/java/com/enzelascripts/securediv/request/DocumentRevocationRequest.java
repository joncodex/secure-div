package com.enzelascripts.securediv.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentRevocationRequest {
    
    @NotBlank(message = "Revocation reason is required")
    private String reason;
    
    @NotBlank(message = "Admin ID is required")
    private String revokedBy;           // Admin username/ID
    
    private boolean notifyStudent = true;
    
    private String notificationMessage; // Custom message to student
    
    private boolean deleteFromStorage = false;  // Also delete S3 file?
}