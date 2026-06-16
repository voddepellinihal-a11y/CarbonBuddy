package com.carbonbuddy.dto.response;

public class AuthResponse {

    private static final String TOKEN_TYPE = "Bearer";

    private final String token;
    private final String refreshToken;
    private final String tokenType = TOKEN_TYPE;
    private final Long userId;
    private final String email;
    private final String name;

    public AuthResponse(String token, String refreshToken, Long userId, String email, String name) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.email = email;
        this.name = name;
    }

    public String getToken() { return token; }
    public String getRefreshToken() { return refreshToken; }
    public String getTokenType() { return tokenType; }
    public Long getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getName() { return name; }
}
