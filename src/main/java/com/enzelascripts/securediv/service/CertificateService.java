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
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

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

        //get the file fingerprint, update the certificate object
        String fingerprint = getFileFingerprint(bytes);
        certificate.setFingerprint(fingerprint);

        s3Service.uploadCertificate(bytes, certificate.getS3Key());

        //save the certificate object
        saveCertificate(certificate);

        //send the email to the student to download the certificate
        //perform on the background
        //emailService.send(studentEmail)

        return s3Service.getCertificateDownloadUrl(certificate.getCertificateNumber());
    }

    public Certificate getCertificateByCertificateNumber(String certificateNumber) {

        validateNotNull(certificateNumber);
        return certificateRepo.
                findCertificateByCertificateNumber(certificateNumber).
                orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Certificate: " + certificateNumber + " not found"));
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

    public String getCertificateHTML(Certificate certificate) {
        //populate the HTML
        CertificateResponse certificateResponse = getCertificateResponseObject(certificate);
        Context context = new Context();
        context.setVariable("certificate", certificateResponse);

        return templateEngine.process("certificate", context);
    }

    public void saveCertificate(Certificate certificate) {

        certificateRepo.save(certificate);
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
                String certificateNumber = generateCertificateNumber();
                certificate.setCertificateNumber(certificateNumber);

                //set the s3 key
                String s3Key = "certificates" + "/" + certificateNumber + "." + "pdf";
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

                //set the certificate validity
                certificate.setValid(true);

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
        String qrCode = generateQRCode(verificationUrl + "/" + certificate.getCertificateNumber());
        response.setQrCode(qrCode);

        String s3Key = institutionRecord.getS3Key();
        byte[] logoBytes = s3Service.getLogoAsBytes(s3Key);
        String extension = s3Key.substring(s3Key.lastIndexOf('.'));
        String logoUrl = "data:" + "image/" + extension + ";base64," + Base64.getEncoder().encodeToString(logoBytes);

        response.setLogoUrl(logoUrl);

        return response;
    }

    private boolean isCertificateExist(Long id, String degree, String course){

        return certificateRepo.existsByStudent_IdAndDegreeAndCourse(id, degree, course);

    }

    private String generateCertificateNumber() {
        String certificateNumber = "";

        do{
            certificateNumber = Utility.get12AlphaNumString();
        }while (certificateRepo.existsByCertificateNumber(certificateNumber));

        return certificateNumber;
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

