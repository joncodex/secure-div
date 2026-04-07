package com.enzelascripts.securediv.service;

import com.enzelascripts.securediv.entity.*;
import com.enzelascripts.securediv.exception.*;
import com.enzelascripts.securediv.repository.*;
import com.enzelascripts.securediv.request.RevokeRequest;
import com.enzelascripts.securediv.request.TranscriptRequest;
import com.enzelascripts.securediv.response.CertificateResponse;
import com.enzelascripts.securediv.response.SignatoryResponse;
import com.enzelascripts.securediv.response.TranscriptResponse;
import com.enzelascripts.securediv.response.VerificationResponse;
import com.enzelascripts.securediv.util.Utility;
import jakarta.persistence.Column;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

import static com.enzelascripts.securediv.util.Utility.*;

@Slf4j
@Service
public class TranscriptService {
// =========================================== fields ==================================================================

    private final String verificationUrl = Utility.CERTIFICATE_VERIFICATION_URL;

    @Autowired
    private S3Service s3Service;
    @Autowired
    private StudentRepo studentRepo;
    @Autowired
    private SignatoryRepo signatoryRepo;
    @Autowired
    private CertificateRepo certificateRepo;
    @Autowired
    private InstitutionRecordRepo institutionRecordRepo;
    @Autowired
    TemplateEngine templateEngine;
    @Autowired
    private PdfService pdfService;
    @Autowired
    private DocumentRepo documentRepo;
    @Autowired
    private AccessLogService accessLogService;

// ============================================ public methods =========================================================

    @Transactional(timeout = 5)
    public String createTranscript(TranscriptRequest dto) {

        //null check the DTO, create a transcript object
        validateNotNull(dto);
        Transcript document = createTranscriptObject(dto);

        //populate the HTML
        String html = getDocumentHTML(document);

        //convert HTML to PDF bytes
        byte[] pdfBytes = pdfService.generatePdf(html);

        //get the file fingerprint, update the certificate object
        String fingerprint = getFileFingerprint(pdfBytes);
        document.setSha256Hash(fingerprint);

        s3Service.uploadTranscript(pdfBytes, document.getS3Key());

        //save the document object
        saveDocument(document);

        //send the email to the student to download the certificate
        //perform on the background
        //emailService.send(studentEmail)

        return s3Service.getPresignedDownloadUrl(document.getS3Key());
    }

    public Document getDocumentByDocumentNumber(String documentNumber) {

        validateNotNull(documentNumber);
        return documentRepo.getDocumentByDocumentNumber(documentNumber)
                .orElseThrow(DocumentNotFoundException::new);
    }

    public boolean isDocumentRevoked(String documentNumber) {

        validateNotNull(documentNumber);
        return getDocumentByDocumentNumber(documentNumber).isRevoked();
    }

    @Transactional
    public void revokeDocument(RevokeRequest request) {

        //null check the request
        validateNotNull(request);

        String documentNumber = request.getDocumentNumber();
        Document document = getDocumentByDocumentNumber(documentNumber);
        if(document.isRevoked())
            throw new IllegalStateException(
                    "Document is already revoked");

        revokeDocumentOnDB(request);
        s3Service.revokeCertificateOnS3(document.getS3Key());

        if(request.isNotifyStudent()) {
            //Email the student
//            if (request.isNotifyStudent()) {
//                emailService.sendRevocationNotice(
//                        doc.getStudentEmail(),
//                        doc.getStudentName(),
//                        doc.getDocumentType(),
//                        request.getReason()
//                );
//
//                webhookService.notifyStudent(NotificationPayload.builder()
//                        .notificationType("REVOKED")
//                        .documentId(doc.getDocumentId())
//                        .documentType(doc.getDocumentType())
//                        .studentId(doc.getStudentId())
//                        .studentEmail(doc.getStudentEmail())
//                        .studentName(doc.getStudentName())
//                        .revocationReason(request.getReason())
//                        .revokedAt(doc.getRevokedAt())
//                        .build()
//                );
//            }

//            notificationService.sendNotification(document, request.getNotificationMessage());
        }
    }

    public String getDocumentHTML(Certificate document) {
        //populate the HTML
        CertificateResponse response = getCertificateResponseObject(document);
        Context context = new Context();
        context.setVariable("certificate", response);

        return templateEngine.process("certificate", context);
    }

    public String getDocumentHTML(Transcript document) {
        //populate the HTML
        TranscriptResponse response = getTranscriptResponseObject(document);
        Context context = new Context();
        context.setVariable("transcript", response);

        return templateEngine.process("transcript", context);
    }

    public void saveDocument(Document document) {

        documentRepo.save(document);
    }





    // ==================== VERIFICATION ====================

