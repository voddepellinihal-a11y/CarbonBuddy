package com.carbonbuddy.security;

import com.carbonbuddy.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private static final String SECRET = Base64.getEncoder().encodeToString(
            "a-secure-secret-key-at-least-32-bytes-long-for-hmac-sha".getBytes());
    private static final long EXPIRATION_MS = 86400000;

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties();
        props.setSecret(SECRET);
        props.setExpirationMs(EXPIRATION_MS);
        jwtTokenProvider = new JwtTokenProvider(props);
    }

    @Test
    void should_generateToken_when_validUserIdAndEmail() {
        String token = jwtTokenProvider.generateToken(1L, "user@example.com");

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void should_parseUserId_when_tokenIsValid() {
        String token = jwtTokenProvider.generateToken(42L, "user@example.com");

        Long userId = jwtTokenProvider.getUserIdFromToken(token);

        assertEquals(42L, userId);
    }

    @Test
    void should_parseEmail_when_tokenIsValid() {
        String token = jwtTokenProvider.generateToken(1L, "test@example.com");

        String email = jwtTokenProvider.getEmailFromToken(token);

        assertEquals("test@example.com", email);
    }

    @Test
    void should_validateToken_when_tokenIsValid() {
        String token = jwtTokenProvider.generateToken(1L, "user@example.com");

        boolean valid = jwtTokenProvider.validateToken(token);

        assertTrue(valid);
    }

    @Test
    void should_rejectExpiredToken() {
        JwtProperties props = new JwtProperties();
        props.setSecret(SECRET);
        props.setExpirationMs(-1);
        JwtTokenProvider expiredProvider = new JwtTokenProvider(props);

        String token = expiredProvider.generateToken(1L, "user@example.com");

        assertFalse(jwtTokenProvider.validateToken(token));
    }

    @Test
    void should_rejectMalformedToken() {
        assertFalse(jwtTokenProvider.validateToken("not-a-valid-jwt"));
    }

    @Test
    void should_rejectEmptyToken() {
        assertFalse(jwtTokenProvider.validateToken(""));
    }

    @Test
    void should_rejectNullToken() {
        assertFalse(jwtTokenProvider.validateToken(null));
    }

    @Test
    void should_rejectTokenWithInvalidSignature() {
        JwtProperties props2 = new JwtProperties();
        String secret2 = Base64.getEncoder().encodeToString(
                "another-different-secret-key-that-is-long-enough-for-hmac".getBytes());
        props2.setSecret(secret2);
        props2.setExpirationMs(EXPIRATION_MS);
        JwtTokenProvider otherProvider = new JwtTokenProvider(props2);

        String token = otherProvider.generateToken(1L, "user@example.com");

        assertFalse(jwtTokenProvider.validateToken(token));
    }

    @Test
    void should_rejectTamperedToken() {
        String token = jwtTokenProvider.generateToken(1L, "user@example.com");
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";

        assertFalse(jwtTokenProvider.validateToken(tampered));
    }

    @Test
    void should_returnCorrectUserId_when_tokenWithDifferentUsers() {
        String token1 = jwtTokenProvider.generateToken(10L, "a@example.com");
        String token2 = jwtTokenProvider.generateToken(99L, "b@example.com");

        assertEquals(10L, jwtTokenProvider.getUserIdFromToken(token1));
        assertEquals(99L, jwtTokenProvider.getUserIdFromToken(token2));
    }

    @Test
    void should_acceptTokenWithinRefreshWindow_when_tokenIsCloseToExpiry() {
        JwtProperties props = new JwtProperties();
        props.setSecret(SECRET);
        props.setExpirationMs(1800000);
        JwtTokenProvider shortWindowProvider = new JwtTokenProvider(props);

        String token = shortWindowProvider.generateToken(1L, "user@example.com");

        assertTrue(shortWindowProvider.validateTokenForRefresh(token));
    }

    @Test
    void should_rejectExpiredTokenForRefresh_when_tokenExpiredTooLong() {
        JwtProperties props = new JwtProperties();
        props.setSecret(SECRET);
        props.setExpirationMs(-7200000);
        JwtTokenProvider expiredProvider = new JwtTokenProvider(props);

        String token = expiredProvider.generateToken(1L, "user@example.com");

        assertFalse(jwtTokenProvider.validateTokenForRefresh(token));
    }

    @Test
    void should_throwException_when_parsingMalformedToken() {
        assertThrows(Exception.class,
                () -> jwtTokenProvider.getUserIdFromToken("invalid.token.here"));
    }
}
