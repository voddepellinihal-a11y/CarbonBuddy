package com.carbonbuddy.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigTest {

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

    @Test
    void should_return200_when_accessingHealthEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
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
    void should_denyFrameEmbedding() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(header().string("X-Frame-Options", "DENY"));
    }

    @Test
    void should_setNoSniffContentTypes() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    @Test
    void should_setCacheControlHeaders() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(header().exists("Cache-Control"));
    }

    @Test
    void should_setReferrerPolicy() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(header().exists("Referrer-Policy"));
    }

    @Test
    void should_setPermissionsPolicy() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(header().exists("Permissions-Policy"));
    }

    @Test
    void should_rejectProtectedEndpoint_when_noToken() throws Exception {
        mockMvc.perform(get("/api/v1/leaderboard")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assert (status == 401 || status == 403);
                });
    }

    @Test
    void should_setCrossOriginHeaders() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(header().exists("Cross-Origin-Opener-Policy"));
    }
}