    @Transactional(readOnly = true)
    public VerificationResponse verifyDocument(String documentNumber) {
        log.info("Verifying document: {}", documentNumber);

        Document document = getDocumentByDocumentNumber(documentNumber);

        // Log access attempt
//        accessLogService.logAccess (document.getDocumentNumber(), "VERIFY");

        if (document.isRevoked()) {
            return VerificationResponse.builder()
                    .status("REVOKED")
                    .documentNumber(document.getDocumentNumber())
                    .documentType(document.getDocumentType())
                    .revokedAt(document.getRevokedAt())
                    .message("This document has been revoked by the issuing authority")
                    .build();
        }

        return VerificationResponse.builder()
                .status("VALID")
                .documentNumber(document.getDocumentNumber())
                .documentType(document.getDocumentType())
                .issuedAt(document.getIssuedAt())
                .verifiedAt(LocalDateTime.now())
                .studentName(document.getStudent().getFirstName() + " " + document.getStudent().getLastName())
                .studentNumber(document.getStudent().getStudentId())
                .course(document.getCourse())
                .degree(document.getDegree())
                .cgpaDisplay(document.getCgpaValue().toString())
                .graduationDate(document.getGraduationDate())
                .institutionName(document.getInstitutionRecord().getInstitutionName())
                .message("Document is valid and authentic")
                .build();
    }


    // ==================== DOWNLOAD & ACCESS ====================

//    @Transactional
//    public byte[] downloadDocument(String documentId, String requesterEmail) {
//        log.info("Download requested for document: {} by {}", documentId, requesterEmail);
//
//        DocumentIssuance doc = issuanceRepository.findById(documentId)
//                .orElseThrow(() -> new DocumentNotFoundException("Document not found: " + documentId));
//
//        // Check if revoked
//        if (doc.isRevoked()) {
//            throw new RevokedDocumentException("Document has been revoked: " + doc.getRevocationReason());
//        }
//
//        // Check if expired
//        if (doc.getExpiresAt().isBefore(LocalDateTime.now())) {
//            throw new StaleLinkException("Download link has expired");
//        }
//
//        // Fetch from S3
//        byte[] fileBytes = s3Service.getObjectBytes(doc.getFileKey());
//
//        // Verify integrity
//        String currentHash = calculateSha256(fileBytes);
//        if (!currentHash.equals(doc.getSha256Hash())) {
//            throw new HashMismatchException("Document integrity check failed. Possible tampering.");
//        }
//
//        // Log successful download
//        logAccess(doc.getDocumentId(), "DOWNLOAD", requesterEmail);
//
//        // Notify via webhook
//        webhookService.notifyStudent(NotificationPayload.builder()
//                .notificationType("DOWNLOADED")
//                .documentId(doc.getDocumentId())
//                .documentType(doc.getDocumentType())
//                .studentId(doc.getStudentId())
//                .studentEmail(doc.getStudentEmail())
//                .studentName(doc.getStudentName())
//                .ipAddress(getClientIp())
//                .userAgent(getClientUserAgent())
//                .build()
//        );
//
//        return fileBytes;
//    }

//    public String rotatePresignedDownloadUrl(String documentNumber) {
//        DocumentIssuance doc = issuanceRepository.findById(documentId)
//                .orElseThrow(() -> new DocumentNotFoundException("Document not found: " + documentId));
//
//        if (doc.isRevoked()) {
//            throw new RevokedDocumentException("Cannot rotate link for revoked document");
//        }
//
//        // Generate new expiration and URL
//        doc.setExpiresAt(LocalDateTime.now().plus(DOWNLOAD_LINK_DURATION));
//        issuanceRepository.save(doc);
//
//        return s3Service.generatePresignedUrl(doc.getFileKey(), DOWNLOAD_LINK_DURATION);
//    }

//    @Transactional(readOnly = true)
//    public List<Document> getAllDocuments(String documentType, Boolean revoked) {
//        if (documentType != null && revoked != null) {
//            return documentRepo.findByDocumentTypeAndRevoked(documentType, revoked);
//        } else if (documentType != null) {
//            return issuanceRepository.findByDocumentType(documentType);
//        } else if (revoked != null) {
//            return issuanceRepository.findByRevoked(revoked);
//        }
//        return issuanceRepository.findAll();
//    }

//    @Transactional(readOnly = true)
//    public List<AccessLog> getDocumentAccessLogs(String documentId) {
//        return accessLogRepository.findByDocumentIdOrderByAccessedAtDesc(documentId);
//    }




// ========================================== private methods ==========================================================

