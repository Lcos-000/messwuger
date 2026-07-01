package com.campusassistant.utils;

public class TokenTool {

    public static String normalizeToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        if (token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }

}
