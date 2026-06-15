package com.carbonbuddy.security;

import org.springframework.security.core.Authentication;

public final class SecurityUtil {

    private static final String ANONYMOUS_USER = "anonymousUser";

    private SecurityUtil() {
    }

    public static Long getCurrentUserId(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) {
            throw new IllegalArgumentException("Authentication required");
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof Long) {
            return (Long) principal;
        }
        if (principal instanceof String && ANONYMOUS_USER.equals(principal)) {
            throw new IllegalArgumentException("Authentication required");
        }
        throw new IllegalArgumentException("Invalid principal type: " + principal.getClass().getName());
    }
}
