package com.enzelascripts.securediv.service;

import com.enzelascripts.securediv.entity.Document;
import com.enzelascripts.securediv.entity.Student;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

//    private final JavaMailSender mailSender;
    private final RestTemplate restTemplate = new RestTemplate();
    
//    @Value("${app.webhook.url:}")
    private String webhookUrl;
    
//    @Value("${app.frontend.verify-url}")
    private String frontendVerifyUrl;
    
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMMM d, yyyy");

    // ==================== PUBLIC API ====================

    @Async
    public void notifyDocumentReady(Document document, String downloadUrl) {
//        log.info("Notifying student {}: Document ready", document.getStudent().getEmail());
//
//        NotificationPayload payload = buildReadyPayload(document, downloadUrl);
//
//         //Send email
//        sendEmailNotification(document.getStudent(), buildReadyEmail(document, downloadUrl));
//
//         //Send webhook
//        sendWebhookNotification(payload);
    }

    @Async
    public void notifyDocumentRevoked(Document document, String reason, String adminName) {
//        log.info("Notifying student {}: Document revoked", document.getStudent().getEmail());
//
//        NotificationPayload payload = buildRevokedPayload(document, reason, adminName);
//
//         Send email
//        sendEmailNotification(document.getStudent(), buildRevokedEmail(document, reason, adminName));
//
        // Send webhook
//        sendWebhookNotification(payload);
    }

    @Async
    public void notifyDownloadAccessed(Document document, String ipAddress, String userAgent) {
//        log.info("Notifying student {}: Document accessed from IP {}",
//            document.getStudent().getEmail(), ipAddress);
//
//        NotificationPayload payload = buildAccessPayload(document, ipAddress, userAgent);
//
//         Optional: email for security alert
//         sendEmailNotification(document.getStudent(), buildAccessAlertEmail(document, ipAddress));
//
//         Always webhook
//        sendWebhookNotification(payload);
    }

    // ==================== EMAIL BUILDERS ====================

//    private EmailContent buildReadyEmail(Document document, String downloadUrl) {
//        Student student = document.getStudent();
//        String subject = String.format("Your %s is Ready for Download",
//            document.getDocumentType());
//
//        String htmlBody = String.format("""
//            <html>
//            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
//                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
//                    <h2 style="color: #1a5f7a;">%s Ready</h2>
//
//                    <p>Dear %s,</p>
//
//                    <p>Your <strong>%s in %s</strong> has been issued and is ready for download.</p>
//
//                    <div style="background: #f4f4f4; padding: 15px; border-radius: 5px; margin: 20px 0;">
//                        <p><strong>Document Details:</strong></p>
//                        <ul>
//                            <li>Document Number: <code>%s</code></li>
//                            <li>Degree: %s</li>
//                            <li>Course: %s</li>
//                            <li>Graduation Date: %s</li>
//                        </ul>
//                    </div>
//
//                    <div style="text-align: center; margin: 30px 0;">
//                        <a href="%s" style="background: #1a5f7a; color: white; padding: 12px 30px;
//                           text-decoration: none; border-radius: 5px; display: inline-block;">
//                            Download Document
//                        </a>
//                    </div>
//
//                    <p style="color: #666; font-size: 14px;">
//                        <strong>Important:</strong> This download link expires on <strong>%s</strong>.
//                        After expiration, contact your institution's registry department for a new link.
//                    </p>
//
//                    <p style="color: #666; font-size: 14px;">
//                        Verify authenticity: <a href="%s">%s</a>
//                    </p>
//
//                    <hr style="margin: 30px 0; border: none; border-top: 1px solid #ddd;">
//                    <p style="color: #999; font-size: 12px;">
//                        This is an automated message from %s. Please do not reply.
//                    </p>
//                </div>
//            </body>
//            </html>
//            """,
//            document.getDocumentType(),
//            student.getFirstName(),
//            document.getDocumentType(),
//            document.getDegree(),
//            document.getDocumentNumber(),
//            document.getDegree(),
//            document.getCourse(),
//            document.getGraduationDate().format(DATE_FORMAT),
//            downloadUrl,
//            document.getExpiresAt().format(DATE_FORMAT),
//            frontendVerifyUrl + "/" + document.getDocumentNumber(),
//            frontendVerifyUrl + "/" + document.getDocumentNumber(),
//            document.getInstitutionRecord().getInstitutionName()
//        );
//
//        String textBody = String.format("""
//            Your %s is Ready for Download
//
//            Dear %s,
//
//            Your %s in %s has been issued.
//
//            Document Number: %s
//            Degree: %s
//            Course: %s
//            Graduation Date: %s
//
//            Download: %s
//            Expires: %s
//
//            Verify: %s
//
//            %s
//            """,
//            document.getDocumentType(),
//            student.getFirstName(),
//            document.getDocumentType(),
//            document.getDegree(),
//            document.getDocumentNumber(),
//            document.getDegree(),
//            document.getCourse(),
//            document.getGraduationDate().format(DATE_FORMAT),
//            downloadUrl,
//            document.getExpiresAt().format(DATE_FORMAT),
//            frontendVerifyUrl + "/" + document.getDocumentNumber(),
//            document.getInstitutionRecord().getInstitutionName()
//        );
//
//        return new EmailContent(subject, htmlBody, textBody);
//    }

