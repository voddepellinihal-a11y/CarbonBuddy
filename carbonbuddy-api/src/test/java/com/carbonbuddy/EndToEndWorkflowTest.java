package com.carbonbuddy;

import com.carbonbuddy.dto.request.ActivityRequest;
import com.carbonbuddy.dto.request.LoginRequest;
import com.carbonbuddy.dto.request.RegisterRequest;
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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EndToEndWorkflowTest {

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

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String VALID_PASSWORD = "Password1!";

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void should_completeFullWorkflow_registerToDashboard() throws Exception {
        RegisterRequest registerReq = new RegisterRequest();
        registerReq.setEmail("e2e@example.com");
        registerReq.setPassword(VALID_PASSWORD);
        registerReq.setName("E2E User");

        String registerResponse = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(registerResponse).path("data").get("token").asText();

        mockMvc.perform(get("/api/v1/analytics/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/v1/leaderboard")
                        .header("Authorization", "Bearer " + token)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.leaderboard").isArray())
                .andExpect(jsonPath("$.data.myRank").isNumber())
                .andExpect(jsonPath("$.data.totalUsers").isNumber());

        mockMvc.perform(get("/api/v1/rewards/store")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.balance").isNumber());

        mockMvc.perform(get("/api/v1/recommendations")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void should_protectAllEndpoints_when_noTokenProvided() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/dashboard"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status == 401 || status == 403,
                            "Expected 401 or 403 but got " + status);
                });

        mockMvc.perform(get("/api/v1/leaderboard")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status == 401 || status == 403,
                            "Expected 401 or 403 but got " + status);
                });

        mockMvc.perform(get("/api/v1/rewards/store"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status == 401 || status == 403,
                            "Expected 401 or 403 but got " + status);
                });

        mockMvc.perform(get("/api/v1/recommendations"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status == 401 || status == 403,
                            "Expected 401 or 403 but got " + status);
                });
    }

    @Test
    void should_returnSecurityHeaders_when_makingRequest() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(header().exists("X-Content-Type-Options"))
                .andExpect(header().exists("X-Frame-Options"))
                .andExpect(header().exists("X-XSS-Protection"))
                .andExpect(header().exists("Strict-Transport-Security"));
    }

    @Test
    void should_completeFullDashboardFlow_when_registered() throws Exception {
        RegisterRequest registerReq = new RegisterRequest();
        registerReq.setEmail("dashboard@example.com");
        registerReq.setPassword(VALID_PASSWORD);
        registerReq.setName("Dashboard User");

        String registerResponse = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(registerResponse).path("data").get("token").asText();

        mockMvc.perform(get("/api/v1/analytics/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.daily.totalCarbonKg").isNumber())
                .andExpect(jsonPath("$.data.weekly.totalCarbonKg").isNumber())
                .andExpect(jsonPath("$.data.monthly.totalCarbonKg").isNumber())
                .andExpect(jsonPath("$.data.recommendations").isArray())
                .andExpect(jsonPath("$.data.benchmarks").isArray());

        mockMvc.perform(get("/api/v1/leaderboard")
                        .header("Authorization", "Bearer " + token)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.myPoints").isNumber());

        mockMvc.perform(get("/api/v1/rewards/store")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.balance").isNumber());

        mockMvc.perform(get("/api/v1/recommendations")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(post("/api/v1/recommendations/generate")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void should_failLogin_when_wrongPassword() throws Exception {
        RegisterRequest registerReq = new RegisterRequest();
        registerReq.setEmail("loginfail@example.com");
        registerReq.setPassword(VALID_PASSWORD);
        registerReq.setName("Login Fail User");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated());

        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail("loginfail@example.com");
        loginReq.setPassword("WrongPass1!");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status == 400 || status == 401,
                            "Expected 400 or 401 but got " + status);
                });
    }

    @Test
    void should_failRegister_when_duplicateEmail() throws Exception {
        RegisterRequest registerReq = new RegisterRequest();
        registerReq.setEmail("duplicate@example.com");
        registerReq.setPassword(VALID_PASSWORD);
        registerReq.setName("First User");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated());

        RegisterRequest duplicateReq = new RegisterRequest();
        duplicateReq.setEmail("duplicate@example.com");
        duplicateReq.setPassword("Another1!");
        duplicateReq.setName("Second User");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateReq)))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status == 400 || status == 409,
                            "Expected 400 or 409 but got " + status);
                });
    }

    @Test
    void should_registerWithAllFields_when_validRequest() throws Exception {
        RegisterRequest registerReq = new RegisterRequest();
        registerReq.setEmail("full@example.com");
        registerReq.setPassword(VALID_PASSWORD);
        registerReq.setName("Full User");
        registerReq.setAge(30);
        registerReq.setMunicipality("Mumbai");
        registerReq.setDefaultTransitMode("METRO");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email").value("full@example.com"))
                .andExpect(jsonPath("$.data.name").value("Full User"));
    }
}
