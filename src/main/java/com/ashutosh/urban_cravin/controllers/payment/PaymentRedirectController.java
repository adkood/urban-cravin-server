package com.ashutosh.urban_cravin.controllers.payment;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class PaymentRedirectController {

    private static final Logger log = LoggerFactory.getLogger(PaymentRedirectController.class);

    @GetMapping("/payment/redirect")
    public String handlePaymentRedirect(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String transactionId,
            @RequestParam(required = false) String merchantTransactionId,
            Model model) {

        log.info("Payment redirect received - Code: {}, TransactionId: {}, MerchantTxnId: {}",
                code, transactionId, merchantTransactionId);

        model.addAttribute("code", code);
        model.addAttribute("transactionId", transactionId);
        model.addAttribute("merchantTransactionId", merchantTransactionId);

        if ("PAYMENT_SUCCESS".equals(code)) {
            model.addAttribute("message", "Payment Successful!");
            model.addAttribute("isSuccess", true);
            return "payment-success";
        } else {
            model.addAttribute("message", "Payment Failed. Please try again.");
            model.addAttribute("isSuccess", false);
            return "payment-failed";
        }
    }
}