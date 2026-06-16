package com.carbonbuddy.dto.response;

/**
 * Response DTO returned after successful authentication.
 * Contains the JWT token and basic user information.
 */
public class AuthResponse {

    private static final String TOKEN_TYPE = "Bearer";

    private final String token;
    private final String tokenType = TOKEN_TYPE;
    private final Long userId;
    private final String email;
    private final String name;

    /**
     * Constructs an AuthResponse with all fields.
     *
     * @param token  the JWT token
     * @param userId the user ID
     * @param email  the user email
     * @param name   the user name
     */
    public AuthResponse(String token, Long userId, String email, String name) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.name = name;
    }

    public String getToken() { return token; }
    public String getTokenType() { return tokenType; }
    public Long getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getName() { return name; }
}
