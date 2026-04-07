package com.enzelascripts.securediv.service;

import com.enzelascripts.securediv.entity.AccessLog;
import com.enzelascripts.securediv.repository.AccessLogRepo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccessLogService {

    @Autowired
    private AccessLogRepo accessLogRepo;

    @Async
    public void logAccess(HttpServletRequest request, String documentNumber, String action) {

        try {
            HttpServletRequest req = request != null ? request : getCurrentRequest();

            String principal = "ANONYMOUS";
            if (req != null && req.getUserPrincipal() != null) {
                principal = req.getUserPrincipal().getName();
            }

            AccessLog logEntry = AccessLog.builder()
                    .documentNumber(documentNumber)
                    .action(action)
                    .ipAddress(getClientIp(req))
                    .userAgent(getClientUserAgent(req))
                    .principalUser(principal)
                    .timestamp(LocalDateTime.now())
                    .build();

            accessLogRepo.save(logEntry);

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