    private Transcript createTranscriptObject(@NonNull TranscriptRequest dto) {

        String studentId = validateNotNull(dto.getStudentId());
        String degree =    validateNotNull(dto.getDegree());
        String course =    validateNotNull(dto.getCourse());

        // get the actual student object, if exists
        Student student = studentRepo
                .findStudentByStudentId(studentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("student with ID: " + studentId + "was not found"));

        Long id = student.getId();

        //check if student has a certificate for same course and degree
        if(!isDocumentExist(id, degree, course)) {
                //transfer data from DTO to transcript
                Transcript document = transferData(dto, new Transcript());

                //generate unique Certificate ID, update the certificate object
                String documentNumber = generateDocumentNumber();
                document.setDocumentNumber(documentNumber);

                //set the s3 key
                String s3Key = document.getDocumentType() + "/" + documentNumber + "." + "pdf";
                document.setS3Key(s3Key);

                //set the student object
                document.setStudent(student);

                //get institution record and signatories
                InstitutionRecord institutionRecord = institutionRecordRepo
                        .findInstitutionRecordByCurrent(true)
                        .orElseThrow(()->
                                new ResourceNotFoundException(
                                        " No active institution record at the moment"));

                List<Signatory> currentSignatories =
                        signatoryRepo.findSignatoriesByCurrent (true);

                //set the institution record and signatories
                document.setInstitutionRecord(institutionRecord);
                document.setSignatories(currentSignatories);

                //set the certificate validity
                document.setRevoked(false);

                return document;
        }

        throw new CertificateExistsException("Student with id "
                + id + " already has " +  degree + " in " + course);
    }

    @NonNull
    public CertificateResponse getCertificateResponseObject(Certificate document) {
        CertificateResponse response = transferData(document, new CertificateResponse());

        //transfer student first and last name
        transferData(document.getStudent(), response);

        //transfer institution name, address
        InstitutionRecord institutionRecord = document.getInstitutionRecord();
        transferData(institutionRecord, response);

        //get signatory response object
        List<SignatoryResponse> signatoryResponse = document.getSignatories()
                .stream()
                .map(s ->{

                    byte[] sigBytes = s3Service.getSignatureAsBytes(s.getS3Key());
                    String sigImgUrl = Base64.getEncoder().encodeToString(sigBytes);

                    SignatoryResponse sigResponse =  new SignatoryResponse();
                    sigResponse.setName(s.getName());
                    sigResponse.setPosition(s.getPosition());
                    sigResponse.setSignatureUrl(sigImgUrl);

                    return sigResponse;
                })
                .toList();

        //set the signatory object
        response.setSignatory(signatoryResponse);

        //update qrcode and logoURL
        String qrCode = generateQRCode(verificationUrl + "/" + document.getDocumentNumber());
        response.setQrCode(qrCode);

        String s3Key = institutionRecord.getS3Key();
        byte[] logoBytes = s3Service.getLogoAsBytes(s3Key);
        String extension = s3Key.substring(s3Key.lastIndexOf('.'));
        String logoUrl = "data:" + "image/" + extension + ";base64," + Base64.getEncoder().encodeToString(logoBytes);

        response.setLogoUrl(logoUrl);

        return response;
    }

    @NonNull
    public TranscriptResponse getTranscriptResponseObject(Transcript document) {
        TranscriptResponse response = transferData(document, new TranscriptResponse());

        //transfer student first and last name
        transferData(document.getStudent(), response);

        //transfer institution name, address
        InstitutionRecord institutionRecord = document.getInstitutionRecord();
        transferData(institutionRecord, response);

        //get signatory response object
        List<SignatoryResponse> signatoryResponse = document.getSignatories()
                .stream()
                .map(s ->{

                    byte[] sigBytes = s3Service.getSignatureAsBytes(s.getS3Key());
                    String sigImgUrl = Base64.getEncoder().encodeToString(sigBytes);

                    SignatoryResponse sigResponse =  new SignatoryResponse();
                    sigResponse.setName(s.getName());
                    sigResponse.setPosition(s.getPosition());
                    sigResponse.setSignatureUrl(sigImgUrl);

                    return sigResponse;
                })
                .toList();

        //set the signatory object
        response.setSignatory(signatoryResponse);

        //update qrcode and logoURL
        String qrCode = generateQRCode(verificationUrl + "/" + document.getDocumentNumber());
        response.setQrCode(qrCode);

        String s3Key = institutionRecord.getS3Key();
        byte[] logoBytes = s3Service.getLogoAsBytes(s3Key);
        String extension = s3Key.substring(s3Key.lastIndexOf('.'));
        String logoUrl = "data:" + "image/" + extension + ";base64," + Base64.getEncoder().encodeToString(logoBytes);

        response.setLogoUrl(logoUrl);

        return response;
    }

    private boolean isDocumentExist(Long id, String degree, String course){

        return documentRepo.existsByStudent_IdAndDegreeAndCourse(id, degree, course);

    }

    private String generateDocumentNumber() {
        String documentNumber = "";

        do{
            documentNumber = Utility.get12AlphaNumString();
        }while (documentRepo.existsByDocumentNumber(documentNumber));

        return documentNumber;
    }

    private void revokeDocumentOnDB(RevokeRequest request) {
        Document document = getDocumentByDocumentNumber(request.getDocumentNumber());

        //Revoked the document
        document.setRevoked(true);
        document.setRevokedAt(LocalDateTime.now());
        document.setRevocationReason(request.getReason());
        document.setRevokedBy(request.getRevokedBy());

        documentRepo.save(document);
        log.info("Document {} has been revoked by {}. Reason: {}", request.getDocumentNumber(), request.getRevokedBy(), request.getReason());
    }


}

