package com.enzelascripts.securediv.request;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateIssueRequest extends DocumentIssueRequest {
    
    // Certificate-specific: no course results needed
    // Uses base fields only
}