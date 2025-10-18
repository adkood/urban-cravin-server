package com.ashutosh.urban_cravin.services.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Service
public class PhonePeAuthService {

    private static final Logger log = LoggerFactory.getLogger(PhonePeAuthService.class);

    @Value("${phonepe.client-id}")
    private String clientId;

    @Value("${phonepe.client-secret}")
    private String clientSecret;

    @Value("${phonepe.base-url}")
    private String baseUrl;

    private final WebClient webClient;
    private String accessToken;
    private long tokenExpiry;

    public PhonePeAuthService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    public String getAccessToken() {
        if (accessToken == null || System.currentTimeMillis() > tokenExpiry) {
            refreshToken();
        }
        return accessToken;
    }

    private void refreshToken() {
        try {
            log.info("Refreshing PhonePe OAuth token...");

            Map<String, String> formData = new HashMap<>();
            formData.put("client_id", clientId);
            formData.put("client_secret", clientSecret);
            formData.put("grant_type", "client_credentials");

            Map<String, Object> response = webClient.post()
                    .uri("/apis/merchant-auth/auth/v1/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(formData)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("access_token")) {
                this.accessToken = "Bearer " + response.get("access_token");
                long expiresIn = Long.parseLong(response.get("expires_in").toString());
                this.tokenExpiry = System.currentTimeMillis() + (expiresIn * 1000) - 300000; // 5 min buffer

                log.info("PhonePe OAuth token refreshed successfully");
            } else {
                throw new RuntimeException("Invalid token response: " + response);
            }

        } catch (Exception e) {
            log.error("Failed to refresh PhonePe OAuth token", e);
            throw new RuntimeException("PhonePe authentication failed", e);
        }
    }
}