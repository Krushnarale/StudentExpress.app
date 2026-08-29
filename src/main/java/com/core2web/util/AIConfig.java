package com.core2web.util;

public class AIConfig {

    public static String getApiKey() {
        String key = System.getenv("GEMINI_API_KEY");
        if (key != null && !key.trim().isEmpty()) {
            return key.trim();
        }
        return null;
    }

    public static boolean isApiKeyAvailable() {
        String key = getApiKey();
        return key != null && !key.isEmpty();
    }
}