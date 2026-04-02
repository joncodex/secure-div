package com.enzelascripts.securediv.service;

import com.enzelascripts.securediv.repository.InstitutionRecordRepo;
import com.enzelascripts.securediv.repository.SignatoryRepo;
import com.enzelascripts.securediv.repository.StudentRepo;
import com.enzelascripts.securediv.request.CertificateRequest;
import com.enzelascripts.securediv.entity.Certificate;
import com.enzelascripts.securediv.entity.InstitutionRecord;
import com.enzelascripts.securediv.entity.Signatory;
import com.enzelascripts.securediv.entity.Student;
import com.enzelascripts.securediv.exception.*;
import com.enzelascripts.securediv.repository.CertificateRepo;
import com.enzelascripts.securediv.response.CertificateResponse;
import com.enzelascripts.securediv.response.SignatoryResponse;
import com.enzelascripts.securediv.util.Utility;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

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


// ============================================ public methods =========================================================

    public String createCertificate(CertificateRequest dto) {
        //create a certificate object
        validateNotNull(dto);
        Certificate certificate = createCertificateObject(dto);

        //populate the HTML
        String html = getCertificateHTML(certificate);

        //convert HTML to PDF bytes
        byte[] bytes = pdfService.generatePdf(html);

        //get the file fingerprint
        String fingerprint = getFileFingerprint(bytes);

        //upload the PDF
        s3Service.upload(bytes, certificate.getCertificateNumber());

        //update and save the certificate object
        certificate.setFingerprint(fingerprint);
        certificate.setDownloadUrl(
                s3Service.getDownloadUrl(certificate.getCertificateNumber()));
        saveCertificate(certificate);

        //send the email to the student to download the certificate
        //perform on the background
        //emailService.send(studentEmail)

        return certificate.getDownloadUrl();
    }

    public Certificate getCertificateByCertificateNumber(String certificateNumber) {

        validateNotNull(certificateNumber);
        return certificateRepo.
                findCertificateByCertificateNumber(certificateNumber).
                orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Certificate: " + certificateNumber + " not found"));
    }

    @NonNull
    public CertificateResponse getCertificateResponseObject(Certificate certificate) {
        CertificateResponse response = transferData(certificate, new CertificateResponse());

        transferData(certificate.getStudent(), response);
        transferData(certificate.getInstitutionRecord(), response);

        //get signatory response object
        List<SignatoryResponse> signatoryResponse = certificate.getSignatories()
                .stream()
                .map(signatory ->
                        transferData(signatory, new SignatoryResponse()))
                .toList();
        //set the signatory object
        response.setSignatory(signatoryResponse);

        return response;
    }

    public boolean isCertificateValid(String certificateNumber) {

        validateNotNull(certificateNumber);
        return getCertificateByCertificateNumber(certificateNumber).isValid();
    }

    public void revokeCertificate(String certificateNumber) {

        validateNotNull(certificateNumber);
        if(!isCertificateValid(certificateNumber))
            throw new OperationalException(
                    "the Certificate: " + certificateNumber + " is already revoked");

        s3Service.revokeCertificateOnS3(certificateNumber);
        revokeCertificateOnDB(certificateNumber);
    }

// ========================================== private methods ==========================================================

    private Certificate createCertificateObject(CertificateRequest dto) {

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

                //generate unique Certificate ID
                String certificateNumber = generateCertificateNumber();

                certificate.setStudent(student);
                certificate.setCertificateNumber(certificateNumber);
                certificate.setQrCode(generateQRCode(verificationUrl + "/" + certificateNumber));

                InstitutionRecord institutionRecord = institutionRecordRepo
                        .findInstitutionRecordByCurrent(true)
                        .orElseThrow(()->
                                new ResourceNotFoundException(
                                        " No active institution record at the moment"));

                certificate.setInstitutionRecord(institutionRecord);

                List<Signatory> currentSignatories =
                        signatoryRepo.findSignatoriesByCurrent (true);
                certificate.setSignatories(currentSignatories);

                certificate.setValid(true);
                saveCertificate(certificate);

                return certificate;

            } catch (RuntimeException e) {
                log.error("Failed to create certificate", e);
//                throw new OperationalException("Failed to create certificate");
                throw new RuntimeException("Failed to create certificate", e);

            }
        }

        throw new CertificateExistsException("Student with id "
                + id + " already has " +  degree + " in " + course);
    }

    public void saveCertificate(Certificate certificate) {

        certificateRepo.save(certificate);
    }

    private boolean isCertificateExist(Long id, String degree, String course) {

        return certificateRepo.existsByStudent_IdAndDegreeAndCourse(id, degree, course);

    }

    private String generateCertificateNumber() {
        String certificateNumber = "";

        do{
            certificateNumber = Utility.get12AlphaNumString();
        }while (certificateRepo.existsByCertificateNumber(certificateNumber));

        return certificateNumber;
    }

    public String getCertificateHTML(Certificate certificate) {
        //populate the HTML
        CertificateResponse certificateResponse = getCertificateResponseObject(certificate);
        Context context = new Context();
        context.setVariable("certificate", certificateResponse);

        return templateEngine.process("certificate", context);
    }

    public byte[] convertHtmlToBytes(String html) {

        return convertHTMLtoBytes(html);
    }

    private void revokeCertificateOnDB(String certificateNumber) {
        Certificate certificate = certificateRepo.
                findCertificateByCertificateNumber(certificateNumber).
                orElseThrow(()->
                        new ResourceNotFoundException("Certificate: " +certificateNumber+ " not found"));
        certificate.setValid(false);
        certificateRepo.save(certificate);
        log.info("Revoked certificate {} from DB", certificateNumber);
    }




}

