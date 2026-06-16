package com.carbonbuddy.security;

import java.time.LocalDateTime;

public class AuditLog {

    private final LocalDateTime timestamp;
    private final String eventType;
    private final String email;
    private final Long userId;
    private final boolean success;
    private final String ip;
    private final String path;
    private final String details;

    private AuditLog(Builder builder) {
        this.timestamp = builder.timestamp;
        this.eventType = builder.eventType;
        this.email = builder.email;
        this.userId = builder.userId;
        this.success = builder.success;
        this.ip = builder.ip;
        this.path = builder.path;
        this.details = builder.details;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public String getEventType() { return eventType; }
    public String getEmail() { return email; }
    public Long getUserId() { return userId; }
    public boolean isSuccess() { return success; }
    public String getIp() { return ip; }
    public String getPath() { return path; }
    public String getDetails() { return details; }

    @Override
    public String toString() {
        return String.format("[%s] %s | email=%s | userId=%s | success=%s | ip=%s | path=%s | details=%s",
                timestamp, eventType, email, userId, success, ip, path, details);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private LocalDateTime timestamp = LocalDateTime.now();
        private String eventType;
        private String email;
        private Long userId;
        private boolean success;
        private String ip;
        private String path;
        private String details;

        public Builder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }
        public Builder eventType(String eventType) { this.eventType = eventType; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder userId(Long userId) { this.userId = userId; return this; }
        public Builder success(boolean success) { this.success = success; return this; }
        public Builder ip(String ip) { this.ip = ip; return this; }
        public Builder path(String path) { this.path = path; return this; }
        public Builder details(String details) { this.details = details; return this; }

        public AuditLog build() {
            return new AuditLog(this);
        }
    }
}
