package com.kalan.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Minimal PayPal REST client (Orders v2) using the JDK HttpClient — no extra
 * dependencies. Handles: OAuth token, create order, capture order.
 * Keys come from env vars (PAYPAL_CLIENT_ID / PAYPAL_SECRET); base-url is the
 * sandbox by default and switches to live via PAYPAL_BASE_URL.
 */
@Service
public class PayPalClient {

    @Value("${paypal.base-url}")  private String baseUrl;
    @Value("${paypal.client-id}") private String clientId;
    @Value("${paypal.secret}")    private String secret;

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public boolean isConfigured() {
        return clientId != null && !clientId.isBlank()
            && secret != null && !secret.isBlank();
    }

    // ── OAuth: client-credentials → access token ──────────────────────────────
    private String accessToken() throws Exception {
        String creds = Base64.getEncoder().encodeToString(
            (clientId + ":" + secret).getBytes(StandardCharsets.UTF_8));
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/v1/oauth2/token"))
            .header("Authorization", "Basic " + creds)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials"))
            .build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() >= 300) {
            throw new RuntimeException("PayPal auth failed (" + res.statusCode() + "): " + res.body());
        }
        return mapper.readTree(res.body()).get("access_token").asText();
    }

    // ── Create an order → returns { orderId, approveUrl } ─────────────────────
    public Map<String, String> createOrder(String value, String currency,
                                           String description, String returnUrl,
                                           String cancelUrl) throws Exception {
        String token = accessToken();

        Map<String, Object> amount = Map.of("currency_code", currency, "value", value);
        Map<String, Object> unit = Map.of("amount", amount, "description", description);
        Map<String, Object> appCtx = new HashMap<>();
        appCtx.put("return_url", returnUrl);
        appCtx.put("cancel_url", cancelUrl);
        appCtx.put("user_action", "PAY_NOW");
        appCtx.put("brand_name", "Kalan");
        appCtx.put("shipping_preference", "NO_SHIPPING");
        Map<String, Object> body = Map.of(
            "intent", "CAPTURE",
            "purchase_units", new Object[]{unit},
            "application_context", appCtx
        );

        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/v2/checkout/orders"))
            .header("Authorization", "Bearer " + token)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
            .build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() >= 300) {
            throw new RuntimeException("PayPal create order failed (" + res.statusCode() + "): " + res.body());
        }

        JsonNode node = mapper.readTree(res.body());
        String orderId = node.get("id").asText();
        String approveUrl = null;
        for (JsonNode link : node.withArray("links")) {
            String rel = link.path("rel").asText();
            if (rel.equals("approve") || rel.equals("payer-action")) {
                approveUrl = link.path("href").asText();
                break;
            }
        }
        Map<String, String> out = new HashMap<>();
        out.put("orderId", orderId);
        out.put("approveUrl", approveUrl);
        return out;
    }

    // ── Capture an approved order → returns status (e.g. COMPLETED) ────────────
    public String captureOrder(String orderId) throws Exception {
        String token = accessToken();
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/v2/checkout/orders/" + orderId + "/capture"))
            .header("Authorization", "Bearer " + token)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString("{}"))
            .build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() >= 300) {
            throw new RuntimeException("PayPal capture failed (" + res.statusCode() + "): " + res.body());
        }
        return mapper.readTree(res.body()).path("status").asText();
    }
}