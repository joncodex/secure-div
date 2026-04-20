package com.enzelascripts.securediv.service;

import com.enzelascripts.securediv.repository.*;
import com.enzelascripts.securediv.request.CertificateRequest;
import com.enzelascripts.securediv.entity.Certificate;
import com.enzelascripts.securediv.entity.InstitutionRecord;
import com.enzelascripts.securediv.entity.Signatory;
import com.enzelascripts.securediv.entity.Student;
import com.enzelascripts.securediv.exception.*;
import com.enzelascripts.securediv.request.DocumentDownloadRequest;
import com.enzelascripts.securediv.request.DocumentRevocationRequest;
import com.enzelascripts.securediv.response.CertificateResponse;
import com.enzelascripts.securediv.response.SignatoryResponse;
import com.enzelascripts.securediv.response.VerificationResponse;
import com.enzelascripts.securediv.util.Utility;
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
import static com.enzelascripts.securediv.util.Utility.validateNotNull;

@Slf4j
@Service
public class CertificateService {
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
    private AccessLogService accessLogService;
    @Autowired
    private WebhookService webhookService;


    // ============================================ public methods =========================================================
    @Transactional(timeout = 9)
    public CertificateResponse createCertificate(CertificateRequest dto) {

        //create a certificate object
        validateNotNull(dto);
        Certificate certificate = createCertificateObject(dto);

        //populate the HTML
        String html = getCertificateHTML(certificate);

        //convert HTML to PDF bytes
        byte[] bytes = pdfService.generatePdf(html);

        //get the file fingerprint, update the certificate object
        String fingerprint = getFileFingerprint(bytes);
        certificate.setSha256Hash(fingerprint);

        s3Service.uploadCertificate(bytes, certificate.getS3Key(), "application/pdf");

        //save the certificate object
        saveCertificate(certificate);

        //email to the student the download url
        webhookService.sendWebhook(certificate);
        EmailService.notifyStudent(certificate.getStudent());

        return getCertificateResponseObject(certificate);
    }

    public Certificate getCertificateByDocumentNumber(String documentNumber) {

        validateNotNull(documentNumber);
        return certificateRepo.getCertificateByDocumentNumber (documentNumber).
                orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Certificate: " + documentNumber + " not found"));
    }

    @Transactional
    public void revokeCertificate(DocumentRevocationRequest r) {

        validateNotNull(r);
        String docNumber = r.getDocumentNumber();

        //get the document
        Certificate cert = getCertificateByDocumentNumber(docNumber);

        if(cert.isRevoked())
            throw new OperationalException(
                    "the Certificate: " + docNumber + " is already revoked");

        //update revocation details
        cert.setRevoked(true);
        cert.setRevokedBy("adm");
        cert.setRevokedAt(LocalDateTime.now());
        cert.setRevocationReason(r.getReason());
        saveCertificate(cert);

        //delete from s3
        if(r.isDeleteFromStorage())
            s3Service.deleteCertificateOnS3(cert.getS3Key());

        //email student
        if(r.isNotifyStudent()){
            String studentEmail = cert.getStudent().getEmail();
            EmailService.notifyStudent(studentEmail, r.getNotificationMessage());
        }

        log.info("Revoked certificate {} from DB", docNumber);

    }

    public String getCertificateHTML(Certificate certificate) {
        //populate the HTML
        CertificateResponse certificateResponse = getCertificateResponseObject(certificate);
        Context context = new Context();
        context.setVariable("certificate", certificateResponse);

        return templateEngine.process("certificate", context);
    }

    public void  saveCertificate(Certificate certificate) {

        certificateRepo.save(certificate);
    }

    public VerificationResponse verify(String documentNumber){

        VerificationResponse response = new VerificationResponse();

        Certificate certificate;
        try {
            certificate = getCertificateByDocumentNumber(documentNumber);
        } catch (Exception e) {
            response.setStatus("NOT_FOUND");
            response.setMessage("Kindly confirm the document number and try again");
            response.setVerifiedAt(LocalDateTime.now());

            return response;
        }

        if(certificate.isRevoked()){

            response.setStatus("REVOKED");
            response.setMessage("This document was revoked by the issuing authority on " + certificate.getRevokedAt());
            response.setVerifiedAt(LocalDateTime.now());

            //transfer data
            transferData(certificate, response);
            transferData(certificate.getStudent(), response);
            transferData(certificate.getInstitutionRecord(), response);

            return response;

        }

        response.setStatus("VALID");
        response.setMessage("This document is valid till this date " + LocalDate.now());
        response.setVerifiedAt(LocalDateTime.now());

        //transfer data
        transferData(certificate, response);
        transferData(certificate.getStudent(), response);
        transferData(certificate.getInstitutionRecord(), response);

        return response;

    }

