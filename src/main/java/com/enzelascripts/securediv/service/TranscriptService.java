package com.enzelascripts.securediv.service;

import com.enzelascripts.securediv.entity.*;
import com.enzelascripts.securediv.repository.*;
import com.enzelascripts.securediv.exception.*;
import com.enzelascripts.securediv.request.DocumentDownloadRequest;
import com.enzelascripts.securediv.request.DocumentRevocationRequest;
import com.enzelascripts.securediv.request.TranscriptRequest;
import com.enzelascripts.securediv.response.SignatoryResponse;
import com.enzelascripts.securediv.response.TranscriptResponse;
import com.enzelascripts.securediv.response.VerificationResponse;
import com.enzelascripts.securediv.util.CourseResultSummary;
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
public class TranscriptService {
// =========================================== fields ==================================================================

    private final String verificationUrl = Utility.TRANSCRIPT_VERIFICATION_URL;


    @Autowired
    private S3Service s3Service;
    @Autowired
    private StudentRepo studentRepo;
    @Autowired
    private SignatoryRepo signatoryRepo;
    @Autowired
    private TranscriptRepo transcriptRepo;
    @Autowired
    private InstitutionRecordRepo institutionRecordRepo;
    @Autowired
    TemplateEngine templateEngine;
    @Autowired
    private PdfService pdfService;
    @Autowired
    private AccessLogService accessLogService;
    @Autowired
    private CourseResultSummary resultSummary;
    @Autowired
    private WebhookService webhookService;

    // ============================================ public methods =========================================================
    @Transactional(timeout = 9)
    public TranscriptResponse createTranscript(TranscriptRequest dto) {

        //create a transcript object
        validateNotNull(dto);
        Transcript transcript = createTranscriptObject(dto);

        //populate the HTML
        String html = getTranscriptHTML(transcript);

        //convert HTML to PDF bytes
        byte[] bytes = pdfService.generatePdf(html);

        //get the file fingerprint, update the certificate object
        String fingerprint = getFileFingerprint(bytes);
        transcript.setSha256Hash(fingerprint);

        s3Service.uploadTranscript(bytes, transcript.getS3Key(), "application/pdf");

        //save the certificate object
        saveTranscript(transcript);

        //email to the student the download url
        webhookService.sendWebhook(transcript);
        EmailService.notifyStudent(transcript.getStudent());

        return getTranscriptResponseObject(transcript);
    }

    public Transcript getTranscriptByDocumentNumber(String documentNumber) {

        validateNotNull(documentNumber);
        return transcriptRepo.getTranscriptByDocumentNumber(documentNumber)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Certificate: " + documentNumber + " not found"));
    }

    @Transactional
    public void revokeTranscript(DocumentRevocationRequest r) {

        validateNotNull(r);
        String docNumber = r.getDocumentNumber();

        //get the document
        Transcript transcript = getTranscriptByDocumentNumber(docNumber);

        if(transcript.isRevoked())
            throw new OperationalException(
                    "the Transcript: " + docNumber + " is already revoked");

        //update revocation details
        transcript.setRevoked(true);
        transcript.setRevokedBy("adm");
        transcript.setRevokedAt(LocalDateTime.now());
        transcript.setRevocationReason(r.getReason());
        saveTranscript(transcript);

        //delete from s3
        if(r.isDeleteFromStorage())
            s3Service.deleteTranscriptOnS3(transcript.getS3Key());

        //email student
        if(r.isNotifyStudent()){
            String studentEmail = transcript.getStudent().getEmail();
            EmailService.notifyStudent(studentEmail, r.getNotificationMessage());
        }

        log.info("Revoked transcript {} from DB", docNumber);

    }

    public String getTranscriptHTML(Transcript transcript) {
        //populate the HTML
        TranscriptResponse response = getTranscriptResponseObject(transcript);
        Context context = new Context();
        context.setVariable("transcript", response);

        //course summary
        String studentId = transcript.getStudent().getStudentId();
        resultSummary.getInstance(studentId);
//        resultSummary.
        context.setVariable("resultSummary", resultSummary);


        return templateEngine.process("transcript", context);
    }

    public void saveTranscript(Transcript transcript) {

        transcriptRepo.save(transcript);
    }

