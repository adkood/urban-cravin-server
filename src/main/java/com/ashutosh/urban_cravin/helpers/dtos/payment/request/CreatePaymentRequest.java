package com.ashutosh.urban_cravin.helpers.dtos.payment.request;

import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CreatePaymentRequest {
    private String merchantTransactionId;
    private BigDecimal amount;
    private String mobile;
    private UUID userId; // Add this field
    private UUID orderId; // Add this field
}