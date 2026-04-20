package com.enzelascripts.securediv.service;

import com.enzelascripts.securediv.entity.Certificate;
import com.enzelascripts.securediv.entity.InstitutionRecord;
import com.enzelascripts.securediv.entity.Student;
import com.enzelascripts.securediv.exception.OperationalException;
import com.enzelascripts.securediv.exception.ResourceNotFoundException;
import com.enzelascripts.securediv.repository.CertificateRepo;
import com.enzelascripts.securediv.repository.InstitutionRecordRepo;
import com.enzelascripts.securediv.repository.SignatoryRepo;
import com.enzelascripts.securediv.repository.StudentRepo;
import com.enzelascripts.securediv.request.DocumentRevocationRequest;
import com.enzelascripts.securediv.response.VerificationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.TemplateEngine;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CertificateServiceTest {

    @Mock private CertificateRepo certificateRepo;
    @Mock private StudentRepo studentRepo;
    @Mock private SignatoryRepo signatoryRepo;
    @Mock private InstitutionRecordRepo institutionRecordRepo;
    @Mock private S3Service s3Service;
    @Mock private PdfService pdfService;
    @Mock private AccessLogService accessLogService;
    @Mock private WebhookService webhookService;
    @Mock private TemplateEngine templateEngine;

    @InjectMocks
    private CertificateService certificateService;

    private Certificate certificate;
    private Student student;
    private InstitutionRecord institution;

    @BeforeEach
    void setUp() {
        student = new Student();
        student.setId(1L);
        student.setStudentId("STU-001");
        student.setFirstName("John");
        student.setLastName("Doe");
        student.setEmail("john@university.edu");

        institution = new InstitutionRecord();
        institution.setInstitutionName("Test University");
        institution.setAddress("123 University Ave");
        institution.setS3Key("logos/logo.png");

        certificate = new Certificate();
        certificate.setDocumentNumber("CERT-ABC-123456");
        certificate.setDegree("Bachelor of Science");
        certificate.setCourse("Computer Science");
        certificate.setClassOfDegree("First-class Honours");
        certificate.setCgpaValue(3.85);
        certificate.setGraduationDate(LocalDate.of(2024, 6, 15));
        certificate.setStudent(student);
        certificate.setInstitutionRecord(institution);
        certificate.setSignatories(List.of());
        certificate.setS3Key("certificates/CERT-ABC-123456.pdf");
        certificate.setSha256Hash("abc123hash");
        certificate.setRevoked(false);
    }

    @Test
    void getCertificateByDocumentNumber_exists_returnsCertificate() {
        when(certificateRepo.getCertificateByDocumentNumber("CERT-ABC-123456"))
                .thenReturn(Optional.of(certificate));

        Certificate result = certificateService.getCertificateByDocumentNumber("CERT-ABC-123456");

        assertThat(result).isNotNull();
        assertThat(result.getDocumentNumber()).isEqualTo("CERT-ABC-123456");
    }

    @Test
    void getCertificateByDocumentNumber_notFound_throwsResourceNotFoundException() {
        when(certificateRepo.getCertificateByDocumentNumber("UNKNOWN"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> certificateService.getCertificateByDocumentNumber("UNKNOWN"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("UNKNOWN");
    }

    @Test
    void getCertificateByDocumentNumber_nullInput_throwsException() {
        assertThatThrownBy(() -> certificateService.getCertificateByDocumentNumber(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void revokeCertificate_validRequest_revokesSuccessfully() {
        DocumentRevocationRequest request = new DocumentRevocationRequest();
        request.setDocumentNumber("CERT-ABC-123456");
        request.setReason("Fraudulent submission");
        request.setRevokedBy("admin");
        request.setNotifyStudent(false);
        request.setDeleteFromStorage(false);

        when(certificateRepo.getCertificateByDocumentNumber("CERT-ABC-123456"))
                .thenReturn(Optional.of(certificate));

        certificateService.revokeCertificate(request);

        assertThat(certificate.isRevoked()).isTrue();
        assertThat(certificate.getRevocationReason()).isEqualTo("Fraudulent submission");
        assertThat(certificate.getRevokedAt()).isNotNull();
        verify(certificateRepo).save(certificate);
    }

    @Test
    void revokeCertificate_alreadyRevoked_throwsOperationalException() {
        certificate.setRevoked(true);

        DocumentRevocationRequest request = new DocumentRevocationRequest();
        request.setDocumentNumber("CERT-ABC-123456");
        request.setReason("Duplicate");
        request.setRevokedBy("admin");

        when(certificateRepo.getCertificateByDocumentNumber("CERT-ABC-123456"))
                .thenReturn(Optional.of(certificate));

        assertThatThrownBy(() -> certificateService.revokeCertificate(request))
                .isInstanceOf(OperationalException.class)
                .hasMessageContaining("already revoked");
    }

    @Test
    void revokeCertificate_withDeleteFromStorage_deletesFromS3() {
        DocumentRevocationRequest request = new DocumentRevocationRequest();
        request.setDocumentNumber("CERT-ABC-123456");
        request.setReason("Fraudulent");
        request.setRevokedBy("admin");
        request.setDeleteFromStorage(true);
        request.setNotifyStudent(false);

        when(certificateRepo.getCertificateByDocumentNumber("CERT-ABC-123456"))
                .thenReturn(Optional.of(certificate));

        certificateService.revokeCertificate(request);

        verify(s3Service).deleteCertificateOnS3(certificate.getS3Key());
    }

    @Test
    void verify_validCertificate_returnsValidStatus() {
        when(certificateRepo.getCertificateByDocumentNumber("CERT-ABC-123456"))
                .thenReturn(Optional.of(certificate));

        VerificationResponse response = certificateService.verify("CERT-ABC-123456");

        assertThat(response.getStatus()).isEqualTo("VALID");
    }

    @Test
    void verify_revokedCertificate_returnsRevokedStatus() {
        certificate.setRevoked(true);
        certificate.setRevokedAt(LocalDateTime.now());

        when(certificateRepo.getCertificateByDocumentNumber("CERT-ABC-123456"))
                .thenReturn(Optional.of(certificate));

        VerificationResponse response = certificateService.verify("CERT-ABC-123456");

        assertThat(response.getStatus()).isEqualTo("REVOKED");
    }

    @Test
    void verify_documentNotFound_returnsNotFoundStatus() {
        when(certificateRepo.getCertificateByDocumentNumber("NONEXISTENT"))
                .thenReturn(Optional.empty());

        VerificationResponse response = certificateService.verify("NONEXISTENT");

        assertThat(response.getStatus()).isEqualTo("NOT_FOUND");
        assertThat(response.getMessage()).contains("Kindly confirm");
    }
}