    public CertificateResponse getCertificate(String documentNumber){

        Certificate certificate = getCertificateByDocumentNumber(documentNumber);
        return getCertificateResponseObject(certificate);
    }

    public String getCertificateDownloadUrl(DocumentDownloadRequest dto){
        validateNotNull(dto);

        //get the Document
        String docNumber = validateNotNull(dto.getDocumentNumber());
        Certificate cert = getCertificateByDocumentNumber(docNumber);

        if(cert.isRevoked()) {
            return "the certificate is revoked, therefore can not be downloaded. " +
                    "Contact the authority for more details";
        }

        //check file authenticity
        byte[] fileHash = s3Service.getDocumentAsByte(cert.getS3Key());
        if(isFileCorrupted(fileHash, cert.getSha256Hash())) {
            return "the transcript is corrupted and therefore can not be downloaded at this moment. " +
                    "Contact the authority for more details";
        }

        //register the request to AccessLog
        accessLogService.logAccess(dto, "DOWNLOAD");

        //set expiration for the link, save it
        cert.setExpiresAt(LocalDateTime.now().plusMinutes(PRESIGNED_DURATION));
        saveCertificate(cert);

        return docNumber + s3Service.getCertificateDownloadUrl(cert.getS3Key());

    }

// ========================================== private methods ==========================================================
    private Certificate createCertificateObject(@NonNull CertificateRequest dto) {

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
        if(!isCertificateExist(id, degree, course)) {
            try {

                //transfer data from DTO to certificate
                Certificate certificate = transferData(dto, new Certificate());

                //generate unique Certificate ID, update the certificate object
                String documentNumber = generateDocumentNumber();
                certificate.setDocumentNumber(documentNumber);

                //set the s3 key
                String s3Key = "certificates" + "/" + documentNumber + ".pdf";
                certificate.setS3Key(s3Key);

                //set the student object
                certificate.setStudent(student);

                //get institution record and signatories
                InstitutionRecord institutionRecord = institutionRecordRepo
                        .findInstitutionRecordByCurrent(true)
                        .orElseThrow(()->
                                new ResourceNotFoundException(
                                        " No active institution record at the moment"));

                List<Signatory> currentSignatories =
                        signatoryRepo.findSignatoriesByCurrent (true);

                //set the institution record and signatories
                certificate.setInstitutionRecord(institutionRecord);
                certificate.setSignatories(currentSignatories);

                //set the certificate revocation status
                certificate.setRevoked(false);

                return certificate;

            } catch (RuntimeException e) {
                log.error("Failed to create certificate", e);
                throw new OperationalException("Failed to create certificate");
            }
        }

        throw new CertificateExistsException("Student with id "
                + id + " already has " +  degree + " in " + course);
    }

    @NonNull
    public CertificateResponse getCertificateResponseObject(Certificate certificate) {
        System.out.println(certificate.getDegree() + "   " + certificate.getCourse());
        CertificateResponse response = transferData(certificate, new CertificateResponse());

        //transfer student first and last name
        transferData(certificate.getStudent(), response);

        //transfer institution name, address
        InstitutionRecord institutionRecord = certificate.getInstitutionRecord();
        transferData(institutionRecord, response);

        //get signatory response object
        List<SignatoryResponse> signatoryResponse = certificate.getSignatories()
                .stream()
                .map(s ->{

                    String s3Key = s.getS3Key();
                    byte[] sigBytes = s3Service.getSignatureAsBytes(s.getS3Key());
                    String extension = s3Key.substring(s3Key.lastIndexOf("."));
                    String sigImgUrl = "data:image/" + extension + ";base64," + Base64.getEncoder().encodeToString(sigBytes);

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
        String qrCode = generateQRCode(verificationUrl + "/" + certificate.getDocumentNumber());
        response.setQrCode(qrCode);

        String s3Key = institutionRecord.getS3Key();
        byte[] logoBytes = s3Service.getLogoAsBytes(s3Key);
        String extension = s3Key.substring(s3Key.lastIndexOf('.'));
        String logoUrl = "data:image/" + extension + ";base64," + Base64.getEncoder().encodeToString(logoBytes);

        response.setLogoUrl(logoUrl);

        return response;
    }

    private boolean isCertificateExist(Long id, String degree, String course){

        return certificateRepo.existsByStudent_IdAndDegreeAndCourse (id, degree, course);

    }

    private String generateDocumentNumber() {
        String certificateNumber = "";

        do{
            certificateNumber = Utility.get12AlphaNumString("CERT");
        }while (certificateRepo.existsByDocumentNumber(certificateNumber));

        return certificateNumber;
    }


}