    public VerificationResponse verify(String documentNumber){

        VerificationResponse response = new VerificationResponse();

        Transcript transcript;
        try {
            transcript = getTranscriptByDocumentNumber(documentNumber);
        } catch (Exception e) {
            response.setStatus("NOT_FOUND");
            response.setMessage("Kindly confirm the document number and try again");
            response.setVerifiedAt(LocalDateTime.now());

            return response;
        }

        if(transcript.isRevoked()){

            response.setStatus("REVOKED");
            response.setMessage("This document was revoked by the issuing authority on " + transcript.getRevokedAt());
            response.setVerifiedAt(LocalDateTime.now());

            //transfer data
            transferData(transcript, response);
            transferData(transcript.getStudent(), response);
            transferData(transcript.getInstitutionRecord(), response);

            return response;

        }

        response.setStatus("VALID");
        response.setMessage("This document is valid till this date " + LocalDate.now());
        response.setVerifiedAt(LocalDateTime.now());

        //transfer data
        transferData(transcript, response);
        transferData(transcript.getStudent(), response);
        transferData(transcript.getInstitutionRecord(), response);

        return response;

    }

    public TranscriptResponse getTranscript(String documentNumber){

        Transcript transcript = getTranscriptByDocumentNumber(documentNumber);
        return getTranscriptResponseObject(transcript);
    }

    public String getTranscriptDownloadUrl(DocumentDownloadRequest dto){
        validateNotNull(dto);

        //get the Document
        String docNumber = validateNotNull(dto.getDocumentNumber());
        Transcript transcript = getTranscriptByDocumentNumber(docNumber);

        if(transcript.isRevoked()) {
            return "the transcript is revoked, therefore can not be downloaded. " +
                    "Contact the authority for more details";
        }

        //check file authenticity
        byte[] fileHash = s3Service.getDocumentAsByte(transcript.getS3Key());
        if(isFileCorrupted(fileHash, transcript.getSha256Hash())) {
            return "the transcript is corrupted and therefore can not be downloaded at this moment. " +
                    "Contact the authority for more details";
        }

        //register the request to AccessLog
        accessLogService.logAccess(dto, "DOWNLOAD");

        //set expiration for the link, save it
        transcript.setExpiresAt(LocalDateTime.now().plusMinutes(PRESIGNED_DURATION));
        saveTranscript(transcript);

        return docNumber + s3Service.getTranscriptDownloadUrl(transcript.getS3Key());

    }

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

        //check if student has a transcript for same course and degree
        if(!isTranscriptExist(id, degree, course)) {
            try {
                //transfer data from DTO to certificate
                Transcript transcript = transferData(dto, new Transcript());

                //generate unique Certificate ID, update the certificate object
                String documentNumber = generateDocumentNumber();
                transcript.setDocumentNumber(documentNumber);

                //set the s3 key
                String s3Key = "transcripts" + "/" + documentNumber + ".pdf";
                transcript.setS3Key(s3Key);

                //set the student object
                transcript.setStudent(student);

                //get institution record and signatories
                InstitutionRecord institutionRecord = institutionRecordRepo
                        .findInstitutionRecordByCurrent(true)
                        .orElseThrow(()->
                                new ResourceNotFoundException(
                                        " No active institution record at the moment"));

                List<Signatory> currentSignatories =
                        signatoryRepo.findSignatoriesByCurrent (true);

                //set the institution record and signatories
                transcript.setInstitutionRecord(institutionRecord);
                transcript.setSignatories(currentSignatories);

                //set the certificate revocation status
                transcript.setRevoked(false);

                return transcript;

            } catch (RuntimeException e) {
                log.error("Failed to create transcript", e);
                throw new OperationalException("Failed to create transcript");
            }
        }

        throw new CertificateExistsException("Student with id "
                + id + " already has a transcript for " +  degree + " in " + course);
    }

    @NonNull
    public TranscriptResponse getTranscriptResponseObject(Transcript transcript) {
        TranscriptResponse response = transferData(transcript, new TranscriptResponse());

        //transfer student first and last name
        transferData(transcript.getStudent(), response);

        //transfer institution name, address
        InstitutionRecord institutionRecord = transcript.getInstitutionRecord();
        transferData(institutionRecord, response);

        //get signatory response object
        List<SignatoryResponse> signatoryResponse = transcript.getSignatories()
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
        String qrCode = generateQRCode(verificationUrl + "/" + transcript.getDocumentNumber());
        response.setQrCode(qrCode);

        String s3Key = institutionRecord.getS3Key();
        byte[] logoBytes = s3Service.getLogoAsBytes(s3Key);
        String extension = s3Key.substring(s3Key.lastIndexOf('.'));
        String logoUrl = "data:image/" + extension + ";base64," + Base64.getEncoder().encodeToString(logoBytes);

        response.setLogoUrl(logoUrl);

        return response;
    }

    private boolean isTranscriptExist(Long id, String degree, String course){

        return transcriptRepo.existsByStudent_IdAndDegreeAndCourse(id, degree, course);

    }

    private String generateDocumentNumber() {
        String documentNumber = "";

        do{
            documentNumber = Utility.get12AlphaNumString("TRNS");
        }while (transcriptRepo.existsByDocumentNumber(documentNumber));

        return documentNumber;
    }


}

