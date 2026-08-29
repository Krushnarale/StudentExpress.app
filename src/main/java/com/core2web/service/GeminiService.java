package com.core2web.service;

import com.core2web.util.AIConfig;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeminiService {

private static final String API_URL_TEMPLATE =
    "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent?key=%s";
    private static final String SYSTEM_INSTRUCTION =
        "You are StudentExpress AI, an assistant for a student rental and marketplace platform. "
      + "Help students understand and navigate StudentExpress. "
      + "You can explain rooms/PGs, rentals, buying and selling, roommates, student services, bookings and application navigation. "
      + "Do not invent listings, prices, availability, users, bookings or database information. "
      + "If real StudentExpress data is not available to the AI, clearly tell the user that you can only provide guidance and cannot confirm live data. "
      + "Keep responses concise, friendly and student-focused.";

    private final HttpClient httpClient;
    private final Gson gson;
    private final List<Map<String, Object>> chatHistory;

    public GeminiService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.gson = new Gson();
        this.chatHistory = new ArrayList<>();
    }

    public void clearHistory() {
        chatHistory.clear();
    }

    public String sendMessage(String userMessage) throws Exception {
        if (!AIConfig.isApiKeyAvailable()) {
            throw new IllegalStateException("Gemini API key is not configured.");
        }

        String apiKey = AIConfig.getApiKey();
        String endpoint = String.format(API_URL_TEMPLATE, apiKey);

        // Build user content (not added to history until success)
        Map<String, Object> userContent = new HashMap<>();
        userContent.put("role", "user");
        List<Map<String, String>> userParts = new ArrayList<>();
        Map<String, String> userPart = new HashMap<>();
        userPart.put("text", userMessage);
        userParts.add(userPart);
        userContent.put("parts", userParts);

        List<Map<String, Object>> requestContents = new ArrayList<>(chatHistory);
        requestContents.add(userContent);

        Map<String, Object> systemInstructionObj = new HashMap<>();
        List<Map<String, String>> systemParts = new ArrayList<>();
        Map<String, String> systemPart = new HashMap<>();
        systemPart.put("text", SYSTEM_INSTRUCTION);
        systemParts.add(systemPart);
        systemInstructionObj.put("parts", systemParts);

        Map<String, Object> payload = new HashMap<>();
        payload.put("system_instruction", systemInstructionObj);
        payload.put("contents", requestContents);

        String jsonPayload = gson.toJson(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(60))
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        // Retry up to 2 times for transient failures
        int maxRetries = 2;
        Exception lastException = null;
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                int status = response.statusCode();

                // Retry on transient server errors
                if (status == 429 || status == 500 || status == 503) {
                    lastException = new RuntimeException(
                        "Gemini API Error (" + status + "): " + extractErrorMessage(response.body()));
                    if (attempt < maxRetries) {
                        Thread.sleep(2000L * (attempt + 1));
                        continue;
                    }
                    throw lastException;
                }

                if (status != 200) {
                    // Non-retryable error (400, 401, 403, 404, etc.) — throw immediately
                    throw new RuntimeException(
                        "Gemini API Error (" + status + "): " + extractErrorMessage(response.body()));
                }

                String replyText = parseReplyText(response.body());
                if (replyText == null || replyText.trim().isEmpty()) {
                    throw new RuntimeException("Received an empty or blocked response from Gemini. Please try again.");
                }

                // Only update history after a confirmed successful response
                chatHistory.add(userContent);
                Map<String, Object> modelContent = new HashMap<>();
                modelContent.put("role", "model");
                List<Map<String, String>> modelParts = new ArrayList<>();
                Map<String, String> modelPart = new HashMap<>();
                modelPart.put("text", replyText);
                modelParts.add(modelPart);
                modelContent.put("parts", modelParts);
                chatHistory.add(modelContent);

                return replyText;

            } catch (HttpTimeoutException e) {
                lastException = new RuntimeException(
                    "Request timed out. Please check your connection and try again.");
                if (attempt < maxRetries) {
                    Thread.sleep(2000L * (attempt + 1));
                } else {
                    throw lastException;
                }
            }
        }
        throw lastException != null ? lastException
            : new RuntimeException("Unable to get a response from Gemini. Please try again.");
    }

    private String parseReplyText(String responseBody) {
        try {
            JsonObject root = JsonParser.parseString(responseBody).getAsJsonObject();

            // Check for a top-level promptFeedback block reason (content was blocked)
            if (root.has("promptFeedback")) {
                JsonObject feedback = root.getAsJsonObject("promptFeedback");
                if (feedback.has("blockReason")) {
                    return "I'm unable to respond to that message. Please rephrase and try again.";
                }
            }

            JsonArray candidates = root.getAsJsonArray("candidates");
            if (candidates == null || candidates.size() == 0) {
                return null;
            }

            JsonObject candidate = candidates.get(0).getAsJsonObject();

            // Check if this candidate was blocked
            if (candidate.has("finishReason")) {
                String finishReason = candidate.get("finishReason").getAsString();
                if ("SAFETY".equals(finishReason) || "BLOCKED".equals(finishReason)) {
                    return "I'm unable to respond to that message. Please rephrase and try again.";
                }
            }

            JsonObject content = candidate.getAsJsonObject("content");
            if (content == null) return null;

            JsonArray parts = content.getAsJsonArray("parts");
            if (parts == null || parts.size() == 0) return null;

            JsonObject firstPart = parts.get(0).getAsJsonObject();
            if (!firstPart.has("text")) return null;

            return firstPart.get("text").getAsString();

        } catch (Exception e) {
            System.err.println("[GeminiService] Response parsing error: " + e.getMessage());
        }
        return null;
    }

    private String extractErrorMessage(String responseBody) {
        try {
            JsonObject root = JsonParser.parseString(responseBody).getAsJsonObject();
            if (root.has("error")) {
                JsonObject error = root.getAsJsonObject("error");
                if (error.has("message")) {
                    return error.get("message").getAsString();
                }
            }
        } catch (Exception ignored) {}
        return "HTTP error during communication.";
    }
}