package com.ashutosh.urban_cravin.helpers.utils;

import com.ashutosh.urban_cravin.helpers.enums.PaymentIntentStatus;
import com.ashutosh.urban_cravin.models.payment.PaymentIntent;
import com.ashutosh.urban_cravin.repositories.payment.PaymentIntentRepo;
import com.ashutosh.urban_cravin.services.payment.PhonePeAuthService;
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
    private final PhonePeAuthService authService;

    @Value("${phonepe.client-id}")
    private String clientId;

    @Value("${phonepe.client-secret}")
    private String clientSecret;

    @Value("${phonepe.key-index}")
    private String keyIndex;

    public PaymentReconciliationScheduler(PaymentIntentRepo intentRepo,
                                          WebClient phonePeWebClient,
                                          ObjectMapper objectMapper,
                                          PhonePeAuthService authService) {
        this.intentRepo = intentRepo;
        this.phonePeWebClient = phonePeWebClient;
        this.objectMapper = objectMapper;
        this.authService = authService;
    }

    @Scheduled(fixedDelayString = "PT5M")
    public void reconcile() {
        try {
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
                    Thread.sleep(1000); // Rate limiting
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
            String apiPath = "/apis/pg-sandbox/pg/v1/status/" + clientId + "/" + merchantTransactionId;

            // Generate checksum
            String xVerify = PhonePeChecksum.buildXVerify("", apiPath, clientSecret, keyIndex);

            // Get OAuth token
            String accessToken = authService.getAccessToken();

            String responseJson = phonePeWebClient.get()
                    .uri(apiPath)
                    .header("Authorization", accessToken)
                    .header("X-VERIFY", xVerify)
                    .header("Content-Type", "application/json")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode response = objectMapper.readTree(responseJson);
            String code = response.path("code").asText();
            String state = response.path("data").path("state").asText();

            PaymentIntentStatus newStatus = mapStatusFromState(state);
            if (newStatus != intent.getStatus()) {
                intent.setStatus(newStatus);
                intent.setLastResponse(responseJson);

                if (newStatus == PaymentIntentStatus.SUCCESS) {
                    String transactionId = response.path("data").path("transactionId").asText();
                    intent.setTransactionId(transactionId);
                }

                intentRepo.save(intent);
                log.info("Payment status updated via reconciliation: {} -> {}", merchantTransactionId, newStatus);

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
            // Implement your business logic here
            log.info("Triggering post-payment actions for: {}", intent.getMerchantTransactionId());

            // Example:
            // orderService.updateOrderStatus(intent.getOrderId(), OrderStatus.PAID);
            // emailService.sendPaymentConfirmation(intent.getUserId(), intent);

        } catch (Exception e) {
            log.error("Failed to trigger post-payment actions for: {}", intent.getMerchantTransactionId(), e);
        }
    }
}