package com.enzelascripts.securediv.service;

import com.enzelascripts.securediv.entity.AccessLog;
import com.enzelascripts.securediv.entity.Student;
import com.enzelascripts.securediv.exception.OperationalException;
import com.enzelascripts.securediv.exception.ResourceNotFoundException;
import com.enzelascripts.securediv.repository.AccessLogRepo;
import com.enzelascripts.securediv.repository.StudentRepo;
import com.enzelascripts.securediv.request.DocumentDownloadRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccessLogService {

    @Autowired
    private AccessLogRepo accessLogRepo;
    @Autowired
    HttpServletRequest request;
    @Autowired
    private StudentRepo studentRepo;

    @Async
    public void logAccess(@NonNull DocumentDownloadRequest d, String action) {

        List<String> commonDomain = new ArrayList<>(List.of("gmail.com", "yahoo.com",
                "outlook.com", "hotmail.com", "live.com", "proton.me", "protonmail.com", "zoho.com",
                "aol.com", "yandex.com", "icloud.com", "gmx.com", "mail.com", "inbox.ru"));

        //ensure the email domain is not a generic one
        String officialEmail = d.getCompanyEmail();
        String emailDomain = officialEmail.split("@")[1].toLowerCase();
        if(commonDomain.contains(emailDomain))
            throw new OperationalException("Please enter official email address");

        //if a student, find student; send downloadURL to their official email
        String studentId = d.getStudentId();
        Student student;

        if(studentId != null){
            student = studentRepo.findStudentByStudentId(studentId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Student Record Not Found"));
            officialEmail = student.getEmail();
        }


        try {
            HttpServletRequest req = request != null ? request : getCurrentRequest();

            String principal = "ANONYMOUS";
            if (req != null && req.getUserPrincipal() != null) {
                principal = req.getUserPrincipal().getName();
            }

            AccessLog logEntry = AccessLog.builder()
                    .documentNumber(d.getDocumentNumber())
                    .action(action)
                    .name(d.getRequesterName())
                    .studentId(studentId)
                    .requesterEmail(officialEmail)
                    .ipAddress(getClientIp(req))
                    .userAgent(getClientUserAgent(req))
                    .principalUser(principal)
                    .timestamp(LocalDateTime.now())
                    .purpose(d.getPurpose())
                    .build();

            accessLogRepo.save(logEntry);

            log.info("successfully registered " + action + " AccessLog for document: " + d.getDocumentNumber());

        } catch (Exception e) {
            log.error("Failed to log access", e);
        }
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attr =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        return attr != null ? attr.getRequest() : null;
    }

    private String getClientIp(HttpServletRequest request) {
        try {
            if (request == null) return "UNKNOWN";

            String forwarded = request.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isEmpty()) {
                return forwarded.split(",")[0].trim();
            }

            return request.getRemoteAddr();

        } catch (Exception e) {
            log.warn("Could not get client IP", e);
            return "UNKNOWN";
        }
    }

    private String getClientUserAgent(HttpServletRequest request) {
        try {
            return request != null ? request.getHeader("User-Agent") : "UNKNOWN";
        } catch (Exception e) {
            log.warn("Could not get user agent", e);
            return "UNKNOWN";
        }
    }
}