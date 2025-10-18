package com.ashutosh.urban_cravin.controllers.payment;

import com.ashutosh.urban_cravin.helpers.enums.PaymentEventStatus;
import com.ashutosh.urban_cravin.helpers.enums.PaymentIntentStatus;
import com.ashutosh.urban_cravin.helpers.utils.PhonePeChecksum;
import com.ashutosh.urban_cravin.models.payment.PaymentEvent;
import com.ashutosh.urban_cravin.models.payment.PaymentIntent;
import com.ashutosh.urban_cravin.repositories.payment.PaymentEventRepo;
import com.ashutosh.urban_cravin.repositories.payment.PaymentIntentRepo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.Optional;

@RestController
@RequestMapping("/api/payments")
public class PaymentWebhookController {

    private static final Logger log = LoggerFactory.getLogger(PaymentWebhookController.class);

    private final PaymentEventRepo eventRepo;
    private final PaymentIntentRepo intentRepo;
    private final ObjectMapper objectMapper;

    @Value("${phonepe.client-secret}")
    private String apiKey;

    @Value("${phonepe.key-index}")
    private String keyIndex;

    public PaymentWebhookController(PaymentEventRepo eventRepo,
                                    PaymentIntentRepo intentRepo,
                                    ObjectMapper objectMapper) {
        this.eventRepo = eventRepo;
        this.intentRepo = intentRepo;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(@RequestHeader("X-VERIFY") String xVerify,
                                          @RequestBody String body) {

        log.info("Received webhook callback: {}", body);

        try {
            // Verify checksum
            if (!verifyChecksum(body, xVerify)) {
                log.warn("Invalid checksum received in webhook. X-VERIFY: {}", xVerify);
                return ResponseEntity.status(401).body("Invalid signature");
            }

            JsonNode response = objectMapper.readTree(body);
            String encodedResponse = response.path("response").asText();

            if (encodedResponse.isEmpty()) {
                log.warn("Empty response in webhook callback");
                return ResponseEntity.badRequest().body("Empty response");
            }

            String decodedResponse = new String(Base64.getDecoder().decode(encodedResponse));
            JsonNode callbackData = objectMapper.readTree(decodedResponse);

            JsonNode dataNode = callbackData.path("data");
            String merchantTxnId = dataNode.path("merchantTransactionId").asText();
            String transactionId = dataNode.path("transactionId").asText();
            String code = callbackData.path("code").asText();
            String state = dataNode.path("state").asText();

            if (merchantTxnId.isEmpty()) {
                log.warn("Missing merchantTransactionId in webhook");
                return ResponseEntity.badRequest().body("Missing merchantTransactionId");
            }

            // Log event
            PaymentEvent event = PaymentEvent.builder()
                    .eventType(PaymentEventStatus.CALLBACK_RECEIVED)
                    .payload(body)
                    .build();

            // Find and update payment intent
            Optional<PaymentIntent> intentOpt = intentRepo.findByMerchantTransactionId(merchantTxnId);
            if (intentOpt.isPresent()) {
                PaymentIntent intent = intentOpt.get();
                event.setPaymentIntentId(intent.getId());

                PaymentIntentStatus newStatus = determinePaymentStatus(code, state);
                if (newStatus != intent.getStatus()) {
                    intent.setStatus(newStatus);
                    intent.setTransactionId(transactionId);
                    intent.setLastResponse(body);
                    intentRepo.save(intent);

                    log.info("Payment status updated via webhook: {} -> {} (State: {})",
                            merchantTxnId, newStatus, state);

                    // Trigger business logic for successful payments
                    if (newStatus == PaymentIntentStatus.SUCCESS) {
                        triggerPostPaymentActions(intent);
                    }
                } else {
                    log.debug("Payment status unchanged: {} -> {}", merchantTxnId, newStatus);
                }
            } else {
                log.warn("Payment intent not found for merchantTransactionId: {}", merchantTxnId);
                event.setPaymentIntentId(null);
            }

            eventRepo.save(event);
            return ResponseEntity.ok("Webhook processed successfully");

        } catch (Exception e) {
            log.error("Webhook processing failed", e);
            return ResponseEntity.status(500).body("Processing failed");
        }
    }

    private PaymentIntentStatus determinePaymentStatus(String code, String state) {
        // Prioritize state over code for status determination
        if (state != null) {
            return switch (state.toUpperCase()) {
                case "COMPLETED", "SUCCESS" -> PaymentIntentStatus.SUCCESS;
                case "FAILED", "ERROR", "DECLINED" -> PaymentIntentStatus.FAILED;
                case "REFUNDED" -> PaymentIntentStatus.REFUNDED;
                default -> PaymentIntentStatus.PENDING;
            };
        }

        // Fallback to code if state is not available
        return switch (code) {
            case "PAYMENT_SUCCESS" -> PaymentIntentStatus.SUCCESS;
            case "PAYMENT_ERROR", "PAYMENT_DECLINED" -> PaymentIntentStatus.FAILED;
            default -> PaymentIntentStatus.PENDING;
        };
    }

    private boolean verifyChecksum(String payload, String xVerify) {
        try {
            String apiPath = "/pg/v1/webhook";
            String expectedChecksum = PhonePeChecksum.buildXVerify(payload, apiPath, apiKey, keyIndex);
            boolean isValid = expectedChecksum.equals(xVerify);

            if (!isValid) {
                log.warn("Checksum mismatch. Expected: {}, Received: {}", expectedChecksum, xVerify);
            }

            return isValid;
        } catch (Exception e) {
            log.error("Checksum verification failed", e);
            return false;
        }
    }

    private void triggerPostPaymentActions(PaymentIntent intent) {
        try {
            // TODO: Implement your business logic here
            // Examples:
            // - Update order status to "PAID"
            // - Send confirmation email to customer
            // - Trigger inventory management
            // - Notify shipping service
            // - Update analytics

            log.info("Triggering post-payment actions for: {}", intent.getMerchantTransactionId());

            // Example implementation:
            // orderService.updateOrderStatus(intent.getOrderId(), OrderStatus.PAID);
            // emailService.sendPaymentConfirmation(intent.getUserId(), intent);
            // inventoryService.updateStock(intent.getOrderId());

        } catch (Exception e) {
            log.error("Failed to trigger post-payment actions for: {}",
                    intent.getMerchantTransactionId(), e);
        }
    }

    // Optional: Add a GET endpoint to check webhook status (for testing)
    @GetMapping("/webhook/test")
    public ResponseEntity<String> testWebhook() {
        return ResponseEntity.ok("Webhook endpoint is active");
    }
}