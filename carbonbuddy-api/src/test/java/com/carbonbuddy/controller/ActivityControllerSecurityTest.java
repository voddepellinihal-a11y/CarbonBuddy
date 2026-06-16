package com.carbonbuddy.controller;

import com.carbonbuddy.dto.request.ActivityRequest;
import com.carbonbuddy.model.User;
import com.carbonbuddy.repository.UserRepository;
import com.carbonbuddy.security.JwtTokenProvider;
import com.carbonbuddy.security.RateLimitFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ActivityControllerSecurityTest {

    @org.springframework.boot.test.context.TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public RateLimitFilter rateLimitFilter() {
            return new RateLimitFilter() {
                @Override
                protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                        throws ServletException, IOException {
                    filterChain.doFilter(request, response);
                }
            };
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void should_return401_when_noTokenProvided() throws Exception {
        ActivityRequest request = buildActivityRequest();

        mockMvc.perform(post("/api/v1/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void should_return201_when_validTokenProvided() throws Exception {
        User user = createUser("activity@example.com");
        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail());

        ActivityRequest request = buildActivityRequest();

        mockMvc.perform(post("/api/v1/activities")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNotEmpty());
    }

    @Test
    void should_return401_when_expiredTokenProvided() throws Exception {
        User user = createUser("expired@example.com");

        com.carbonbuddy.config.JwtProperties props = new com.carbonbuddy.config.JwtProperties();
        props.setSecret(java.util.Base64.getEncoder().encodeToString(
                "test-secret-key-for-unit-tests-only-1234567890".getBytes()));
        props.setExpirationMs(-1);
        JwtTokenProvider shortLivedProvider = new JwtTokenProvider(props);
        String expiredToken = shortLivedProvider.generateToken(user.getId(), user.getEmail());

        ActivityRequest request = buildActivityRequest();

        mockMvc.perform(post("/api/v1/activities")
                        .header("Authorization", "Bearer " + expiredToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void should_return401_when_invalidTokenProvided() throws Exception {
        ActivityRequest request = buildActivityRequest();

        mockMvc.perform(post("/api/v1/activities")
                        .header("Authorization", "Bearer invalid.token.here")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void should_return401_when_malformedAuthorizationHeader() throws Exception {
        ActivityRequest request = buildActivityRequest();

        mockMvc.perform(post("/api/v1/activities")
                        .header("Authorization", "Token somevalue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void should_return401_when_emptyAuthorizationHeader() throws Exception {
        ActivityRequest request = buildActivityRequest();

        mockMvc.perform(post("/api/v1/activities")
                        .header("Authorization", "")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void should_return401_when_tokenWithoutBearerPrefix() throws Exception {
        User user = createUser("nobearer@example.com");
        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail());

        ActivityRequest request = buildActivityRequest();

        mockMvc.perform(post("/api/v1/activities")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void should_return400_when_invalidActivityRequest() throws Exception {
        User user = createUser("invalid@example.com");
        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail());

        mockMvc.perform(post("/api/v1/activities")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    private User createUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode("Password1!"));
        user.setName("Test User");
        return userRepository.save(user);
    }

    private ActivityRequest buildActivityRequest() {
        ActivityRequest request = new ActivityRequest();
        request.setTransitMode("BUS");
        request.setDistanceKm(10.0);
        request.setDurationMinutes(30.0);
        request.setActivityStart(LocalDateTime.now());
        request.setManual(true);
        return request;
    }
}
