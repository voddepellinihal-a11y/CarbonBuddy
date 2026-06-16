package com.carbonbuddy.service;

import com.carbonbuddy.dto.request.LoginRequest;
import com.carbonbuddy.dto.request.RegisterRequest;
import com.carbonbuddy.dto.response.AuthResponse;
import com.carbonbuddy.model.User;
import com.carbonbuddy.repository.UserRepository;
import com.carbonbuddy.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service responsible for user registration and authentication.
 * Handles password hashing, duplicate-email prevention, and JWT token generation.
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private static final String AUTH_FAILED_MSG = "Invalid email or password";
    private static final String DUPLICATE_EMAIL_MSG = "Email already registered";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Constructs AuthService with required dependencies.
     *
     * @param userRepository    the user persistence repository
     * @param passwordEncoder   the password encoder for hashing
     * @param jwtTokenProvider  the JWT token provider for authentication tokens
     */
    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Registers a new user with the provided details.
     * Returns an authentication response containing a JWT token on success.
     *
     * @param request the registration request containing user details
     * @return the authentication response with token and user info
     * @throws IllegalArgumentException if the email is already registered
     */
    public AuthResponse register(RegisterRequest request) {
        String sanitizedEmail = sanitizeInput(request.getEmail());

        if (userRepository.existsByEmail(sanitizedEmail)) {
            log.warn("Registration attempt with existing email: {}", sanitizedEmail);
            throw new IllegalArgumentException(DUPLICATE_EMAIL_MSG);
        }

        User user = new User();
        user.setEmail(sanitizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setName(sanitizeInput(request.getName()));
        user.setAge(request.getAge());
        user.setMunicipality(sanitizeInput(request.getMunicipality()));
        user.setDefaultTransitMode(sanitizeInput(request.getDefaultTransitMode()));

        user = userRepository.save(user);
        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail());

        return new AuthResponse(token, user.getId(), user.getEmail(), user.getName());
    }

    /**
     * Authenticates a user with email and password.
     * Returns an authentication response containing a JWT token on success.
     *
     * @param request the login request containing credentials
     * @return the authentication response with token and user info
     * @throws IllegalArgumentException if credentials are invalid
     */
    public AuthResponse login(LoginRequest request) {
        String sanitizedEmail = sanitizeInput(request.getEmail());

        User user = userRepository.findByEmail(sanitizedEmail)
                .orElseThrow(() -> {
                    log.warn("Login attempt for unknown email: {}", sanitizedEmail);
                    return new IllegalArgumentException(AUTH_FAILED_MSG);
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Invalid password attempt for email: {}", sanitizedEmail);
            throw new IllegalArgumentException(AUTH_FAILED_MSG);
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail());
        return new AuthResponse(token, user.getId(), user.getEmail(), user.getName());
    }

    /**
     * Sanitizes a string input by trimming whitespace and applying max length.
     *
     * @param input the raw input string
     * @return the sanitized string, or null if input is null
     */
    private String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        return input.trim().substring(0, Math.min(input.trim().length(), 255));
    }
}
