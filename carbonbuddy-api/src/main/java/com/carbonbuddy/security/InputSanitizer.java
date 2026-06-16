package com.carbonbuddy.security;

public final class InputSanitizer {

    private static final int MAX_NAME_LENGTH = 255;
    private static final int MAX_EMAIL_LENGTH = 100;

    private InputSanitizer() {
    }

    public static String sanitize(String input) {
        if (input == null) {
            return null;
        }
        String result = input.trim();
        result = result.replace("\0", "");
        result = escapeHtml(result);
        if (result.length() > MAX_NAME_LENGTH) {
            result = result.substring(0, MAX_NAME_LENGTH);
        }
        return result;
    }

    public static String sanitizeEmail(String input) {
        if (input == null) {
            return null;
        }
        String result = input.trim().toLowerCase();
        result = result.replace("\0", "");
        if (result.length() > MAX_EMAIL_LENGTH) {
            result = result.substring(0, MAX_EMAIL_LENGTH);
        }
        return result;
    }

    public static String sanitizeWithLength(String input, int maxLength) {
        if (input == null) {
            return null;
        }
        String result = input.trim();
        result = result.replace("\0", "");
        result = escapeHtml(result);
        if (result.length() > maxLength) {
            result = result.substring(0, maxLength);
        }
        return result;
    }

    private static String escapeHtml(String input) {
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }
}
