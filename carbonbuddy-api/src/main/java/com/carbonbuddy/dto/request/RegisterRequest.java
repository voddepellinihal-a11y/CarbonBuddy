package com.carbonbuddy.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for user registration.
 * Validates email format, password strength, name, age, and optional profile fields.
 */
public class RegisterRequest {

    private static final int MAX_EMAIL_LENGTH = 100;
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 128;
    private static final int MAX_NAME_LENGTH = 100;
    private static final int MIN_AGE = 1;
    private static final int MAX_AGE = 150;
    private static final int MAX_MUNICIPALITY_LENGTH = 100;
    private static final int MAX_TRANSIT_MODE_LENGTH = 50;

    @NotBlank
    @Email
    @Size(max = MAX_EMAIL_LENGTH)
    private String email;

    @NotBlank
    @Size(min = MIN_PASSWORD_LENGTH, max = MAX_PASSWORD_LENGTH)
    private String password;

    @NotBlank
    @Size(max = MAX_NAME_LENGTH)
    private String name;

    @Min(value = MIN_AGE, message = "Age must be at least 1")
    @Max(value = MAX_AGE, message = "Age must not exceed 150")
    private Integer age;

    @Size(max = MAX_MUNICIPALITY_LENGTH)
    private String municipality;

    @Size(max = MAX_TRANSIT_MODE_LENGTH)
    private String defaultTransitMode;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public String getMunicipality() { return municipality; }
    public void setMunicipality(String municipality) { this.municipality = municipality; }
    public String getDefaultTransitMode() { return defaultTransitMode; }
    public void setDefaultTransitMode(String defaultTransitMode) { this.defaultTransitMode = defaultTransitMode; }
}
