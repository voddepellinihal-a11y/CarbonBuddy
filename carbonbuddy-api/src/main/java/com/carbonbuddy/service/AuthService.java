package com.carbonbuddy.service;

import com.carbonbuddy.dto.request.LoginRequest;
import com.carbonbuddy.dto.request.RegisterRequest;
import com.carbonbuddy.dto.response.AuthResponse;
import com.carbonbuddy.model.User;
import com.carbonbuddy.repository.UserRepository;
import com.carbonbuddy.security.AuditService;
import com.carbonbuddy.security.JwtTokenProvider;
import com.carbonbuddy.security.InputSanitizer;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private static final String AUTH_FAILED_MSG = "Invalid email or password";
    private static final String DUPLICATE_EMAIL_MSG = "Email already registered";
    private static final String PASSWORD_POLICY_MSG = "Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, one digit, and one special character (!@#$%^&*)";

    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHAR = Pattern.compile("[!@#$%^&*]");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuditService auditService;
    private final HttpServletRequest httpServletRequest;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       AuditService auditService,
                       HttpServletRequest httpServletRequest) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.auditService = auditService;
        this.httpServletRequest = httpServletRequest;
    }

    public AuthResponse register(RegisterRequest request) {
        String sanitizedEmail = InputSanitizer.sanitizeEmail(request.getEmail());

        validatePasswordPolicy(request.getPassword());

        if (userRepository.existsByEmail(sanitizedEmail)) {
            log.warn("Registration attempt with existing email: {}", sanitizedEmail);
            throw new IllegalArgumentException(DUPLICATE_EMAIL_MSG);
        }

        User user = new User();
        user.setEmail(sanitizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setName(InputSanitizer.sanitize(request.getName()));
        user.setAge(request.getAge());
        user.setMunicipality(InputSanitizer.sanitize(request.getMunicipality()));
        user.setDefaultTransitMode(InputSanitizer.sanitize(request.getDefaultTransitMode()));

        user = userRepository.save(user);
        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail());
        String refreshToken = jwtTokenProvider.generateToken(user.getId(), user.getEmail());

        auditService.logRegistration(sanitizedEmail, getClientIp());

        return new AuthResponse(token, refreshToken, user.getId(), user.getEmail(), user.getName());
    }

    public AuthResponse login(LoginRequest request) {
        String sanitizedEmail = InputSanitizer.sanitizeEmail(request.getEmail());

        User user = userRepository.findByEmail(sanitizedEmail)
                .orElseThrow(() -> {
                    log.warn("Login attempt for unknown email: {}", sanitizedEmail);
                    auditService.logLogin(sanitizedEmail, false, getClientIp());
                    return new IllegalArgumentException(AUTH_FAILED_MSG);
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Invalid password attempt for email: {}", sanitizedEmail);
            auditService.logLogin(sanitizedEmail, false, getClientIp());
            throw new IllegalArgumentException(AUTH_FAILED_MSG);
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail());
        String refreshToken = jwtTokenProvider.generateToken(user.getId(), user.getEmail());

        auditService.logLogin(sanitizedEmail, true, getClientIp());

        return new AuthResponse(token, refreshToken, user.getId(), user.getEmail(), user.getName());
    }

    public AuthResponse refreshToken(String expiredToken) {
        if (!jwtTokenProvider.validateTokenForRefresh(expiredToken)) {
            throw new IllegalArgumentException("Token is not eligible for refresh");
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(expiredToken);
        String email = jwtTokenProvider.getEmailFromToken(expiredToken);

        String newToken = jwtTokenProvider.generateToken(userId, email);
        String newRefreshToken = jwtTokenProvider.generateToken(userId, email);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return new AuthResponse(newToken, newRefreshToken, user.getId(), user.getEmail(), user.getName());
    }

    private void validatePasswordPolicy(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException(PASSWORD_POLICY_MSG);
        }
        if (!UPPERCASE.matcher(password).find()) {
            throw new IllegalArgumentException(PASSWORD_POLICY_MSG);
        }
        if (!LOWERCASE.matcher(password).find()) {
            throw new IllegalArgumentException(PASSWORD_POLICY_MSG);
        }
        if (!DIGIT.matcher(password).find()) {
            throw new IllegalArgumentException(PASSWORD_POLICY_MSG);
        }
        if (!SPECIAL_CHAR.matcher(password).find()) {
            throw new IllegalArgumentException(PASSWORD_POLICY_MSG);
        }
    }

    private String getClientIp() {
        String xForwardedFor = httpServletRequest.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return httpServletRequest.getRemoteAddr();
    }
}
