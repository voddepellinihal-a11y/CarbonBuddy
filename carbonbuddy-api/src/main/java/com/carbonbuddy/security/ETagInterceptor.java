package com.carbonbuddy.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class ETagInterceptor implements HandlerInterceptor {

    private static final String ETAG_HEADER = "ETag";
    private static final String IF_NONE_MATCH_HEADER = "If-None-Match";
    private static final int NOT_MODIFIED_STATUS = 304;

    private final ThreadLocal<String> responseBodyHash = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return;
        }

        if (response.getStatus() != 200) {
            return;
        }

        String etag = response.getHeader(ETAG_HEADER);
        if (etag == null) {
            String clientEtag = request.getHeader(IF_NONE_MATCH_HEADER);
            if (clientEtag != null && !clientEtag.isEmpty()) {
                response.setStatus(NOT_MODIFIED_STATUS);
                response.setContentLength(0);
            }
        }
    }

    public static String generateETag(byte[] body) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest(body);
            return "\"" + HexFormat.of().formatHex(hash) + "\"";
        } catch (NoSuchAlgorithmException e) {
            return "\"" + System.currentTimeMillis() + "\"";
        }
    }
}
