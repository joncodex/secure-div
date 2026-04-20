package com.enzelascripts.securediv.service;

import com.enzelascripts.securediv.entity.AccessLog;
import com.enzelascripts.securediv.entity.Certificate;
import com.enzelascripts.securediv.entity.InstitutionRecord;
import com.enzelascripts.securediv.entity.Student;
import com.enzelascripts.securediv.entity.Transcript;
import com.enzelascripts.securediv.repository.AccessLogRepo;
import com.enzelascripts.securediv.repository.CertificateRepo;
import com.enzelascripts.securediv.repository.StudentRepo;
import com.enzelascripts.securediv.repository.TranscriptRepo;
import com.enzelascripts.securediv.request.DocumentRevocationRequest;
import com.enzelascripts.securediv.response.AdminDocumentSummary;
import com.enzelascripts.securediv.response.DashboardStats;
import com.enzelascripts.securediv.response.StudentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock private CertificateRepo certificateRepo;
    @Mock private TranscriptRepo transcriptRepo;
    @Mock private StudentRepo studentRepo;
    @Mock private AccessLogRepo accessLogRepo;
    @Mock private CertificateService certificateService;
    @Mock private TranscriptService transcriptService;
    @Mock private StudentService studentService;

    @InjectMocks
    private AdminService adminService;

    private Student student;
    private InstitutionRecord institution;
    private Certificate certificate;
    private Transcript transcript;

    @BeforeEach
    void setUp() {
        student = new Student();
        student.setId(1L);
        student.setStudentId("STU-001");
        student.setFirstName("John");
        student.setLastName("Doe");

        institution = new InstitutionRecord();
        institution.setInstitutionName("Test University");
        institution.setAddress("123 University Ave");

        certificate = new Certificate();
        certificate.setDocumentNumber("CERT-001");
        certificate.setDegree("BSc");
        certificate.setCourse("Computer Science");
        certificate.setClassOfDegree("First-class");
        certificate.setCgpaValue(3.9);
        certificate.setGraduationDate(LocalDate.of(2024, 6, 1));
        certificate.setStudent(student);
        certificate.setInstitutionRecord(institution);
        certificate.setRevoked(false);

        transcript = new Transcript();
        transcript.setDocumentNumber("TRNS-001");
        transcript.setDegree("BSc");
        transcript.setCourse("Mathematics");
        transcript.setStudent(student);
        transcript.setInstitutionRecord(institution);
        transcript.setGraduationDate(LocalDate.of(2024, 6, 1));
        transcript.setRevoked(false);
    }

    @Test
    void getDashboardStats_returnsCorrectCounts() {
        when(studentRepo.count()).thenReturn(50L);
        when(certificateRepo.count()).thenReturn(30L);
        when(transcriptRepo.count()).thenReturn(20L);
        when(certificateRepo.countByIsRevoked (true)).thenReturn(3L);
        when(transcriptRepo.countByIsRevoked(true)).thenReturn(1L);
        when(accessLogRepo.count()).thenReturn(200L);

        DashboardStats stats = adminService.getDashboardStats();

        assertThat(stats.getTotalStudents()).isEqualTo(50L);
        assertThat(stats.getTotalCertificates()).isEqualTo(30L);
        assertThat(stats.getTotalTranscripts()).isEqualTo(20L);
        assertThat(stats.getRevokedCertificates()).isEqualTo(3L);
        assertThat(stats.getRevokedTranscripts()).isEqualTo(1L);
        assertThat(stats.getTotalAccessLogs()).isEqualTo(200L);
    }

    @Test
    void getAllCertificates_returnsMappedSummaries() {
        when(certificateRepo.findAll()).thenReturn(List.of(certificate));

        List<AdminDocumentSummary> result = adminService.getAllCertificates();

        assertThat(result).hasSize(1);
        AdminDocumentSummary summary = result.get(0);
        assertThat(summary.getDocumentNumber()).isEqualTo("CERT-001");
        assertThat(summary.getDocumentType()).isEqualTo("CERTIFICATE");
        assertThat(summary.getStudentId()).isEqualTo("STU-001");
        assertThat(summary.getStudentName()).isEqualTo("John Doe");
        assertThat(summary.getInstitutionName()).isEqualTo("Test University");
        assertThat(summary.isRevoked()).isFalse();
    }

    @Test
    void getAllTranscripts_returnsMappedSummaries() {
        when(transcriptRepo.findAll()).thenReturn(List.of(transcript));

        List<AdminDocumentSummary> result = adminService.getAllTranscripts();

        assertThat(result).hasSize(1);
        AdminDocumentSummary summary = result.get(0);
        assertThat(summary.getDocumentNumber()).isEqualTo("TRNS-001");
        assertThat(summary.getDocumentType()).isEqualTo("TRANSCRIPT");
        assertThat(summary.getCourse()).isEqualTo("Mathematics");
    }

    @Test
    void getAllCertificates_emptyRepo_returnsEmptyList() {
        when(certificateRepo.findAll()).thenReturn(List.of());

        List<AdminDocumentSummary> result = adminService.getAllCertificates();

        assertThat(result).isEmpty();
    }

    @Test
    void revokeCertificate_delegatesToCertificateService() {
        DocumentRevocationRequest request = new DocumentRevocationRequest();
        request.setDocumentNumber("CERT-001");
        request.setReason("Fraud");
        request.setRevokedBy("admin");

        adminService.revokeCertificate(request);

        verify(certificateService).revokeCertificate(request);
        verifyNoInteractions(transcriptService);
    }

    @Test
    void revokeTranscript_delegatesToTranscriptService() {
        DocumentRevocationRequest request = new DocumentRevocationRequest();
        request.setDocumentNumber("TRNS-001");
        request.setReason("Error");
        request.setRevokedBy("admin");

        adminService.revokeTranscript(request);

        verify(transcriptService).revokeTranscript(request);
        verifyNoInteractions(certificateService);
    }

    @Test
    void getAccessLogs_returnsAllLogs() {
        AccessLog log1 = AccessLog.builder()
                .documentNumber("CERT-001")
                .action("DOWNLOAD")
                .requesterEmail("hr@company.com")
                .timestamp(LocalDateTime.now())
                .build();

        when(accessLogRepo.findAll()).thenReturn(List.of(log1));

        List<AccessLog> logs = adminService.getAccessLogs();

        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getDocumentNumber()).isEqualTo("CERT-001");
    }

    @Test
    void getAllStudents_delegatesToStudentService() {
        StudentResponse response = new StudentResponse();
        response.setStudentId("STU-001");

        when(studentService.getAllStudents()).thenReturn(List.of(response));

        List<StudentResponse> result = adminService.getAllStudents();

        assertThat(result).hasSize(1);
        verify(studentService).getAllStudents();
    }
}
