package com.ashutosh.urban_cravin.controllers.payment;

import com.ashutosh.urban_cravin.helpers.dtos.payment.request.CreatePaymentRequest;
import com.ashutosh.urban_cravin.helpers.dtos.payment.response.PaymentInitiateResponse;
import com.ashutosh.urban_cravin.services.payment.PhonePePaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PhonePePaymentService paymentService;

    public PaymentController(PhonePePaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/initiate")
    public ResponseEntity<PaymentInitiateResponse> initiate(@RequestBody CreatePaymentRequest req) throws Exception {
        PaymentInitiateResponse res = paymentService.initiate(req);
        return ResponseEntity.ok(res);
    }
}