//    private EmailContent buildRevokedEmail(Document document, String reason, String adminName) {
//        Student student = document.getStudent();
//        String subject = String.format("IMPORTANT: Your %s Has Been Revoked",
//            document.getDocumentType());
//
//        String htmlBody = String.format("""
//            <html>
//            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
//                <div style="max-width: 600px; margin: 0 auto; padding: 20px;
//                            border: 3px solid #dc3545; border-radius: 5px;">
//                    <h2 style="color: #dc3545;">⚠️ Document Revoked</h2>
//
//                    <p>Dear %s,</p>
//
//                    <p>Your <strong>%s</strong> (Document Number: <code>%s</code>)
//                       has been <strong>revoked</strong> by %s.</p>
//
//                    <div style="background: #f8d7da; padding: 15px; border-radius: 5px; margin: 20px 0;">
//                        <p><strong>Reason for Revocation:</strong></p>
//                        <p style="font-style: italic;">%s</p>
//                    </div>
//
//                    <p><strong>What this means:</strong></p>
//                    <ul>
//                        <li>The document is no longer valid for verification</li>
//                        <li>Any copies you have are now void</li>
//                        <li>Third parties will see "REVOKED" status when verifying</li>
//                    </ul>
//
//                    <p>If you believe this is an error, please contact the
//                       Registry Department immediately:</p>
//                    <p>Email: registry@%s<br>
//                       Phone: %s</p>
//
//                    <hr style="margin: 30px 0; border: none; border-top: 1px solid #ddd;">
//                    <p style="color: #999; font-size: 12px;">
//                        Revoked on %s by %s<br>
//                        This is an automated security notification.
//                    </p>
//                </div>
//            </body>
//            </html>
//            """,
//            student.getFirstName(),
//            document.getDocumentType(),
//            document.getDocumentNumber(),
//            document.getInstitutionRecord().getInstitutionName(),
//            reason,
//            document.getInstitutionRecord().getWebsite(),
//            document.getInstitutionRecord().getPhoneNumber(),
//            document.getRevokedAt().format(DATE_FORMAT),
//            adminName
//        );
//
//        String textBody = String.format("""
//            ⚠️ DOCUMENT REVOKED ⚠️
//
//            Dear %s,
//
//            Your %s (Document Number: %s) has been REVOKED by %s.
//
//            REASON: %s
//
//            This document is no longer valid. Contact Registry immediately:
//            Email: registry@%s
//            Phone: %s
//
//            Revoked: %s by %s
//            """,
//            student.getFirstName(),
//            document.getDocumentType(),
//            document.getDocumentNumber(),
//            document.getInstitutionRecord().getInstitutionName(),
//            reason,
//            document.getInstitutionRecord().getWebsite(),
//            document.getInstitutionRecord().getPhoneNumber(),
//            document.getRevokedAt().format(DATE_FORMAT),
//            adminName
//        );
//
//        return new EmailContent(subject, htmlBody, textBody);
//    }

    // ==================== WEBHOOK BUILDERS ====================

