package com.carbonbuddy.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuditServiceTest {

    private AuditService auditService;

    @BeforeEach
    void setUp() {
        auditService = new AuditService();
    }

    @Test
    void should_logLoginSuccess_when_calledWithSuccess() {
        assertDoesNotThrow(() -> auditService.logLogin("user@example.com", true, "127.0.0.1"));
    }

    @Test
    void should_logLoginFailure_when_calledWithFailure() {
        assertDoesNotThrow(() -> auditService.logLogin("user@example.com", false, "127.0.0.1"));
    }

    @Test
    void should_logRegistration_when_called() {
        assertDoesNotThrow(() -> auditService.logRegistration("new@example.com", "192.168.1.1"));
    }

    @Test
    void should_logActivityCreation_when_called() {
        assertDoesNotThrow(() -> auditService.logActivityCreation(1L, "BUS"));
    }

    @Test
    void should_logRedemption_when_called() {
        assertDoesNotThrow(() -> auditService.logRedemption(1L, "1"));
    }

    @Test
    void should_logUnauthorizedAccess_when_called() {
        assertDoesNotThrow(() -> auditService.logUnauthorizedAccess("/api/secret", "10.0.0.1"));
    }

    @Test
    void should_handleNullEmail_when_loggingLogin() {
        assertDoesNotThrow(() -> auditService.logLogin(null, true, "127.0.0.1"));
    }

    @Test
    void should_handleNullIp_when_loggingLogin() {
        assertDoesNotThrow(() -> auditService.logLogin("user@example.com", true, null));
    }

    @Test
    void should_handleNullEmail_when_loggingRegistration() {
        assertDoesNotThrow(() -> auditService.logRegistration(null, "127.0.0.1"));
    }

    @Test
    void should_handleNullTransitMode_when_loggingActivity() {
        assertDoesNotThrow(() -> auditService.logActivityCreation(1L, null));
    }

    @Test
    void should_handleNullPath_when_loggingUnauthorized() {
        assertDoesNotThrow(() -> auditService.logUnauthorizedAccess(null, "10.0.0.1"));
    }

    @Test
    void should_handleEmptyEmail_when_loggingLogin() {
        assertDoesNotThrow(() -> auditService.logLogin("", true, "127.0.0.1"));
    }

    @Test
    void should_handleEmptyIp_when_loggingLogin() {
        assertDoesNotThrow(() -> auditService.logLogin("user@example.com", true, ""));
    }
}
