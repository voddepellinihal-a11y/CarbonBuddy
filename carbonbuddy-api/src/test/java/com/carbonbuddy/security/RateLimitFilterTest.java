package com.carbonbuddy.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RateLimitFilterTest {

    private RateLimitFilter rateLimitFilter;

    @BeforeEach
    void setUp() {
        rateLimitFilter = new RateLimitFilter();
    }

    @Test
    void should_allowRequest_when_withinLimit() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        rateLimitFilter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        assertEquals(200, response.getStatus());
    }

    @Test
    void should_return429_when_unauthenticatedRequestExceedsLimit() throws ServletException, IOException {
        String clientIp = "192.168.1.100";
        FilterChain chain = mock(FilterChain.class);

        for (int i = 0; i < 20; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
            request.setRemoteAddr(clientIp);
            MockHttpServletResponse response = new MockHttpServletResponse();
            rateLimitFilter.doFilterInternal(request, response, chain);
        }

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        request.setRemoteAddr(clientIp);
        MockHttpServletResponse response = new MockHttpServletResponse();
        rateLimitFilter.doFilterInternal(request, response, chain);

        assertEquals(429, response.getStatus());
    }

    @Test
    void should_haveSeparateCounters_for_differentIPs() throws ServletException, IOException {
        String ip1 = "10.0.0.1";
        String ip2 = "10.0.0.2";
        FilterChain chain = mock(FilterChain.class);

        for (int i = 0; i < 20; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
            request.setRemoteAddr(ip1);
            MockHttpServletResponse response = new MockHttpServletResponse();
            rateLimitFilter.doFilterInternal(request, response, chain);
        }

        MockHttpServletRequest request2 = new MockHttpServletRequest("GET", "/api/test");
        request2.setRemoteAddr(ip2);
        MockHttpServletResponse response2 = new MockHttpServletResponse();
        rateLimitFilter.doFilterInternal(request2, response2, chain);

        verify(chain, times(1)).doFilter(request2, response2);
        assertEquals(200, response2.getStatus());
    }

    @Test
    void should_useXForwardedFor_when_present() throws ServletException, IOException {
        String forwardedIp = "172.16.0.1";
        FilterChain chain = mock(FilterChain.class);

        for (int i = 0; i < 20; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
            request.addHeader("X-Forwarded-For", forwardedIp);
            MockHttpServletResponse response = new MockHttpServletResponse();
            rateLimitFilter.doFilterInternal(request, response, chain);
        }

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        request.addHeader("X-Forwarded-For", forwardedIp);
        MockHttpServletResponse response = new MockHttpServletResponse();
        rateLimitFilter.doFilterInternal(request, response, chain);

        assertEquals(429, response.getStatus());
    }

    @Test
    void should_useFirstIP_when_multipleForwardedFor() throws ServletException, IOException {
        String firstIp = "1.2.3.4";
        FilterChain chain = mock(FilterChain.class);

        for (int i = 0; i < 20; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
            request.addHeader("X-Forwarded-For", firstIp + ", 5.6.7.8");
            MockHttpServletResponse response = new MockHttpServletResponse();
            rateLimitFilter.doFilterInternal(request, response, chain);
        }

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        request.addHeader("X-Forwarded-For", firstIp + ", 5.6.7.8");
        MockHttpServletResponse response = new MockHttpServletResponse();
        rateLimitFilter.doFilterInternal(request, response, chain);

        assertEquals(429, response.getStatus());
    }

    @Test
    void should_returnJsonError_when_rateLimited() throws ServletException, IOException {
        String clientIp = "192.168.2.200";
        FilterChain chain = mock(FilterChain.class);

        for (int i = 0; i < 21; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
            request.setRemoteAddr(clientIp);
            MockHttpServletResponse response = new MockHttpServletResponse();
            rateLimitFilter.doFilterInternal(request, response, chain);
        }

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        request.setRemoteAddr(clientIp);
        MockHttpServletResponse response = new MockHttpServletResponse();
        rateLimitFilter.doFilterInternal(request, response, chain);

        assertEquals("application/json", response.getContentType());
        assertTrue(response.getContentAsString().contains("Too Many Requests"));
    }

    @Test
    void should_setCorrectMessage_when_rateLimited() throws ServletException, IOException {
        String clientIp = "192.168.3.300";
        FilterChain chain = mock(FilterChain.class);

        for (int i = 0; i < 21; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
            request.setRemoteAddr(clientIp);
            MockHttpServletResponse response = new MockHttpServletResponse();
            rateLimitFilter.doFilterInternal(request, response, chain);
        }

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        request.setRemoteAddr(clientIp);
        MockHttpServletResponse response = new MockHttpServletResponse();
        rateLimitFilter.doFilterInternal(request, response, chain);

        assertTrue(response.getContentAsString().contains("Rate limit exceeded"));
    }
}