//    private NotificationPayload buildReadyPayload(Document document, String downloadUrl) {
//        return NotificationPayload.builder()
//            .notificationType("DOCUMENT_READY")
//            .documentId(document.getDocumentNumber())
//            .documentType(document.getDocumentType())
//            .studentId(document.getStudent().getStudentNumber())
//            .studentEmail(document.getStudent().getEmail())
//            .studentName(document.getStudent().getFullName())
//            .downloadUrl(downloadUrl)
//            .expiresAt(document.getExpiresAt())
//            .institutionName(document.getInstitution().getName())
//            .build();
//    }

//    private NotificationPayload buildRevokedPayload(Document document, String reason, String adminName) {
//        return NotificationPayload.builder()
//            .notificationType("DOCUMENT_REVOKED")
//            .documentId(document.getDocumentNumber())
//            .documentType(document.getDocumentType())
//            .studentId(document.getStudent().getStudentNumber())
//            .studentEmail(document.getStudent().getEmail())
//            .studentName(document.getStudent().getFullName())
//            .revocationReason(reason)
//            .revokedAt(document.getRevokedAt())
//            .revokedBy(adminName)
//            .institutionName(document.getInstitution().getName())
//            .build();
//    }

//    private NotificationPayload buildAccessPayload(Document document, String ip, String userAgent) {
//        return NotificationPayload.builder()
//            .notificationType("DOCUMENT_ACCESSED")
//            .documentId(document.getDocumentNumber())
//            .documentType(document.getDocumentType())
//            .studentId(document.getStudent().getStudentNumber())
//            .studentEmail(document.getStudent().getEmail())
//            .studentName(document.getStudent().getFullName())
//            .ipAddress(ip)
//            .userAgent(userAgent)
//            .accessedAt(java.time.LocalDateTime.now())
//            .institutionName(document.getInstitution().getName())
//            .build();
//    }

    // ==================== SENDERS ====================

    private void sendEmailNotification(Student student, EmailContent content) {
//        try {
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//
//            helper.setTo(student.getEmail());
//            helper.setSubject(content.subject());
//            helper.setText(content.textBody(), content.htmlBody());
//
//            mailSender.send(message);
//            log.info("Email sent to {}", student.getEmail());
            
//        } catch (MessagingException e) {
//            log.error("Failed to send email to {}: {}", student.getEmail(), e.getMessage());
//    //         Fallback to simple text email
//            sendSimpleFallback(student.getEmail(), content);
//        }
    }

    private void sendSimpleFallback(String email, EmailContent content) {
//        try {
//            SimpleMailMessage message = new SimpleMailMessage();
//            message.setTo(email);
//            message.setSubject(content.subject());
//            message.setText(content.textBody());
//            mailSender.send(message);
//        } catch (Exception e) {
//            log.error("Fallback email also failed: {}", e.getMessage());
//        }
    }

//    private void sendWebhookNotification(NotificationPayload payload) {
//        if (webhookUrl == null || webhookUrl.isBlank()) {
//            log.warn("Webhook URL not configured, skipping webhook");
//            return;
//        }
//
//        try {
//            restTemplate.postForEntity(webhookUrl, payload, String.class);
//            log.info("Webhook notification sent for {}", payload.getDocumentId());
//        } catch (Exception e) {
//            log.error("Webhook failed: {}", e.getMessage());
//            // Don't throw — webhook is best-effort
//        }
//    }

    // ==================== INNER RECORDS ====================

    private record EmailContent(String subject, String htmlBody, String textBody) {


    }
}