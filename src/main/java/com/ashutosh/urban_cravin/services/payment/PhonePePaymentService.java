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

    private final WebClient phonePeWebClient;
    private final PaymentIntentRepo intentRepo;
    private final ObjectMapper objectMapper;
    private final Retry retry;
    private final CircuitBreaker cb;

    @Value("${phonepe.merchant-id}")
    private String merchantId;

    @Value("${phonepe.api-key}")
    private String apiKey;

    @Value("${phonepe.key-index}")
    private String keyIndex;

    @Value("${phonepe.redirect-url}")
    private String redirectUrl;

    @Value("${phonepe.callback-url}")
    private String callbackUrl;

    @Value("${phonepe.base-url}")
    private String baseUrl;

    public PhonePePaymentService(WebClient phonePeWebClient,
                                 PaymentIntentRepo intentRepo,
                                 ObjectMapper objectMapper,
                                 RetryRegistry retryRegistry,
                                 CircuitBreakerRegistry cbRegistry) {
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
                .amount(req.getAmount().multiply(BigDecimal.valueOf(100))) // Convert to paise
                .status(PaymentIntentStatus.INITIATED)
                .build();

        // Build PhonePe payload
        Map<String, Object> payload = buildPhonePePayload(merchantTxnId, req);
        String jsonPayload = objectMapper.writeValueAsString(payload);

        intent.setRequestPayload(jsonPayload);
        intent = intentRepo.save(intent);

        try {
            // Prepare API call
            String apiPath = "/pg/v1/pay";
            String xVerify = PhonePeChecksum.buildXVerify(jsonPayload, apiPath, apiKey, keyIndex);
            String base64Payload = Base64.getEncoder()
                    .encodeToString(jsonPayload.getBytes(StandardCharsets.UTF_8));

            Map<String, String> requestBody = Map.of("request", base64Payload);

            log.info("Initiating payment with merchantTxnId: {}", merchantTxnId);

            // Make API call
            String responseJson = phonePeWebClient.post()
                    .uri(apiPath)
                    .header("X-VERIFY", xVerify)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.debug("PhonePe API response: {}", responseJson);

            // Parse response using JsonNode for flexibility
            JsonNode response = objectMapper.readTree(responseJson);

            boolean success = response.path("success").asBoolean();
            String code = response.path("code").asText();
            String message = response.path("message").asText();

            if (success && "PAYMENT_INITIATED".equals(code)) {
                intent.setStatus(PaymentIntentStatus.PENDING);
                intent.setLastResponse(responseJson);

                // Extract redirect URL from response
                String redirectUrl = extractRedirectUrl(response);

                if (redirectUrl != null) {
                    intentRepo.save(intent);
                    return new PaymentInitiateResponse(merchantTxnId, redirectUrl, PaymentIntentStatus.PENDING);
                } else {
                    log.warn("Redirect URL not found in response for merchantTxnId: {}", merchantTxnId);
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
            // Try different possible paths for redirect URL
            JsonNode data = response.path("data");

            // Path 1: data.instrumentResponse.redirectInfo.url
            JsonNode redirectInfo = data.path("instrumentResponse").path("redirectInfo");
            if (!redirectInfo.isMissingNode() && redirectInfo.has("url")) {
                return redirectInfo.path("url").asText();
            }

            // Path 2: data.instrumentResponse.redirectInfo
            String redirectInfoStr = data.path("instrumentResponse").path("redirectInfo").asText();
            if (!redirectInfoStr.isEmpty() && redirectInfoStr.contains("url=")) {
                return redirectInfoStr.substring(redirectInfoStr.indexOf("url=") + 4);
            }

            // Path 3: data.instrumentResponse (direct URL)
            String instrumentResponse = data.path("instrumentResponse").asText();
            if (!instrumentResponse.isEmpty()) {
                return instrumentResponse;
            }

            // Path 4: Check if URL is directly in data
            if (data.has("url")) {
                return data.path("url").asText();
            }

            return null;
        } catch (Exception e) {
            log.warn("Failed to extract redirect URL from response", e);
            return null;
        }
    }

    private Map<String, Object> buildPhonePePayload(String merchantTxnId, CreatePaymentRequest req) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("merchantId", merchantId);
        payload.put("merchantTransactionId", merchantTxnId);
        payload.put("amount", req.getAmount().multiply(BigDecimal.valueOf(100)).longValue()); // in paise
        payload.put("redirectUrl", redirectUrl);
        payload.put("redirectMode", "REDIRECT");
        payload.put("callbackUrl", callbackUrl);
        payload.put("paymentInstrument", Map.of("type", "PAY_PAGE"));

        if (req.getMobile() != null) {
            payload.put("mobileNumber", req.getMobile());
        }

        // Add merchant user ID if available
        if (req.getUserId() != null) {
            payload.put("merchantUserId", req.getUserId().toString());
        }

        return payload;
    }

    private PaymentInitiateResponse buildResponseFromIntent(PaymentIntent intent) {
        // For existing intents, check if we have a redirect URL in lastResponse
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