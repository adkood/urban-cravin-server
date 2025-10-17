package com.ashutosh.urban_cravin.helpers.utils;

import com.ashutosh.urban_cravin.helpers.enums.PaymentIntentStatus;
import com.ashutosh.urban_cravin.models.payment.PaymentIntent;
import com.ashutosh.urban_cravin.repositories.payment.PaymentIntentRepo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class PaymentReconciliationScheduler {
    private static final Logger log = LoggerFactory.getLogger(PaymentReconciliationScheduler.class);

    private final PaymentIntentRepo intentRepo;
    private final WebClient phonePeWebClient;
    private final ObjectMapper objectMapper;

    @Value("${phonepe.merchant-id}")
    private String merchantId;

    @Value("${phonepe.api-key}")
    private String apiKey;

    @Value("${phonepe.key-index}")
    private String keyIndex;

    public PaymentReconciliationScheduler(PaymentIntentRepo intentRepo,
                                          WebClient phonePeWebClient,
                                          ObjectMapper objectMapper) {
        this.intentRepo = intentRepo;
        this.phonePeWebClient = phonePeWebClient;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelayString = "PT5M")
    public void reconcile() {
        try {
            // Find pending payments older than 10 minutes
            LocalDateTime threshold = LocalDateTime.now().minusMinutes(10);
            List<PaymentIntent> pendingPayments = intentRepo
                    .findByStatusInAndCreatedAtBefore(
                            List.of(PaymentIntentStatus.PENDING, PaymentIntentStatus.INITIATED),
                            threshold
                    );

            log.info("Reconciliation started. Found {} pending payments", pendingPayments.size());

            for (PaymentIntent intent : pendingPayments) {
                try {
                    checkPaymentStatus(intent);
                    // Small delay to avoid rate limiting
                    Thread.sleep(1000);
                } catch (Exception e) {
                    log.error("Failed to check status for payment: {}", intent.getMerchantTransactionId(), e);
                }
            }

            log.info("Reconciliation completed");
        } catch (Exception e) {
            log.error("Reconciliation job failed", e);
        }
    }

    private void checkPaymentStatus(PaymentIntent intent) {
        try {
            String merchantTransactionId = intent.getMerchantTransactionId();
            String apiPath = "/pg/v1/status/" + merchantId + "/" + merchantTransactionId;

            // Generate checksum for status API
            String checksumInput = apiPath + apiKey;
            String xVerify = PhonePeChecksum.buildXVerify("", apiPath, apiKey, keyIndex);

            String responseJson = phonePeWebClient.get()
                    .uri(apiPath)
                    .header("X-VERIFY", xVerify)
                    .header("X-MERCHANT-ID", merchantId)
                    .header("Content-Type", "application/json")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode response = objectMapper.readTree(responseJson);
            String code = response.path("code").asText();
            String state = response.path("data").path("state").asText();

            // Update payment status based on response
            PaymentIntentStatus newStatus = mapStatusFromState(state);
            if (newStatus != intent.getStatus()) {
                intent.setStatus(newStatus);
                intent.setLastResponse(responseJson);

                if (newStatus == PaymentIntentStatus.SUCCESS) {
                    String transactionId = response.path("data").path("transactionId").asText();
                    intent.setTransactionId(transactionId);
                }

                intentRepo.save(intent);
                log.info("Payment status updated: {} -> {}", merchantTransactionId, newStatus);

                // Trigger business logic for successful payments
                if (newStatus == PaymentIntentStatus.SUCCESS) {
                    triggerPostPaymentActions(intent);
                }
            }

        } catch (Exception e) {
            log.error("Failed to check status for payment: {}", intent.getMerchantTransactionId(), e);
        }
    }

    private PaymentIntentStatus mapStatusFromState(String state) {
        return switch (state.toUpperCase()) {
            case "COMPLETED", "SUCCESS" -> PaymentIntentStatus.SUCCESS;
            case "FAILED", "ERROR", "DECLINED" -> PaymentIntentStatus.FAILED;
            case "PENDING" -> PaymentIntentStatus.PENDING;
            case "REFUNDED" -> PaymentIntentStatus.REFUNDED;
            default -> PaymentIntentStatus.PENDING;
        };
    }

    private void triggerPostPaymentActions(PaymentIntent intent) {
        try {
            // TODO: Implement your business logic here
            // - Update order status
            // - Send confirmation email
            // - Trigger inventory update
            // - Notify other services

            log.info("Triggering post-payment actions for: {}", intent.getMerchantTransactionId());

        } catch (Exception e) {
            log.error("Failed to trigger post-payment actions for: {}", intent.getMerchantTransactionId(), e);
        }
    }
}