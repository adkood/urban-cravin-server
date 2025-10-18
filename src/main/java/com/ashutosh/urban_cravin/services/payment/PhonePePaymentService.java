package com.ashutosh.urban_cravin.services.payment;

import com.ashutosh.urban_cravin.helpers.dtos.payment.request.CreatePaymentRequest;
import com.ashutosh.urban_cravin.helpers.dtos.payment.response.PaymentInitiateResponse;
import com.ashutosh.urban_cravin.helpers.enums.PaymentIntentStatus;
import com.ashutosh.urban_cravin.helpers.utils.PhonePeChecksum;
import com.ashutosh.urban_cravin.models.payment.PaymentIntent;
import com.ashutosh.urban_cravin.repositories.payment.PaymentIntentRepo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class PhonePePaymentService {

    private static final Logger log = LoggerFactory.getLogger(PhonePePaymentService.class);

    private final PhonePeAuthService authService;
    private final WebClient phonePeWebClient;
    private final PaymentIntentRepo intentRepo;
    private final ObjectMapper objectMapper;
    private final Retry retry;
    private final CircuitBreaker cb;

    @Value("${phonepe.client-id}")
    private String clientId;

    @Value("${phonepe.client-secret}")
    private String clientSecret;

    @Value("${phonepe.key-index}")
    private String keyIndex;

    @Value("${phonepe.redirect-url}")
    private String redirectUrl;

    @Value("${phonepe.callback-url}")
    private String callbackUrl;

    @Value("${phonepe.base-url}")
    private String baseUrl;

    public PhonePePaymentService(PhonePeAuthService authService,
                                 WebClient phonePeWebClient,
                                 PaymentIntentRepo intentRepo,
                                 ObjectMapper objectMapper,
                                 RetryRegistry retryRegistry,
                                 CircuitBreakerRegistry cbRegistry) {
        this.authService = authService;
        this.phonePeWebClient = phonePeWebClient;
        this.intentRepo = intentRepo;
        this.objectMapper = objectMapper;
        this.retry = retryRegistry.retry("phonepeRetry");
        this.cb = cbRegistry.circuitBreaker("phonepeCircuitBreaker");
    }

    public PaymentInitiateResponse initiate(CreatePaymentRequest req) throws Exception {
        return cb.executeSupplier(() ->
                retry.executeSupplier(() -> {
                    try {
                        return initiatePayment(req);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
        );
    }

    private PaymentInitiateResponse initiatePayment(CreatePaymentRequest req) throws Exception {
        String merchantTxnId = Optional.ofNullable(req.getMerchantTransactionId())
                .orElse(UUID.randomUUID().toString());

        // Idempotency check
        Optional<PaymentIntent> existingIntent = intentRepo.findByMerchantTransactionId(merchantTxnId);
        if (existingIntent.isPresent()) {
            PaymentIntent intent = existingIntent.get();
            if (intent.getStatus() == PaymentIntentStatus.SUCCESS) {
                throw new IllegalArgumentException("Payment already completed for this transaction");
            }
            return buildResponseFromIntent(intent);
        }

        // Create new payment intent
        PaymentIntent intent = PaymentIntent.builder()
                .merchantTransactionId(merchantTxnId)
                .userId(req.getUserId())
                .orderId(req.getOrderId())
                .amount(req.getAmount().multiply(BigDecimal.valueOf(100))) // Convert to paise
                .status(PaymentIntentStatus.INITIATED)
                .build();

        // Build PhonePe payload
        Map<String, Object> payload = buildPhonePePayload(merchantTxnId, req);
        String jsonPayload = objectMapper.writeValueAsString(payload);

        intent.setRequestPayload(jsonPayload);
        intent = intentRepo.save(intent);

        try {
            // Prepare API call - Business API uses OAuth + checksum
            String apiPath = "/apis/pg-sandbox/pg/v1/pay";

            // Generate checksum
            String xVerify = PhonePeChecksum.buildXVerify(jsonPayload, apiPath, clientSecret, keyIndex);
            String base64Payload = Base64.getEncoder()
                    .encodeToString(jsonPayload.getBytes(StandardCharsets.UTF_8));

            Map<String, String> requestBody = Map.of("request", base64Payload);

            // Get OAuth token
            String accessToken = authService.getAccessToken();

            log.info("Initiating payment with merchantTxnId: {}", merchantTxnId);

            // Make API call with both OAuth and checksum
            String responseJson = phonePeWebClient.post()
                    .uri(apiPath)
                    .header("Authorization", accessToken)
                    .header("X-VERIFY", xVerify)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.debug("PhonePe Business API response: {}", responseJson);

            // Parse response
            JsonNode response = objectMapper.readTree(responseJson);

            boolean success = response.path("success").asBoolean();
            String code = response.path("code").asText();
            String message = response.path("message").asText();

            if (success && "PAYMENT_INITIATED".equals(code)) {
                intent.setStatus(PaymentIntentStatus.PENDING);
                intent.setLastResponse(responseJson);

                // Extract redirect URL from response
                String extractedRedirectUrl = extractRedirectUrl(response);

                if (extractedRedirectUrl != null && !extractedRedirectUrl.isEmpty()) {
                    intentRepo.save(intent);
                    return new PaymentInitiateResponse(merchantTxnId, extractedRedirectUrl, PaymentIntentStatus.PENDING);
                } else {
                    log.warn("Redirect URL not found in response for merchantTxnId: {}", merchantTxnId);
                    throw new RuntimeException("Redirect URL not provided by payment gateway");
                }
            }

            // Handle failure
            intent.setStatus(PaymentIntentStatus.FAILED);
            intent.setLastResponse(responseJson);
            intentRepo.save(intent);

            throw new RuntimeException("Payment initiation failed: " + message);

        } catch (WebClientResponseException e) {
            log.error("PhonePe API error - Status: {}, Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            intent.setStatus(PaymentIntentStatus.FAILED);
            intent.setLastResponse(e.getResponseBodyAsString());
            intentRepo.save(intent);
            throw new RuntimeException("Payment service unavailable: " + e.getStatusCode(), e);
        } catch (Exception e) {
            log.error("Payment initiation failed for merchantTxnId: {}", merchantTxnId, e);
            intent.setStatus(PaymentIntentStatus.FAILED);
            intent.setLastResponse(e.getMessage());
            intentRepo.save(intent);
            throw e;
        }
    }

    private String extractRedirectUrl(JsonNode response) {
        try {
            JsonNode data = response.path("data");

            // Business API response structure
            if (data.has("instrumentResponse")) {
                JsonNode instrumentResponse = data.path("instrumentResponse");

                if (instrumentResponse.has("redirectInfo")) {
                    JsonNode redirectInfo = instrumentResponse.path("redirectInfo");
                    if (redirectInfo.has("url")) {
                        return redirectInfo.path("url").asText();
                    }
                }

                // Fallback: check if redirectInfo is a string containing URL
                String redirectInfoStr = instrumentResponse.path("redirectInfo").asText();
                if (redirectInfoStr.contains("http")) {
                    return redirectInfoStr;
                }
            }

            // Alternative path
            if (data.has("redirectUrl")) {
                return data.path("redirectUrl").asText();
            }

            return null;
        } catch (Exception e) {
            log.warn("Failed to extract redirect URL from response", e);
            return null;
        }
    }

    private Map<String, Object> buildPhonePePayload(String merchantTxnId, CreatePaymentRequest req) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("merchantId", clientId);  // Use clientId as merchantId
        payload.put("merchantTransactionId", merchantTxnId);
        payload.put("amount", req.getAmount().multiply(BigDecimal.valueOf(100)).longValue());
        payload.put("redirectUrl", redirectUrl);
        payload.put("redirectMode", "REDIRECT");
        payload.put("callbackUrl", callbackUrl);
        payload.put("paymentInstrument", Map.of("type", "PAY_PAGE"));

        if (req.getMobile() != null) {
            payload.put("mobileNumber", req.getMobile());
        }

        if (req.getUserId() != null) {
            payload.put("merchantUserId", req.getUserId().toString());
        }

        return payload;
    }

    private PaymentInitiateResponse buildResponseFromIntent(PaymentIntent intent) {
        String redirectUrl = "";
        try {
            if (intent.getLastResponse() != null) {
                JsonNode response = objectMapper.readTree(intent.getLastResponse());
                redirectUrl = extractRedirectUrl(response);
            }
        } catch (Exception e) {
            log.debug("Could not extract redirect URL from existing intent");
        }

        return new PaymentInitiateResponse(
                intent.getMerchantTransactionId(),
                redirectUrl != null ? redirectUrl : "",
                intent.getStatus()
        );
    }
}