package com.enzelascripts.securediv.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {
    private long totalStudents;
    private long totalCertificates;
    private long totalTranscripts;
    private long revokedCertificates;
    private long revokedTranscripts;
    private long totalAccessLogs;
}
