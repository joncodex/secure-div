package com.enzelascripts.securediv.service;

import com.enzelascripts.securediv.entity.AccessLog;
import com.enzelascripts.securediv.entity.Document;
import com.enzelascripts.securediv.entity.InstitutionRecord;
import com.enzelascripts.securediv.entity.Student;
import com.enzelascripts.securediv.repository.AccessLogRepo;
import com.enzelascripts.securediv.repository.CertificateRepo;
import com.enzelascripts.securediv.repository.StudentRepo;
import com.enzelascripts.securediv.repository.TranscriptRepo;
import com.enzelascripts.securediv.request.DocumentRevocationRequest;
import com.enzelascripts.securediv.response.AdminDocumentSummary;
import com.enzelascripts.securediv.response.DashboardStats;
import com.enzelascripts.securediv.response.StudentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final CertificateRepo certificateRepo;
    private final TranscriptRepo transcriptRepo;
    private final StudentRepo studentRepo;
    private final AccessLogRepo accessLogRepo;
    private final CertificateService certificateService;
    private final TranscriptService transcriptService;
    private final StudentService studentService;

    public DashboardStats getDashboardStats() {
        return DashboardStats.builder()
                .totalStudents(studentRepo.count())
                .totalCertificates(certificateRepo.count())
                .totalTranscripts(transcriptRepo.count())
                .revokedCertificates(certificateRepo.countByIsRevoked(true))
                .revokedTranscripts(transcriptRepo.countByIsRevoked(true))
                .totalAccessLogs(accessLogRepo.count())
                .build();
    }

    public List<AdminDocumentSummary> getAllCertificates() {
        return certificateRepo.findAll().stream()
                .map(cert -> toAdminSummary(cert, "CERTIFICATE"))
                .toList();
    }

    public List<AdminDocumentSummary> getAllTranscripts() {
        return transcriptRepo.findAll().stream()
                .map(transcript -> toAdminSummary(transcript, "TRANSCRIPT"))
                .toList();
    }

    public void revokeCertificate(DocumentRevocationRequest request) {
        certificateService.revokeCertificate(request);
    }

    public void revokeTranscript(DocumentRevocationRequest request) {
        transcriptService.revokeTranscript(request);
    }

    public List<AccessLog> getAccessLogs() {
        return accessLogRepo.findAll();
    }

    public List<StudentResponse> getAllStudents() {
        return studentService.getAllStudents();
    }

    private AdminDocumentSummary toAdminSummary(Document document, String docType) {
        Student student = document.getStudent();
        InstitutionRecord institution = document.getInstitutionRecord();

        return AdminDocumentSummary.builder()
                .documentNumber(document.getDocumentNumber())
                .documentType(docType)
                .studentId(student.getStudentId())
                .studentName(student.getFirstName() + " " + student.getLastName())
                .degree(document.getDegree())
                .course(document.getCourse())
                .classOfDegree(document.getClassOfDegree())
                .cgpaValue(document.getCgpaValue())
                .graduationDate(document.getGraduationDate())
                .institutionName(institution.getInstitutionName())
                .issuedAt(document.getIssuedAt())
                .revoked(document.isRevoked())
                .revocationReason(document.getRevocationReason())
                .revokedAt(document.getRevokedAt())
                .revokedBy(document.getRevokedBy())
                .build();
    }
}
