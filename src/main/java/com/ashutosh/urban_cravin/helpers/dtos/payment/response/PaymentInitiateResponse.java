package com.ashutosh.urban_cravin.helpers.dtos.payment.response;

import com.ashutosh.urban_cravin.helpers.enums.PaymentIntentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentInitiateResponse {
    private String merchantTransactionId;
    private String redirectUrl;
    private PaymentIntentStatus status;
}
