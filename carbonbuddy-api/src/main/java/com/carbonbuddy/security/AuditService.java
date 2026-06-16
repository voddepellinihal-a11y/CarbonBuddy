package com.carbonbuddy.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private static final Logger auditLog = LoggerFactory.getLogger("AUDIT");

    public void logLogin(String email, boolean success, String ip) {
        AuditLog logEntry = AuditLog.builder()
                .eventType("LOGIN")
                .email(email)
                .success(success)
                .ip(ip)
                .build();
        if (success) {
            auditLog.info("LOGIN_SUCCESS: {}", logEntry);
        } else {
            auditLog.warn("LOGIN_FAILURE: {}", logEntry);
        }
    }

    public void logRegistration(String email, String ip) {
        AuditLog logEntry = AuditLog.builder()
                .eventType("REGISTRATION")
                .email(email)
                .success(true)
                .ip(ip)
                .build();
        auditLog.info("REGISTRATION: {}", logEntry);
    }

    public void logActivityCreation(Long userId, String transitMode) {
        AuditLog logEntry = AuditLog.builder()
                .eventType("ACTIVITY_CREATE")
                .userId(userId)
                .success(true)
                .details("transitMode=" + transitMode)
                .build();
        auditLog.info("ACTIVITY_CREATE: {}", logEntry);
    }

    public void logRedemption(Long userId, String itemId) {
        AuditLog logEntry = AuditLog.builder()
                .eventType("REDEMPTION")
                .userId(userId)
                .success(true)
                .details("itemId=" + itemId)
                .build();
        auditLog.info("REDEMPTION: {}", logEntry);
    }

    public void logUnauthorizedAccess(String path, String ip) {
        AuditLog logEntry = AuditLog.builder()
                .eventType("UNAUTHORIZED_ACCESS")
                .success(false)
                .ip(ip)
                .path(path)
                .build();
        auditLog.warn("UNAUTHORIZED_ACCESS: {}", logEntry);
    }
}
