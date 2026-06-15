package com.carbonbuddy.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;

class SecurityUtilTest {

    @Test
    void getCurrentUserId_shouldReturnUserId() {
        Authentication auth = new UsernamePasswordAuthenticationToken(42L, null);
        assertEquals(42L, SecurityUtil.getCurrentUserId(auth));
    }

    @Test
    void getCurrentUserId_shouldThrowForNullAuth() {
        assertThrows(IllegalArgumentException.class, () -> SecurityUtil.getCurrentUserId(null));
    }

    @Test
    void getCurrentUserId_shouldThrowForAnonymous() {
        Authentication auth = new UsernamePasswordAuthenticationToken("anonymousUser", null);
        assertThrows(IllegalArgumentException.class, () -> SecurityUtil.getCurrentUserId(auth));
    }

    @Test
    void getCurrentUserId_shouldThrowForInvalidPrincipal() {
        Authentication auth = new UsernamePasswordAuthenticationToken(3.14, null);
        assertThrows(IllegalArgumentException.class, () -> SecurityUtil.getCurrentUserId(auth));
    }
}
